package com.zzc.springbootstu.eventListener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author zzc
 * @since 2020-09-01
 * 容器监听事件：
 *    ApplicationStartingEvent
 *    ApplicationEnvironmentPreparedEvent
 *    ApplicationContextInitializedEvent
 *    ApplicationPreparedEvent
 *    ContextRefreshedEvent
 *    ApplicationStartedEvent
 *    ApplicationReadyEvent
 *    ContextClosedEvent
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HelloWorldApplicationListener implements ApplicationListener<ApplicationEvent>{
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println("helloWorld first" + event.getClass().getName());
    }
}
