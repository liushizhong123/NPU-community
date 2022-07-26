package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.RedisKeyUtil;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void init(){
        // 初始化所有的帖子
        String key = RedisKeyUtil.getHOTKey();
        List<DiscussPost> discussPostList = discussPostMapper.selectAllDiscussPosts();
        // 装入redis
        if(discussPostList != null){
            for(DiscussPost post : discussPostList){
                redisTemplate.opsForZSet().add(key,post,post.getScore());
            }
        }
    }

    /**
     * 分页获取帖子
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode) {
        // 从 redis 获取
        String key = RedisKeyUtil.getHOTKey();
        if(userId == 0 && orderMode == 1){ // 查询热帖
            Set<DiscussPost> discussPostSet = redisTemplate.opsForZSet().reverseRange(key, offset, limit + offset - 1);
            if(discussPostSet != null){
                List<DiscussPost> discussPostList = new LinkedList<>();
                for(DiscussPost post : discussPostSet){
                    discussPostList.add(post);
                }
                return discussPostList;
            }
            throw new IllegalArgumentException("参数不合法！");
        }
        // 这里的 orderMode  用于热帖排行榜,为 0 时最新帖子， 为 1 时是最热帖子，两种帖子排序方式不同
        return discussPostMapper.selectDiscussPosts(userId, offset, limit,orderMode);
    }

    /**
     * 帖子列表
     * @param userId
     * @return
     */
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 添加帖子
     * 转义html标记 过滤敏感词
     * @param post 帖子实例
     * @return
     */
    public int addDiscussPost(DiscussPost post) {
        // 判空处理
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // HtmlUtils.htmlEscape(): 转义HTML标记
        // 将标题与内容中的html标签转义
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        // 插入帖子
        return discussPostMapper.insertDiscussPost(post);
    }

    /**
     * 通过 id 查询帖子详情
     * @param id
     * @return
     */
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 更新帖子评论数量
     * @param id 帖子 id
     * @param commentCount 评论数量
     * @return
     */
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    /**
     * 更新帖子类型
     * @param id
     * @param type
     * @return
     */
    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }

    /**
     * 更新帖子状态
     * @param id
     * @param status
     * @return
     */
    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id,status);
    }


    /**
     * 更新帖子分数
     * @param id
     * @param score
     */
    public int updateScore(Integer id, double score) {
        return discussPostMapper.updateScore(id,score);
    }
}
