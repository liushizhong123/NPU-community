package com.nowcoder.community.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @Author liushizhong
 * @Date 2022/5/9 17:31
 * @Version 1.0
 */

@Service
public class TestService {

    private static final Logger logger =  LoggerFactory.getLogger(TestService.class);

//    @Async("taskExecutor")
    public void test1(){ // 多线程环境下，异步执行
        logger.debug("test1");
    }

//    @Async("taskScheduleExecutor")
//    @Scheduled(initialDelay = 10000,fixedDelay = 1000)
    public void test2(){ // 定时任务
        logger.debug("test2");
    }
}
