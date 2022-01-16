package com.nowcoder.community.config;

import com.nowcoder.community.interceptor.LoginRequiredInterceptor;
import com.nowcoder.community.interceptor.LoginTicketInterceptor;
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
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 对于所有页面-显示登录信息
        registry.addInterceptor(loginTicketInterceptor) //注入登录信息拦截器
                .excludePathPatterns("/*/*.css", "/*/*.js", "/*/*.png", "/*/*.jpg", "/*/*.jpeg"); //排除静态资源

        registry.addInterceptor(loginRequiredInterceptor) //注入登录状态拦截器
                .excludePathPatterns("/*/*.css", "/*/*.js", "/*/*.png", "/*/*.jpg", "/*/*.jpeg"); //排除静态资源

    }
}