package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author liushizhong
 * @Date 2022/5/9 17:16
 * @Version 1.0
 */

@Configuration
@EnableAsync
@EnableScheduling
public class ThreadPollConfig { // 线程池配置

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //设置线程池参数信息
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(15);
        taskExecutor.setQueueCapacity(200);
        taskExecutor.setKeepAliveSeconds(60);
        taskExecutor.setThreadNamePrefix("myTaskExecutor--");
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(60);
        //修改拒绝策略为使用当前线程执行
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //初始化线程池
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean("taskScheduleExecutor")
    public Executor taskScheduleExecutor() {
        ThreadPoolTaskScheduler taskScheduleExecutor = new ThreadPoolTaskScheduler();
        //设置线程池参数信息
        taskScheduleExecutor.setPoolSize(5);
        taskScheduleExecutor.setThreadNamePrefix("myTaskScheduler--");
        taskScheduleExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduleExecutor.setAwaitTerminationSeconds(60);
        //修改拒绝策略为使用当前线程执行
        taskScheduleExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //初始化线程池
        taskScheduleExecutor.initialize();
        return taskScheduleExecutor;
    }

}
