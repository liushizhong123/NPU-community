package com.nowcoder.community.interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DataService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author liushizhong
 * @Date 2022/3/4 21:46
 * @Version 1.0
 */
@Component
public class DataInterceptor implements HandlerInterceptor { // 统计UV与DAU拦截器

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计UV
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);
        // 统计DAU
        User user = hostHolder.getUser();
        if(user!= null){ // 用户必须登入才算
            dataService.recordDAU(user.getId());
        }
        // 放开执行
        return true;
    }
}
