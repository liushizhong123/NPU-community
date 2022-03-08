package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 首页(里面的帖子分页展示)
     * @param model
     * @param page
     * @return
     */
    @GetMapping(value = "/index")
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode",defaultValue = "0") int orderMode) {
        // 方法调用栈,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode");

        List<DiscussPost> list = discussPostService
                .findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        // 返回前端数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);// 存放帖子信息
                User user = userService.findUserById(post.getUserId());
                map.put("user", user); // 存放帖子用户信息

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        // 注入model
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode",orderMode);
        // 返回首页
        return "/index";
    }

    /**
     * 跳转异常处理错误页面
     * @return
     */
    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }

    /**
     * 跳转权限不足处理页面
     * @return
     */
    @GetMapping("/denied")
    public String getDeniedPage(){
        return "/error/404";
    }

}
