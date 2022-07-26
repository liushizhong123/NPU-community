package com.nowcoder.community;

import com.nowcoder.community.service.TestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author liushizhong
 * @Date 2022/5/9 16:09
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadTest {

    @Autowired
    private TestService testService;

    private final static Logger logger = LoggerFactory.getLogger(ThreadTest.class);

    @Test
    public void testThreadPoolTaskExecutorSimple(){
        for (int i = 0; i < 10; i++) {
            testService.test1();
        }
        sleep(10000);
    }

    @Test
    public void testThreadPoolTaskScheduleSimple(){
        sleep(30000);
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
//    @Test
//    public void test1(){
//        ThreadPoolExecutor executor = new ThreadPoolExecutor();
//    }
}
