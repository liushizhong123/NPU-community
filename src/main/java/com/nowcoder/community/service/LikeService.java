package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import jdk.nashorn.internal.ir.EmptyNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 点赞业务
 *
 * @author lsz on 2022/1/27
 */
@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞业务,包括对人、帖子、评论点赞
     * @param userId 用户id
     * @param entityType 实体类型 :帖子/评论
     * @param entityId 实体id
     */
    public void like(int userId,int entityType,int entityId,int entityUserId){
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
//        // 判断是否已经点赞
//        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
//        if(isMember){
//            // 取消赞
//            redisTemplate.opsForSet().remove(entityLikeKey,userId);
//        }else {
//            // 点赞
//            redisTemplate.opsForSet().add(entityLikeKey,userId);
//        }
        //  redis 事务执行
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
                // 开启事务
                operations.multi();
                if(isMember){
                    // 取消赞
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else {
                    // 更新点赞集合
                    operations.opsForSet().add(entityLikeKey,userId);
                    // 用户得到的赞，即对其评论与帖子赞的总和
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    /**
     * 查询点赞数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某人对某实体的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        // 1 表示点赞, 0 表示取消赞 (用整数表示更具有扩展性)
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ?  1 : 0;
    }

    /**
     * 查询某个用户获得的点赞数量
     * @param userId
     * @return
     */
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }
}