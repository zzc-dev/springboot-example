package com.zzc.cache;

import com.zzc.cache.entity.User;
import com.zzc.cache.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
class SpringbootZ1CacheApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        System.out.println(userService.test(1));
    }

    @Test
    void test(){
//        stringRedisTemplate.opsForValue().set("str", "hello");
//        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent("str", "world"); // setnx
//        stringRedisTemplate.opsForValue().append("str", " world");
//        System.out.println(stringRedisTemplate.opsForValue().get("str"));
//        List list = Arrays.asList("1","2","3");
//        stringRedisTemplate.opsForList().leftPushAll("list", list);
//        List<String> range = stringRedisTemplate.opsForList().range("list", 0, stringRedisTemplate.opsForList().size("list"));
//        System.out.println(range);

        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("a", "1");
        hashMap.put("b", "2");
        hashMap.put("c", "3");
        stringRedisTemplate.opsForHash().putAll("map", hashMap);
        Set<Object> kyes = stringRedisTemplate.opsForHash().keys("map");
        List<Object> values = stringRedisTemplate.opsForHash().values("map");

        List keyList = Arrays.asList("a");
        List list = stringRedisTemplate.opsForHash().multiGet("map", keyList);
        System.out.println(list);
    }

    @Test
    void testObject(){
//        redisTemplate.opsForValue().set("user1", new User());
//        User user1 = (User) redisTemplate.opsForValue().get("user1");
//        System.out.println(user1);
        HashMap<String,User> hashMap = new HashMap<>();
        hashMap.put("a", new User(1L, "zzc", "123456"));
        redisTemplate.opsForHash().putAll("map1", hashMap);
        List<Object> values = redisTemplate.opsForHash().values("map1");
    }
}
