package com.zzc.demo_starter.config;

import com.zzc.demo_starter.properties.DemoProperties;
import com.zzc.demo_starter.service.DemoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzc
 * @since 2020-09-10
 */
@Configuration
@EnableConfigurationProperties(DemoProperties.class)
@ConditionalOnProperty(prefix = "demo",name = "isOpen", havingValue = "true")
public class DemoAutoConfiguration {

    @Bean
    public DemoService demoService(){
        return new DemoService();
    }
}
