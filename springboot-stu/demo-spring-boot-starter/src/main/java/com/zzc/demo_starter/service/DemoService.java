package com.zzc.demo_starter.service;

import com.zzc.demo_starter.properties.DemoProperties;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zzc
 * @since 2020-09-10
 */
public class DemoService {
    @Autowired
    DemoProperties demoProperties;

    public String test(String name){
        System.out.println(demoProperties);
        return name;
    }
}
