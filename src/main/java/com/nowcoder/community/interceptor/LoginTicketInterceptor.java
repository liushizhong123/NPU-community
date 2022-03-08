package com.nowcoder.community.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LoginTicketService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 登录拦截器
 *
 * @author lsz on 2022/1/15
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private LoginTicketService loginTicketService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 在Controller之前执行
     * 在请求之前将user暂存到hostHolder
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 从cookie获取登录凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if(ticket != null){
            // 查询凭证
            LoginTicket loginTicket = loginTicketService.findLoginTicket(ticket);
            // 判断凭证是否有效
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTicket.getUserId());
                // 本次请求持有用户
                hostHolder.setUser(user);

                // 保存认证结果，并存入SecurityContext 以便Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getAuthorities(user.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
//        System.out.println("111111111");
        return true; // 允许执行方法
    }

    /**
     * 在Controller之后执行
     * 在模板引擎之前使用 user, 存入modelAndView
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
//        System.out.println("2222222222");
    }

    /**
     * 在模板引擎之后执行
     * 整个请求结束时, 清除 user.
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        hostHolder.clear();
//        System.out.println("33333333333333");
          // 清理 SecurityContextHolder
//        SecurityContextHolder.clearContext();
    }
}