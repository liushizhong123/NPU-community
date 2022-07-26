package com.nowcoder.community.annotation;

import java.lang.annotation.*;

/**
 * @Author liushizhong
 * @Date 2022/4/17 15:24
 * @Version 1.0
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {

    int second();
    int maxCount();
    boolean needLogin() default false;
}
