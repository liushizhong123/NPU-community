package com.nowcoder.community.controller;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 帖子相关逻辑
 *
 * @author lsz on 2022/1/19
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 添加帖子
     * @param title 标题
     * @param content 内容
     * @return
     */
    @PostMapping(value = "/add")
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还未登录，请登录！");
        }
        // 创建帖子并插入
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId()); // 用户id
        post.setTitle(title); // 标题
        post.setContent(content); // 内容
        post.setCreateTime(new Date()); // 创建时间
        discussPostService.addDiscussPost(post);

        // 将帖子id存入redis,以便之后计算帖子的分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        // 使用集合存储，天然去重
        redisTemplate.opsForSet().add(redisKey,post.getId());

        // 报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    /**
     * 帖子详情
     * @param discussPostId
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);


        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount()); // 帖子总评论数

        /*
         * 1 评论列表
         * VO(View Object 显示对象)
         * 评论：给帖子的评论 - post.getId(), 且 targetId = 0, 表示对当前用户帖子的评论
         * 回复：给评论的评论 - comment.getId() 且 targetId != 0, 表示别的用户对当前帖子用户的评论的回复
         * */
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 1 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>(16);
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(
                                hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 2 回复列表 (评论的评论) - comment.getId()
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE); // 设置 从 0 到最大, 即 有多少条查多少, 不做分页
                // 2 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>(16);
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标，即帖子评论的用户
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(
                                        hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                // 最后加入到集合中
                commentVoList.add(commentVo);
            }
        }
        // 这里的commentVoList为所有评论以及回复
        model.addAttribute("comments", commentVoList);

        return "site/discuss-detail";
    }


    /**
     * 置顶帖子
     * @param id
     * @return
     */
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);

        // 返回响应码，异步请求
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 加精帖子
     * @param id
     * @return
     */
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);

        // 将帖子 id 存入redis,以便之后计算帖子的分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        // 返回响应码，异步请求
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 删除帖子
     * @param id
     * @return
     */
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);

        // 返回响应码，异步请求
        return CommunityUtil.getJSONString(0);
    }
}