package com.nowcoder.community.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nowcoder.community.annotation.AccessLimit;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import com.nowcoder.community.vo.RespBean;
import com.nowcoder.community.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @Author liushizhong
 * @Date 2022/4/17 15:31
 * @Version 1.0
 */


/**
 * 接口防刷拦截器,5秒内最多访问5次
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DefaultRedisScript script;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            HandlerMethod method = (HandlerMethod) handler;
            AccessLimit accessLimit = method.getMethodAnnotation(AccessLimit.class);
            if(accessLimit == null){
                return true;
            }

            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            if(needLogin){ // 需要登录才能访问
                if(hostHolder.getUser() == null){
                    // 用户未登录，转到登录页
                    response.sendRedirect(request.getContextPath() + "/login");
                    return false; // 拦截，不让继续执行方法
                }
            }

            // redis 分布式锁实现接口限流
            ValueOperations valueOperations = redisTemplate.opsForValue();
            // 当前线程标识
            String uid = CommunityUtil.generateUUID();
            Boolean isLock = valueOperations.setIfAbsent("limit",uid,30,TimeUnit.SECONDS);
            if(isLock){
                try {
                    String redisKey = RedisKeyUtil.getRepeatNumKey(request.getRequestURI(), request.getRemoteHost());
                    Integer count = (Integer) valueOperations.get(redisKey);
                    if(count == null){ // 第一次点击
                        valueOperations.set(redisKey, 1, second, TimeUnit.SECONDS);
                    }else if (count < maxCount){ // 继续点击
                        valueOperations.increment(redisKey);
                    }else{ // 点击次数超限
                        render(response, RespBeanEnum.ACCESS_LIMIT);
                        return false;
                    }
                } finally {
                    // 使用lua脚本确保删除自己的锁
                    redisTemplate.execute(script, Collections.singletonList("limit"),uid);
                }
            }
        }
        return true;
    }

    /**
     * 构建返回对象
     * @param response
     * @param respBeanEnum
     * @throws IOException
     */
    private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        // 创建失败返回对象
        RespBean error = RespBean.error(respBeanEnum);
        // 输出
        out.write(new ObjectMapper().writeValueAsString(error));
        out.flush();
        out.close();
    }
}
