package com.zzc.cache.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * @author zzc
 * @since 2020-09-15
 */
public class RedisSerializerCustom implements RedisSerializer {

    private final static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    static {
        // 添加白名单过滤
        ParserConfig.getGlobalInstance().addAccept("com.zzc.");
    }

    @Override
    public byte[] serialize(Object o) throws SerializationException {
        System.out.println("serialize="+ o);
        if(o == null){
            return new byte[0];
        }
        return JSON.toJSONString(o).getBytes(DEFAULT_CHARSET);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        System.out.println("deserialize"+bytes);
        if(bytes == null || bytes.length == 0){
            return null;
        }
        try {
            if (!ParserConfig.getGlobalInstance().isAutoTypeSupport()) {
                ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
            }
            return JSONObject.parse(new String(bytes, DEFAULT_CHARSET));
        } catch (Exception e) {
           throw new SerializationException("Could not deserialize: " + e.getMessage(), e);
        }
    }
}
