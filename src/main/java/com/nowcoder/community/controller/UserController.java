package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 用户模块
 *
 * @author lsz on 2022/1/15
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    /**
     * 跳转用户页
     * @return
     */
    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model){
        return "/site/setting";
    }

    /**
     * 上传用户头像
     * @param headerImage 上传文件类型
     * @param model
     * @return
     */
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        // 判断有没有图片
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "site/setting";
        }

        // 通过图片的后缀名来判断图片格式
        String fileName = headerImage.getOriginalFilename(); // 获得原始文件名
        String suffix = fileName.substring(fileName.lastIndexOf(".")); // 获得文件后缀名
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确！");
            return "site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径，这里先存到服务器上
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件 (可以检查uploadPath是否存在, 否则创建该目录)
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：", e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }

        // 更新当前用户头像的路径(web访问路径)
        // http://localhost:8083/community/user/header/xxx.png
        User user = hostHolder.getUser(); // 得到当前用户
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl); // 更新数据库

        return "redirect:/index";
    }

    /**
     * 返回用户头像
     * 对应上文的 headerUrl
     * @param fileName 文件名
     * @param response 响应流-输出到浏览器
     */
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 设置响应格式为图片
        response.setContentType("image/" + suffix); // 固定写法
        FileInputStream fis = null;
        OutputStream os = null;
        try {
            // 输入字节流
            fis = new FileInputStream(fileName);
            // 输出字节流
            os = response.getOutputStream();
             // 输入缓冲区
             byte[] buffer = new byte[1024];
             // 游标
             int b = 0;
             // 读取文件
             while ((b = fis.read(buffer)) != -1) { // 读到b个字节的数据, 最大1024个字节，未读到文件末尾
                 // 输出
                 os.write(buffer, 0, b);
             }
        } catch (IOException e) {
            logger.error("读取文件失败：", e.getMessage());
        }finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 更新用户密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @param model
     * @return
     */
    @PostMapping("/updatePassword")
    public String updatePassword(String oldPassword,String newPassword,String confirmPassword,Model model){
        // 得到当前用户
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword,confirmPassword);
        // 修改成功，退出登录
        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            model.addAttribute("confirmPasswordMsg", map.get("confirmPasswordMsg"));
            return "/site/setting";
        }

    }

    /**
     * 跳转个人主页，可以跳转到任意用户主页
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        // 用户信息
        model.addAttribute("user",user);
        // 点赞数量
        long likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // 关注数量(用户)
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量（用户）
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注（用户）
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    /**
     * 分页显示用户发布的帖子列表
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @GetMapping(value = "/mypost/{userId}")
    public String getMyPost(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);
        // 分页信息
        page.setPath("/user/mypost/" + userId);
        page.setRows(discussPostService.findDiscussPostRows(userId));
        page.setLimit(5);

        // 帖子列表
        List<DiscussPost> discussList = discussPostService.findDiscussPosts(userId,
                page.getOffset(),page.getLimit());
        // 结果集
        List<Map<String,Object>> discussVOList = new ArrayList<>();
        if(discussList != null){
            for(DiscussPost discussPost : discussList){
                Map<String,Object> map = new HashMap<>(16);
                map.put("discussPost",discussPost);
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPost.getId()));
                discussVOList.add(map);
            }
        }
        model.addAttribute("discussPosts",discussVOList);
        return "/site/my-post";
    }

    /**
     * 分页显示用户发布针对于帖子的回复列表
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @GetMapping(value = "/myreply/{userId}")
    public String getMyReply(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);
        // 分页信息
        page.setPath("/user/myreply/" + userId);
        page.setRows(commentService.findUserCount(userId));
        page.setLimit(5);

        // 回复列表
        List<Comment> commentList = commentService.findUserComments(userId,
                page.getOffset(),page.getLimit());
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> map = new HashMap<>(16);
                // 评论
                map.put("comment", comment);
                // 回复的帖子
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("discussPost", post);
                commentVOList.add(map);
            }
        }
        model.addAttribute("comments", commentVOList);

        return "/site/my-reply";
    }
}