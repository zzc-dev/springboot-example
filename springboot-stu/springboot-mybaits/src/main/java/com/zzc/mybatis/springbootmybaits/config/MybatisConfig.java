package com.zzc.mybatis.springbootmybaits.config;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzc
 * @since 2020-09-09
 */
@Configuration
public class MybatisConfig {

    @Bean
    public ConfigurationCustomizer configurationCustomizer(){
        return new ConfigurationCustomizer() {
            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                // 开启表中带_的字段映射成驼峰命名的java bean
                configuration.setMapUnderscoreToCamelCase(true);
            }
        };
    }
}
