package com.zzc.springbootstu.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzc.springbootstu.po.User;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * @author zzc
 * @since 2020-09-02
 */
public class SettingConverter extends AbstractHttpMessageConverter<Object> {
    /**
     * 定义字符编码，防止乱码
     */
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /**
     * 新建自定义的媒体类型
     */
    public SettingConverter() {
        super(new MediaType("application", "json", DEFAULT_CHARSET));
    }

    /**
     * 表明只处理Settings这个类
     */
    @Override
    protected boolean supports(Class<?> aClass) {
        return true;
    }

    /**
     * 重写readInternal方法，处理请求的数据
     */
    @Override
    protected Object readInternal(Class<? extends Object> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        final String temp = StreamUtils.copyToString(httpInputMessage.getBody(), DEFAULT_CHARSET);
        Object object = JSON.parseObject(temp, aClass);
        return object;
//        if (temp.contains(TRAINING.name())) {
//            return JSONObject.parseObject(temp, new TypeReference<SettingInfoRequest<SettingInfo>>(){});
//        } else if (temp.contains(MODELDUMP.name())) {
//            return JSONObject.parseObject(temp, new TypeReference<SettingInfoRequest<DumpSettings>>(){});
//        }
//        return null;
    }

    @Override
    protected void writeInternal(Object user, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        httpOutputMessage.getBody().write("11".getBytes());
    }
}
