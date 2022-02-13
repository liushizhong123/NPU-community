package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.SensitiveFilter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
class CommunityApplicationTests {

    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Producer producer;

    @Test
    void testMailSend() {
        mailClient.sendMail("1013552449@qq.com", "这是测试SpringBoot发普通内容邮件的功能", "你好帅!!!");
    }

    @Test
    public void testHtmlMail() { // 尝试发送HTML邮件
        Context context = new Context(); // thymeleaf 的 Context
        context.setVariable("username", "Jame Liu");
        String content = templateEngine.process("/mail/demo", context); // 生成动态网页
        System.out.println(content);
        mailClient.sendMail("1013552449@qq.com", "这是测试SpringBoot发HTML邮件的功能", content);
    }

    @Test
    public void testSensitiveFilter() {
        String text = "这里可以赌博，把那个开票，嫖娼，哈哈哈！！！";
        String text1 = "这里￥￥￥￥可以&&赌&&博&&，把****那个…………开%%房，---嫖###娼--，哈哈哈！！！";
        text = sensitiveFilter.filter(text);
        text1 = sensitiveFilter.filter(text1);
        System.out.println(text);
        System.out.println(text1);
    }

    @Test
    public void redisTest() {
        String key = "test:count";

        redisTemplate.opsForValue().set(key, 1);
        Object o = redisTemplate.opsForValue().get(key);
        Object o1 = redisTemplate.opsForValue().get("111");
        System.out.println(o1);
        Integer res = (Integer) o1;
        System.out.println(res);


//        String key1 = "test:set";
//        redisTemplate.opsForSet().add(key1,222);
//        Long size = redisTemplate.opsForSet().size(key);
//        long count = size;
//        System.out.println(size);
//        System.out.println(count);

    }

    @Test
    public void testKafka(){
        producer.sendMessage("test","hello");
        producer.sendMessage("test","你好");

        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

@Component
class Producer{

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic,String content){
        kafkaTemplate.send(topic,content);
    }
}
@Component
class Consumer{

    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }
}
