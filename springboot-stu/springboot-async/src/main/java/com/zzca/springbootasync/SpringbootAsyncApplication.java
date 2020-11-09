package com.zzca.springbootasync;

import com.zzca.springbootasync.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Future;

@SpringBootApplication
@EnableAsync
public class SpringbootAsyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootAsyncApplication.class, args);
    }

    @Autowired
    private TestService testService;

    @Bean
    public ApplicationRunner applicationRunner(){
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
                System.out.println("1."+Thread.currentThread().getName());
                Future<String> stringFuture = testService.test2();
                System.out.println("2."+Thread.currentThread().getName());
                String s = stringFuture.get();
                System.out.println("s="+s);
                System.out.println("3."+Thread.currentThread().getName());
            }
        };
    }

}
