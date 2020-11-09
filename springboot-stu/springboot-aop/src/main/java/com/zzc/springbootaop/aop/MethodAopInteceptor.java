package com.zzc.springbootaop.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author zzc
 * @since 2020-10-20
 */
@Component
@Aspect
public class MethodAopInteceptor {

    @Pointcut("execution(public * com.zzc.springbootaop.controller.AopController.*(..))")
    public void cut(){}

    @Before("cut()")
    public void before(){
        System.out.println("aop before");
    }
}
