package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 分页查询评论
     * @param entityType 目标类型
     * @param entityId 目标id
     * @param offset  位置偏移量
     * @param limit 每页记录数
     * @return
     */
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId,
                                        @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询评论数量
     * @param entityType
     * @param entityId
     * @return
     */
    int selectCountByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    /**
     * 添加帖子评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);

    /**
     * 查询用户的评论数量
     * @param userId
     * @return
     */
    int selectCountByUser(int userId);

    /**
     * 查询用户的评论集
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> selectCommentsByUser(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);
}
