package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * redis 配置类
 *
 * @author lsz on 2022/1/26
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        /* 设置key的序列化方式
         * String
         * RedisSerializer.string()返回 能够序列化为String的序列化器 */
        template.setKeySerializer(RedisSerializer.string());

        /* 设置value的序列化
         * Object*/
        template.setValueSerializer(RedisSerializer.json());

        /* 设置hash的key的序列化方式
         * String */
        template.setHashKeySerializer(RedisSerializer.string());

        /* 设置hash的value的序列化方式 */
        template.setHashValueSerializer(RedisSerializer.json());

        /* 设置生效 */
        template.afterPropertiesSet();

        return template;
    }
}