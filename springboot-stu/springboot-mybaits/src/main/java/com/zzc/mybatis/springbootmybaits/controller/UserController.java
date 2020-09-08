package com.zzc.mybatis.springbootmybaits.controller;

import com.zzc.mybatis.springbootmybaits.entity.Area;
import com.zzc.mybatis.springbootmybaits.entity.Store;
import com.zzc.mybatis.springbootmybaits.entity.User;
import com.zzc.mybatis.springbootmybaits.mapper.UserMapper;
import com.zzc.mybatis.springbootmybaits.mapper.test.StoreMapper;
import com.zzc.mybatis.springbootmybaits.mapper.testTwo.AreaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzc
 * @since 2020-09-02
 */
@RestController
public class UserController {

//    @Autowired
    private UserMapper userMapper;

//    @Autowired
    @GetMapping("/get")
    public User user(){
        User sel = userMapper.Sel(1);
        System.out.println(sel);
        return sel;
    }

    @Autowired
    private AreaMapper areaMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    @GetMapping("/get1")
    public String test(){
        Area area = areaMapper.sel(1);
        System.out.println(area);
        Store store = storeMapper.sel(1);
        System.out.println(store);
        return "1";
    }
}
