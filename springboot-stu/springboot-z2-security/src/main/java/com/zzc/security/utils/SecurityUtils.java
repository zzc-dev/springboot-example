package com.zzc.security.utils;

import com.zzc.security.security.JwtAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zzc
 * @since 2020-09-18
 */
public class SecurityUtils {
    /**
     * 系统登陆认证
     * */
    public static JwtAuthenticationToken login(HttpServletRequest request, String username, String password, AuthenticationManager manager) {
        JwtAuthenticationToken token = new JwtAuthenticationToken(username, password);
        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // 登陆认证过程
        Authentication authentication = manager.authenticate(token);
        // 认证成功存储认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 生成令牌并返回客户端
        token.setToken(JwtTokenUtils.generateToken(authentication));
        return token;
    }

    /**
     * 获取令牌认证
     * */
    public static void checkAuthentication(HttpServletRequest request){
        // 获取令牌并根据令牌获取登陆信息
        Authentication authentication = JwtTokenUtils.getAuthenticationFromToken(request);
        // 设置登陆认证到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 获取当前用户名
     * */
    public static String getUsername() {
        Authentication authentication = getAuthentication();
        return getUsername(authentication);
    }

    /**
     * 获取用户名
     * */
    public static String getUsername(Authentication authentication) {
        String username = null;
        if(authentication != null){
            Object principal = authentication.getPrincipal();
            if(principal != null && principal instanceof UserDetails){
                username = ((UserDetails)principal).getUsername();
            }
        }
        return username;
    }

    /**
     * 获取当前登陆信息
     * */
    public static Authentication getAuthentication() {
        if(SecurityContextHolder.getContext() == null){
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication;
    }
}
