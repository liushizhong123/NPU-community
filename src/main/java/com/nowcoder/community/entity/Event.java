package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件
 *
 * @author lsz on 2022/2/11
 */
public class Event {

    private String topic; // 主题
    private int userId; // 事件触发者
    private int entityType; // 目标实体类型
    private int entityId; // 目标实体 id
    private int entityUserId; // 目标实体用户
    private Map<String,Object> data = new HashMap<>(); // 目标实体相关其他信息

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key,Object value) {
        this.data.put(key,value);
        return this;
    }
}