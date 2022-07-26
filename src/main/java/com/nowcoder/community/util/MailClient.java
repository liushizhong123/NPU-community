package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 发送邮件工具类
 *
 * @author lsz on 2022/1/13
 */
@Component
public class MailClient {
    // 日志
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}") // 从配置文件导入用户名
    private String from;

    /**
     * 发送邮件
     * @param to 接收者
     * @param subject 主题
     * @param content 内容
     */
    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from); // 发件人
            helper.setTo(to); // 发送给谁
            helper.setSubject(subject); // 发送的主题
            helper.setText(content, true); // 允许发送html字符串
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error("发送邮件失败：" + e.getMessage());
        }
    }
}