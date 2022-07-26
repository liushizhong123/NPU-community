package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 生产者
 *
 * @author lsz on 2022/2/11
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件
     * @param event 封装的事件
     */
   public void fireEvent(Event event){
       // 将事件发送到指定的主题
       // 消息是一个 json 字符串
       kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
   }
}