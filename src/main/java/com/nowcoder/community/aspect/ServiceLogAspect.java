package com.nowcoder.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志业务组件
 *
 * @author lsz on 2022/1/23
 */
@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    /**
     * 切点
     */
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut() {

    }

    /**
     * 前置通知
     * @param joinPoint 连接点
     */
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 用户IP[1.2.3.4], 在[xxxx], 访问了[com.nowcoder.community.service.xxx()].
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest(); // 先得到request
        String ip = request.getRemoteHost(); // 然后再得到ip地址
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target =
                joinPoint.getSignature().getDeclaringTypeName() // 得到类名
                        + "."
                        + joinPoint.getSignature().getName(); // 得到方法名
        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }

}