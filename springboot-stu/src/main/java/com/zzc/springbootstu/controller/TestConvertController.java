package com.zzc.springbootstu.controller;

import com.zzc.springbootstu.po.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzc
 * @since 2020-09-02
 */
@Controller
@RequestMapping("/convert")
public class TestConvertController {

    @GetMapping("/error")
    @ResponseBody
    public User test(Integer id) throws Exception {
        try {
            if(id == 1){
                return new User();
            }
            throw new Exception("11");
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("11");
        }
    }

    @PostMapping("/test1")
    @ResponseBody
    public Object test1(){
        List<User> users = new ArrayList<>();
        users.add(new User());
        return users;
    }

    @PostMapping("/test2")
    @ResponseBody
    public Object test2(@RequestBody User user){
        System.out.println(user);
        List<User> users = new ArrayList<>();
        users.add(new User());
        return users;
    }
}
