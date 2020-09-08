package com.zzc.springbootstu.componet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author zzc
 * @since 2020-09-01
 */
@ConfigurationProperties(prefix = "app",  ignoreUnknownFields = true)
@Profile("prod")
public class TestValue {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TestValue{" +
                "name='" + name + '\'' +
                '}';
    }
}
