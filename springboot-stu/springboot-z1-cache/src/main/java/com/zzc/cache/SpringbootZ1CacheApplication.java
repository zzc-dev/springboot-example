package com.zzc.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringbootZ1CacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootZ1CacheApplication.class, args);
    }

}
