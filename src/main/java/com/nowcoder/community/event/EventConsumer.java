package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费者
 *
 * @author lsz on 2022/2/11
 */
@Component
public class EventConsumer implements CommunityConstant {
    // 日志
    private final static Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if(record == null || record.value() == null){
            logger.error("消息的内容为空！");
            return;
        }
        // 如果不为空就往下执行
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID); // 系统用户
        message.setToId(event.getEntityUserId()); // 目标用户
        message.setConversationId(event.getTopic()); // 主题
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>(16);
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        // 转换为json字符串
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
}