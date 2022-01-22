package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识该方法是否需要在登录的情况下才能进行访问
 */
@Target(ElementType.METHOD) // 定义 Annotation 能够被应用于源码的哪些位置,这里是方法上
@Retention(RetentionPolicy.RUNTIME) // 定义了 Annotation 的生命周期 ，这里是运行时
public @interface LoginRequired {
}
