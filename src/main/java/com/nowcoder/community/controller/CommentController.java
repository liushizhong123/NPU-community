package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * 评论逻辑
 *
 * @author lsz on 2022/1/20
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 添加帖子评论
     * @param discussPostId
     * @param comment
     * @return
     */
    @PostMapping(value = "/add/{discussPostId}")
    @LoginRequired
    public String addComment(@PathVariable("discussPostId") int discussPostId,
                             Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        // 添加帖子评论
        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT) // 评论主题
                .setUserId(hostHolder.getUser().getId()) // 触发评论的用户
                .setEntityId(comment.getEntityId()) // 评论目标的id
                .setEntityType(comment.getEntityType()) // 评论目标的类型
                .setData("posId",discussPostId); // 帖子id,便于查询帖子详情
        if(comment.getEntityType() == ENTITY_TYPE_POST){// 查询帖子信息
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){ // 查询评论信息
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        // 发布事件
        eventProducer.fireEvent(event);

        // 跳转到帖子详情页
        return "redirect:/discuss/detail/" + discussPostId;
    }
}