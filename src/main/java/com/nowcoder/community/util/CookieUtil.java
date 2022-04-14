package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * cookie工具类
 *
 * @author lsz on 2022/1/14
 */
public class CookieUtil {
    // 获得指定name的cookie值，静态方法，直接通过类来调用
    public static String getValue(HttpServletRequest request, String name) {
        // 判空处理
        if (request == null || name == null) {
            throw new IllegalArgumentException("参数为空！");
        }
        // 得到 cookie 数组
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}