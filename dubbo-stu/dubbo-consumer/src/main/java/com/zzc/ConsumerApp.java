package com.zzc;

import com.zzc.service.ProviderService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author zzc
 * @since 2020-09-29
 */
public class ConsumerApp {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("consumer.xml");
        context.start();
        ProviderService providerService = (ProviderService) context.getBean("providerService");
        System.out.println(providerService.sayHello("hoo"));
    }
}
