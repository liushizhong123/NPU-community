package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.service.CommentService;
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
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

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
        // 跳转到帖子详情页
        return "redirect:/discuss/detail/" + discussPostId;
    }
}