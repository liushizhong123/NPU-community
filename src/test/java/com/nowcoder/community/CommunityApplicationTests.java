package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
class CommunityApplicationTests {

    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void testMailSend() {
        mailClient.sendMail("1013552449@qq.com", "这是测试SpringBoot发普通内容邮件的功能", "你好帅!!!");
    }

    @Test
    public void testHtmlMail(){ // 尝试发送HTML邮件
        Context context = new Context(); // thymeleaf 的 Context
        context.setVariable("username", "Jame Liu");
        String content = templateEngine.process("/mail/demo", context); // 生成动态网页
        System.out.println(content);
        mailClient.sendMail("1013552449@qq.com", "这是测试SpringBoot发HTML邮件的功能", content);
    }

}
