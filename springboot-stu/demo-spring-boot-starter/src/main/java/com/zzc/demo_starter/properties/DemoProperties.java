package com.zzc.demo_starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zzc
 * @since 2020-09-10
 */
@ConfigurationProperties(prefix = "demo.zcc")
public class DemoProperties {
    private String name;
    private int sex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "DemoProperties{" +
                "name='" + name + '\'' +
                ", sex=" + sex +
                '}';
    }
}
