package com.zzc.mybatis.springbootmybaits;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude ={DataSourceAutoConfiguration.class})
//@MapperScan("com.zzc.mybatis.springbootmybaits.mapper")
public class SpringbootMybaitsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootMybaitsApplication.class, args);
    }

}
