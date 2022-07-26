package com.nowcoder.community.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import javax.annotation.sql.DataSourceDefinition;

/**
 * @Author liushizhong
 * @Date 2022/4/17 16:23
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class RespBean { // 返回对象统一处理

    private long code;
    private String message;
    private Object obj;



    /**
     * 成功返回
     * @return
     */
    public static RespBean success(){
        return new RespBean(RespBeanEnum.SUCCESS.getCode(),RespBeanEnum.SUCCESS.getMessage(),null);
    }


    //重载
    public static RespBean success(Object object){
        return new RespBean(RespBeanEnum.SUCCESS.getCode(),RespBeanEnum.SUCCESS.getMessage(),object);
    }


    /**
     * 失败返回
     * @return
     */
    public static RespBean error(RespBeanEnum respBeanEnum){
        return new RespBean(respBeanEnum.getCode(),respBeanEnum.getMessage(),null);
    }


    //重载
    public static RespBean error(RespBeanEnum respBeanEnum,Object object){
        return new RespBean(respBeanEnum.getCode(),respBeanEnum.getMessage(),object);
    }

}
