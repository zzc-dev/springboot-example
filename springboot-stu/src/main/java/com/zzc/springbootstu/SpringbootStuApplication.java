package com.zzc.springbootstu;

import com.zzc.springbootstu.componet.TestValue;
import com.zzc.springbootstu.eventListener.HelloWorldApplicationListener;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Arrays;

@SpringBootApplication
@EnableConfigurationProperties(TestValue.class)
public class SpringbootStuApplication {

    public static void main(String[] args) {
//        SpringApplication.run(SpringbootStuApplication.class, args);
        SpringApplication springApplication = new SpringApplication(SpringbootStuApplication.class);
        springApplication.setBannerMode(Banner.Mode.OFF); // 关闭横幅
        // 添加事件监听器
//        springApplication.addListeners(new HelloWorldApplicationListener());
        System.out.println(Arrays.toString(args));
        springApplication.run(args);
    }
}
