package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 关注逻辑
 *
 * @author lsz on 2022/1/29
 */
@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/follow")
    @ResponseBody
    @LoginRequired
    public String follow(int entityId,int entityType){
        // 获取当前用户
        User user = hostHolder.getUser();
        // 点赞
        followService.follow(user.getId(),entityId,entityType);

        return CommunityUtil.getJSONString(0,"已关注！");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    @LoginRequired
    public String unfollow(int entityId,int entityType){
        // 获取当前用户
        User user = hostHolder.getUser();
        // 点赞
        followService.unfollow(user.getId(),entityId,entityType);

        return CommunityUtil.getJSONString(0,"已取消关注！");
    }
}