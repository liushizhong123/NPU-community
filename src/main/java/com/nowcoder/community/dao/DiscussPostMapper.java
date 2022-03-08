package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    /**
     * 分页获取帖子
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderMode);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 插入帖子数据
     * @param discussPost
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 查询帖子详情
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 更新帖子评论数量
     * @param id 帖子 id
     * @param commentCount 帖子评论数量
     * @return
     */
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    /**
     * 更新帖子类型
     * @param id
     * @param type
     * @return
     */
    int updateType(@Param("id") int id, @Param("type") int type);

    /**
     * 更新帖子状态
     * @param id
     * @param status
     * @return
     */
    int updateStatus(@Param("id") int id, @Param("status") int status);


    /**
     * 更新帖子分数
     * @param id
     * @param score
     * @return
     */
    int updateScore(@Param("id") Integer id, @Param("score") double score);
}
