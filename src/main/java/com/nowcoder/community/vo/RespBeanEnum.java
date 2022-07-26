package com.nowcoder.community.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Author liushizhong
 * @Date 2022/4/17 16:25
 * @Version 1.0
 */
@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {

    // 登录模块
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务器异常"),

    // 接口防刷模块
    ACCESS_LIMIT(10001,"访问过于频繁，请稍后再试");

    private final long code;
    private final String message;

}
