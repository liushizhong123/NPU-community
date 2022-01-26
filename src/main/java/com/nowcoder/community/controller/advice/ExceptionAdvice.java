package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 统一异常处理类
 *
 * @author lsz on 2022/1/22
 */
@ControllerAdvice(annotations = Controller.class) // 声明作用范围
public class ExceptionAdvice {

    /**
     * 记录日志
     */
    public static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * 异常处理方法
     * @param e
     * @param request
     * @param response
     */
    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常: " + e.getMessage());
        // 遍历打印详细的信息
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }
        // 通过请求获取请求方式:是返回页面还是json
        String xRequestedWith = request.getHeader("x-requested-with");
        String type = "XMLHttpRequest"; // 如果 xRequestedWith 为 XMLHttpRequest 则为 Ajax 异步HTTP请求
        if (type.equals(xRequestedWith)) {
            // text/plain的意思是将文件设置为纯文本的形式，浏览器在获取到这种文件时并不会对其进行处理
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            // 将返回字符串转换为 json 字符串，由前端解析
            writer.write(CommunityUtil.getJSONString(1, "服务器异常！"));
        } else {
            // 普通请求，返回错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}