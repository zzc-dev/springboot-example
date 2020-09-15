package com.zzc.cache.controller;

import com.zzc.cache.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzc
 * @since 2020-09-10
 */
@RestController
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping("/test")
    public String test(){
        return service.test(1);
    }

    @GetMapping("/test1")
    public String test1(){
        return service.test1();
    }
}
