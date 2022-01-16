package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * 验证码配置
 *
 * @author lsz on 2022/1/14
 */
@Component
public class KaptchaConfig {

    // 生成验证码
    @Bean
    public Producer kaptchaProducer(){
        // 设置验证码的属性
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        // 创建验证码
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties); // 传入配置
        kaptcha.setConfig(config);

        // class DefaultKaptcha implements Producer
        return kaptcha;
    }
}