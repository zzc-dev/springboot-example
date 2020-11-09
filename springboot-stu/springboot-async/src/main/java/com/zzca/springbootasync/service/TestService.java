package com.zzca.springbootasync.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

/**
 * @author zzc
 * @since 2020-11-09
 */
@Service
public class TestService {
    public void test() throws Exception {
        System.out.println("TestService.test");
        Thread.sleep(3000);
        System.out.println("end");
        System.out.println();
    }

    @Async("asyncTaskExecutor")
    public void test1() throws Exception {
        System.out.println(Thread.currentThread().getName());
        Thread.sleep(3000);
        System.out.println(Thread.currentThread().getName());
    }

    @Async
    public Future<String> test2()throws Exception{
        System.out.println(Thread.currentThread().getName());
        Thread.sleep(3000);
        System.out.println(Thread.currentThread().getName());
        return AsyncResult.forValue("1");
    }
}
