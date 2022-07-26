package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 关注逻辑
 *
 * @author lsz on 2022/1/29
 */
@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityId,int entityType){
        // 获取当前用户
        User user = hostHolder.getUser();
        // 关注
        followService.follow(user.getId(),entityId,entityType);
        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(entityId)
                .setEntityType(entityType)
                .setEntityUserId(entityId);
        // 发布事件
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注！");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityId,int entityType){
        // 获取当前用户
        User user = hostHolder.getUser();
        // 取消关注
        followService.unfollow(user.getId(),entityId,entityType);

        return CommunityUtil.getJSONString(0,"已取消关注！");
    }


    /**
     * 分页获取某个用户关注目标用户列表
     * @param userId 某个用户
     * @param page 分页信息
     * @param model 模板
     * @return
     */
    @GetMapping(value= "/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }


    /**
     * 分页获取某个用户粉丝列表
     * @param userId 某个用户 id
     * @param page
     * @param model
     * @return
     */
    @GetMapping(value = "/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/follower";
    }

    /**
     * 判断当前用户是否关注了某个用户
     * @param userId 目标用户
     * @return
     */
    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) { // 未登录，默认未关注
            return false;
        }

        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

}