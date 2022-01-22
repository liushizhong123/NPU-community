package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * 登录注册工具类
 *
 * @author lsz on 2022/1/13
 */
public class CommunityUtil {

    // 随机生成字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // MD5 加密
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 转换json字符串
     * @param code
     * @param msg
     * @param map
     * @return
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject(); // 可以认为是一个 map
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        // 转换为Json格式的字符串
        return json.toJSONString();
    }

    // 重载
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    // 重载
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

}