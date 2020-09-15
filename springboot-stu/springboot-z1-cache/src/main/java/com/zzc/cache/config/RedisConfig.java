package com.zzc.cache.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author zzc
 * @since 2020-09-15
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
//        template.setDefaultSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        RedisSerializerCustom redisSerializerCustom = new RedisSerializerCustom();
        RedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        template.setValueSerializer(redisSerializerCustom);
        template.setHashValueSerializer(redisSerializerCustom);

        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    public KeyGenerator myKeyGenerator(){
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                // 前缀处理
//                Cacheable annotation = method.getAnnotation(Cacheable.class);
//                if (null != annotation) {
//                    // 存在个问题，当这里的value使用数组时，缓存无法正常分层，应该是受到spring内部关于redis配置影响
//                    for (String v : annotation.value()) {
//                        sb.append(v);
//                    }
//                }
//                // 标识处理
//                for (Object obj : params) {
//                    sb.append(obj.toString()).append("-");
//                }
//                if (params.length > 0) {
//                    sb.deleteCharAt(sb.length() - 1);
//                }
                sb.append(target.getClass().getName()).append(":").append(method.getName()).append(Arrays.toString(params));
                return sb.toString();
            }
        };
    }
}
