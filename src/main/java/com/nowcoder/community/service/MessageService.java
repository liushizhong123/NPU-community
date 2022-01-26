package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 消息业务
 *
 * @author lsz on 2022/1/21
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询当前用户的会话列表
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    /**
     * 查询当前用户的会话数量
     * @param userId
     * @return
     */
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    /**
     * 查询某个会话所包含的私信列表
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    /**
     * 查询某个会话包含的私信数量
     * @param conversationId
     * @return
     */
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    /**
     * 查询未读私信数量
     * @param userId
     * @param conversationId
     * @return
     */
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    /**
     * 添加消息
     * @param message
     * @return
     */
    public int addMessage(Message message){
        // 过滤消息内容
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));

        return messageMapper.insertMessage(message);
    }

    /**
     * 读消息
     * @param ids
     * @return
     */
    public int readMessage(List<Integer> ids){
        // 更新消息状态为已读
        return messageMapper.updateStatus(ids,1);
    }


    /**
     * 删除消息
     * @param id 消息id
     */
    public int deleteMessage(int id) {
        // 更新消息状态为删除状态
        return messageMapper.updateStatus(Arrays.asList(new Integer[]{id}), 2);
    }
}