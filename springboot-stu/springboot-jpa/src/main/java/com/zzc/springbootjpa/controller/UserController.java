package com.zzc.springbootjpa.controller;

import com.zzc.springbootjpa.dao.UserDao;
import com.zzc.springbootjpa.data.entity.User;
import com.zzc.springbootjpa.jpql.JpqlQuerySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
    public String testVersion(String id){
        User user = userDao.getById(1);
        user.setPassword(id);
        if("1".equals(id)){
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        userDao.save(user);
       return user.toString();
    }

    @Autowired
    private JpqlQuerySupport jpqlQuerySupport;

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
