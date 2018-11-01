package com.example.redislock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisCheckLockTest {
    private static Logger logger = LoggerFactory.getLogger(RedisCheckLock.class);
    @Autowired
    RedisCheckLock redisCheckLock;

    @Test
    public void test() {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        // 模拟100个线程并发获取锁
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                doBussiness(Thread.currentThread().getName());
            });
        }
        // main thread block here
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doBussiness(String requestId) {
        String key = "REDIS:CHECK:LOCK:test";
        int expires = 10000;
        while (true) {
            Boolean aBoolean = redisCheckLock.tryLock(key, requestId, expires);
            if (aBoolean) {
                break;
            } else {
                // 尝试重新获取锁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        logger.info("Thead[{}]获得锁,key:{}", requestId, key);

        // 模拟处理业务时间
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            logger.info("Thead[{}]释放锁,key:{}", requestId, key);
            redisCheckLock.releaseLock(key, requestId);
        }

    }
}