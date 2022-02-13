package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    /**
     * 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
     *
     * @param userId
     * @param offset
     * @param limit
     * @return 一条最新的私信
     */
    List<Message> selectConversations(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询当前用户的会话数量
     *
     * @param userId
     * @return 当前用户的会话数量
     */
    int selectConversationCount(@Param("userId") int userId);

    /**
     * 查询某个会话所包含的私信列表
     *
     * @param conversationId
     * @param offset
     * @param limit
     * @return 某个会话所包含的私信列表
     */
    List<Message> selectLetters(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询某个会话包含的私信数量
     *
     * @param conversationId
     * @return 某个会话包含的私信数量
     */
    int selectLetterCount(@Param("conversationId") String conversationId);

    /**
     * 查询未读私信数量
     *
     * @param userId
     * @param conversationId
     * @return 未读私信数量
     */
    int selectLetterUnreadCount(@Param("userId") int userId, @Param("conversationId") String conversationId);

    /**
     * 增加消息
     * @param message
     * @return
     */
    int insertMessage(Message message);

    /**
     * 更新消息状态
     * @param ids
     * @param status
     * @return
     */
    int updateStatus(List<Integer> ids,@Param("status") int status);

    /**
     * 查询某个主题下最新的通知
     */
    Message selectLatestNotice(@Param("userId") int userId, @Param("topic") String topic);

    /**
     * 查询某个主题下的所包含的通知的数量
     */
    int selectNoticeCount(@Param("userId") int userId, @Param("topic") String topic);

    /**
     * 查询某个主题下未读的消息的数量
     */
    int selectNoticeUnreadCount(@Param("userId") int userId, @Param("topic") String topic);

    /**
     * 分页查询某个主题下的所有通知
     */
    List<Message> selectNotices(@Param("userId") int userId, @Param("topic") String topic, @Param("offset") int offset, @Param("limit") int limit);
}
