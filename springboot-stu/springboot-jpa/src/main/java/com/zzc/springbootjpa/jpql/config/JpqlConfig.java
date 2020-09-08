package com.zzc.springbootjpa.jpql.config;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzc
 * @since 2020-09-03
 */
@Configuration
public class JpqlConfig {

    @Bean
    public VelocityEngine velocityEngine(){
        return new VelocityEngine();
    }
}
