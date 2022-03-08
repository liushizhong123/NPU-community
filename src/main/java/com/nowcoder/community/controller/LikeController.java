package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * @param entityType
     * @param entityId
     * @return
     */
    @LoginRequired
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
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

        // 触发点赞事件：点赞则触发，取消赞不触发
        if(likeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            //发送消息
            eventProducer.fireEvent(event);
        }

        // 点赞的是帖子
        if(entityType == ENTITY_TYPE_POST){
            // 将帖子id存入redis,以便之后计算帖子的分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }

        return CommunityUtil.getJSONString(0,null,map);

    }
}