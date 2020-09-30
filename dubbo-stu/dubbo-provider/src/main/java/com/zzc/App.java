package com.zzc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author zzc
 * @since 2020-09-29
 */
public class App {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF.spring/provider.xml");
        context.start();
        System.in.read();
    }
}
