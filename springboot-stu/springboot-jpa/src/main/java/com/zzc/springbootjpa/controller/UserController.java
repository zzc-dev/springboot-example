package com.zzc.springbootjpa.controller;

import com.zzc.springbootjpa.dao.UserDao;
import com.zzc.springbootjpa.data.entity.User;
import com.zzc.springbootjpa.jpql.JpqlQuerySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zzc
 * @since 2020-09-03
 */
@RestController
public class UserController {

    @Autowired
    private UserDao userDao;

    @GetMapping("/test")
    public String test(){
        List<User> all = userDao.findAll();
        System.out.println(all.size());
        User byId = userDao.getById(1);
        System.out.println(byId);
        return "";
    }

    @GetMapping("/test1")
    @Transactional
    public String testVersion(){
        User byId = userDao.getById(1);
        byId.setPassword("123456");
        User save = userDao.save(byId);
        System.out.println(save);
        return "";
    }

    @Autowired
    private JpqlQuerySupport jpqlQuerySupport;

    @Autowired
    @GetMapping("/test2")
    public String testJpql(){
        HashMap hashMap = new HashMap();
        Set<String > set = new HashSet<>();
        set.add("1");
        set.add("2");
        hashMap.put("devGbIds",set);
        jpqlQuerySupport.findAll("user.findSubTaskCount",hashMap);
        return "";
    }
}
