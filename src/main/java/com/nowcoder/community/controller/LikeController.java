package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞业务逻辑
 *
 * @author lsz on 2022/1/27
 */
@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    /**
     * 点赞
     * @param entityType
     * @param entityId
     * @return
     */
    @LoginRequired
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId){
        // 获取当前用户
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回结果
        Map<String ,Object> map = new HashMap<>(16);
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        return CommunityUtil.getJSONString(0,null,map);

    }
}