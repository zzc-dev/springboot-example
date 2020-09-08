package com.zzc.springbootstu.runner;

import com.zzc.springbootstu.componet.TestValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author zzc
 * @since 2020-09-01
 */
@Component
public class MyRunner implements CommandLineRunner {

    @Autowired
    private TestValue testValue;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("MyRunner ="+ Arrays.toString(args));
        System.out.println(testValue);
    }
}
