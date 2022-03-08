package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.ibatis.javassist.Loader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

/**
 * @Author liushizhong
 * @Date 2022/3/4 20:40
 * @Version 1.0
 */
@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    //将指定的ip计入UV
    public void recordUV(String ip){
        // 构造 key
        String redisKey = RedisKeyUtil.getUVKey(dateFormat.format(new Date()));
        // 加入redis
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    // 统计指定日期范围内的UV
    public long calculateUV(Date start,Date end){
        if(start == null || end == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(start.after(end)){
            throw new IllegalArgumentException("请输入正确的时间段！");
        }
        // 整理该日期范围内的Key
        List<String> keyList = new ArrayList<>();
        // 日期计算
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String uvKey = RedisKeyUtil.getUVKey(dateFormat.format(calendar.getTime()));
            keyList.add(uvKey);
            // 日期加一天
            calendar.add(Calendar.DATE,1);

        }
        // 合并这些数据
        String redisKey =  RedisKeyUtil.getUVKey(dateFormat.format(start),dateFormat.format(end));
        // 第一个参数为目标key 第二个参数为合并key的集合数组
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray(new String[0]));

        //返回统计结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    //将指定的用户计入DAU
    public void recordDAU(int userId){
        // 构造 key
        String redisKey = RedisKeyUtil.getDAUKey(dateFormat.format(new Date()));
        // 加入redis
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }

    // 统计指定日期范围内的DAU
    public long calculateDAU(Date start,Date end){
        if(start == null || end == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(start.after(end)){
            throw new IllegalArgumentException("请输入正确的时间段！");
        }
        // 整理该日期范围内的Key
        List<byte[]> keyList = new ArrayList<>();
        // 日期计算
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUKey(dateFormat.format(calendar.getTime()));
            keyList.add(key.getBytes());
            // 日期加一天
            calendar.add(Calendar.DATE,1);

        }
        // 进行OR运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 构造结果的key
                String redisKey = RedisKeyUtil.getDAUKey(dateFormat.format(start),dateFormat.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }

}
