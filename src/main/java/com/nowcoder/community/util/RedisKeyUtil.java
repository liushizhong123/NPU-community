package com.nowcoder.community.util;

import org.springframework.stereotype.Component;

/**
 * redis 工具类
 *
 * @author lsz on 2022/1/26
 */
public class RedisKeyUtil {
    private static final String SPLIT = ":";

    /**
     * 点赞实体前缀
     */
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    /**
     * 点赞用户前缀
     */
    private static final String PREFIX_USER_LIKE = "like:user";

    /**
     * 关注目标前缀，便于统计关注了谁
     */
    private static final String PREFIX_FOLLOWEE = "followee";

    /**
     * 关注者前缀，便于统计粉丝数
     */
    private static final String PREFIX_FOLLOWER = "follower";

    /* 验证码 */
    private static final String PREFIX_KAPTCHA = "kaptcha";
    /*  忘记密码验证码 */
    private static final String PREFIX_Code = "forgetCode";

    /* 凭证 */
    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";

    private static final String PREFIX_UV = "uv";

    private static final String PREFIX_DAU = "dau";

    private static final String PREFIX_POST = "post";

    /**
     * 某个实体的赞
     * 实体: 包括帖子和评论
     * 前缀以 like:entity 开头
     * 举例如下:
     * like:entity:entityType:entityId -> set(userId)
     *
     * */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户的赞 like:user:userId -> int
     *
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体 followee:userId:entityType -> zset(entityId, now)
     *
     * @param userId
     * @param entityType
     * @return
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体拥有的粉丝 follower:entityType:entityId -> zset(userId, now)
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 登录验证码
     *
     * @param owner 服务器发给客户端的验证码
     * @return
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 登录验证码
     *
     * @param verifyCode 服务器发给客户端的验证码
     * @return
     */
    public static String getForgetCodeKey(String verifyCode) {
        return PREFIX_Code + SPLIT + verifyCode;
    }

    /**
     * 登录的凭证
     *
     * @param ticket
     * @return
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 用户
     *
     * @param userId
     * @return
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * 单日UV
     *
     * @param date
     * @return
     */
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * 区间UV
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 单日DAU
     *
     * @param date
     * @return
     */
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * 区间DAU
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 帖子分数
     *
     * @return
     */
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

}