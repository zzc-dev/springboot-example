package com.zzc.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.zzc.security")
public class SpringbootZ2SecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootZ2SecurityApplication.class, args);
    }

}
