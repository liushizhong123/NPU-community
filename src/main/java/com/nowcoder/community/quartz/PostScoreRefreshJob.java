package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author liushizhong
 * @Date 2022/3/6 15:08
 * @Version 1.0
 *  定时任务，刷新帖子分数
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

    // 记录日志
    private static final  Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    private static final Date epoch;

    // 静态初始化，整个运行过程中只初始化一次
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化NPU论坛纪元时间失败！");
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        // 定义集合操作
        BoundSetOperations boundSetOperations = redisTemplate.boundSetOps(redisKey);

        if(boundSetOperations.size() == 0){
            // 没有帖子需要刷新
            logger.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数：" + boundSetOperations.size());
        // 遍历集合执行任务
        while(boundSetOperations.size() > 0){
            // 每次更新一个帖子都是pop出来，防止重复计算，同时节省内存
            this.refresh((Integer)boundSetOperations.pop());
        }

        logger.info("[任务结束] 帖子分数刷新完毕!");

    }

    // 刷新帖子分数
    private void refresh(Integer postId) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(postId);

        if(discussPost == null){
            logger.error("该帖子不存在： id = " + postId);
            return;
        }

        if(discussPost.getStatus() == 2){
            logger.error("帖子已被删除");
            return;
        }

        //是否加精
        boolean wonderful = discussPost.getStatus() == 1;
        // 评论数量
        int commentCount = discussPost.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w,1))
                + (discussPost.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId,score);
        // 更新缓存
        String key = RedisKeyUtil.getHOTKey();
        // 先删除
        redisTemplate.opsForZSet().remove(key,discussPost);
        redisTemplate.opsForZSet().add(key,discussPost,score);
    }
}
