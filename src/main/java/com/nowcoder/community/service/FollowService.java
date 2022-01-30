package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 关注业务
 *
 * @author lsz on 2022/1/29
 */
@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 关注
     * @param userId
     * @param entityId
     * @param entityType
     */
    public void follow(int userId,int entityId,int entityType){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerkey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                // 开启事务
                operations.multi();
                operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                operations.opsForZSet().add(followerkey,userId,System.currentTimeMillis());
                // 执行事务
                return operations.exec();
            }
        });
    }

    /**
     * 取消关注
     * @param userId
     * @param entityId
     * @param entityType
     */
    public void unfollow(int userId,int entityId,int entityType){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerkey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                // 开启事务
                operations.multi();
                operations.opsForZSet().remove(followeeKey,entityId);
                operations.opsForZSet().remove(followerkey,userId);
                // 执行事务
                return operations.exec();
            }
        });
    }

    /**
     * 查询关注的实体的数量
     * @param userId
     * @param entityType
     * @return
     */
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        // zCard -> set 的长度
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询实体的粉丝的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        // zCard -> set 的长度
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询当前用户是否已关注该实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        // score -> 权重
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }
}