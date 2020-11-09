package com.zzc.springbootaop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzc
 * @since 2020-10-20
 */
@RestController
public class AopController {


    @GetMapping("/testMethod")
    public String testMethod(String id){
        System.out.println("方法执行"+id);
        return id;
    }


}
