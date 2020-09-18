package com.zzc.security.utils;

import com.zzc.security.security.GrantedAuthorityImpl;
import com.zzc.security.security.JwtAuthenticationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

/**
 * @author zzc
 * @since 2020-09-18
 */
public class JwtTokenUtils implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名称
     */
    private static final String USERNAME = Claims.SUBJECT;
    /**
     * 创建时间
     */
    private static final String CREATED = "created";
    /**
     * 权限列表
     */
    private static final String AUTHORITIES = "authorities";
    /**
     * 密钥
     */
    private static final String SECRET = "abcdefgh";
    /**
     * 有效期12小时
     */
    private static final long EXPIRE_TIME = 12 * 60 * 60 * 1000;

    /**
     * 生成令牌
     * */
    public static String generateToken(Authentication authentication) {
        Map<String,Object > claims = new HashMap<>(3);
        claims.put(USERNAME, SecurityUtils.getUsername(authentication));
        claims.put(CREATED, new Date());
        claims.put(AUTHORITIES, authentication.getAuthorities());
        return generateToken(claims);
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private static String generateToken(Map<String,Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        return Jwts.builder().setClaims(claims).setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, SECRET).compact();
    }

    /**
     * 从令牌中获取用户名
     * */
    private static String getUsernameFromToken(String token) {
        String username;
        try{
          Claims claims = getClaimsFromToken(token);
          username = claims.getSubject();
        }catch (Exception e){
            username = null;
        }
        return username;
    }

    /**
     * 根据请求令牌获取登陆认证信息
     * */
    public static Authentication getAuthenticationFromToken(HttpServletRequest request) {
        Authentication authentication = null;
        String token = getToken(request);
        if(token != null){
            if(SecurityUtils.getAuthentication() == null){
                Claims claims = getClaimsFromToken(token);
                if(claims == null){
                    return null;
                }
                String username = claims.getSubject();
                if(username == null){
                    return null;
                }
                if(isTokenExpired(token)){
                    return null;
                }
                Object authors = claims.get(AUTHORITIES);
                List<GrantedAuthority> authorities = new ArrayList<>();
                if(authors != null && authors instanceof List){
                   for(Object o : (List) authors){
                       authorities.add(new GrantedAuthorityImpl( ((Map)o).get("authority").toString() ));
                   }
                }
                authentication = new JwtAuthenticationToken(username, null, authorities, token);
            }else {
                if(validateToken(token, SecurityUtils.getUsername())){
                    // 如果上下文中Authentication非空，且请求令牌合法，直接返回当前登录认证信息
                    authentication = SecurityUtils.getAuthentication();
                }
            }
        }
        return authentication;
    }


    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private static Claims getClaimsFromToken(String token) {
        Claims claims;
        try{
            claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        }catch (Exception e){
            e.printStackTrace();
            claims = null;
        }
        return claims;
    }

    /**
     * 验证令牌
     * */
    private static boolean validateToken(String token, String username) {
        String userName = getUsernameFromToken(token);
        return (userName.equals(userName) && !isTokenExpired(token));
    }

    public static String refreshToken(String token){
        String refreshToken;
        try {
            Claims claims = getClaimsFromToken(token);
            claims.put(CREATED, new Date());
            refreshToken = generateToken(claims);
            return refreshToken;
        }catch (Exception e){
            return null;
        }
    }

    private static boolean isTokenExpired(String token) {
        try{
            Claims claims = getClaimsFromToken(token);
            Date date = claims.getExpiration();
            return date.before(new Date());
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String tokenHeader  = "Bearer ";
        if(token == null){
            token = request.getHeader("token");
        }else if(token.contains(tokenHeader)){
            token = tokenHeader.substring(tokenHeader.length());
        }

        if("".equals(token)){
            token = null;
        }
        return token;
    }
}
