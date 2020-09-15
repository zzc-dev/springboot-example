package com.zzc.cache.service;

import com.zzc.cache.entity.User;
import com.zzc.cache.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzc
 * @since 2020-09-10
 */
@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    @Cacheable(cacheNames = "user")
    public String test(Integer id){
        User user = userMapper.getUser(id);
        System.out.println(user);
        return user.toString();
    }

    @Cacheable(cacheNames ="user", keyGenerator = "myKeyGenerator")
    public String test1(){
        return "2";
    }
}
