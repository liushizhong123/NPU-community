package com.nowcoder.community.config;

import com.nowcoder.community.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置
 *
 * @author lsz on 2022/1/15
 */
@Component
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor; // 登录用户信息拦截器

//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private AccessLimitInterceptor accessLimitInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 对于所有页面-显示登录信息
        registry.addInterceptor(loginTicketInterceptor) //注入登录信息拦截器
                .excludePathPatterns("/*/*.css", "/*/*.js", "/*/*.png", "/*/*.jpg", "/*/*.jpeg"); // 排除静态资源

        registry.addInterceptor(accessLimitInterceptor) // 接口防刷拦截器
                .excludePathPatterns("/*/*.css", "/*/*.js", "/*/*.png", "/*/*.jpg", "/*/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor) //注入登录状态拦截器,（不在使用）由spring security 负责
//                .excludePathPatterns("/*/*.css", "/*/*.js", "/*/*.png", "/*/*.jpg", "/*/*.jpeg"); // 排除静态资源

        registry.addInterceptor(messageInterceptor) //注入未读消息拦截器
                .excludePathPatterns("/*/*.css", "/*/*.js", "/*/*.png", "/*/*.jpg", "/*/*.jpeg"); // 排除静态资源

        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/*/*.css", "/*/*.js", "/*/*.png", "/*/*.jpg", "/*/*.jpeg");
    }
}