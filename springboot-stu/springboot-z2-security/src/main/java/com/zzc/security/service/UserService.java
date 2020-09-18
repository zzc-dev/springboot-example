package com.zzc.security.service;

import com.zzc.security.bean.User;

import java.util.Set;

/**
 * @author zzc
 * @since 2020-09-18
 */
public interface UserService {

    /**
     * 根据用户名查找用户
     * @param username
     * @return
     */
    User findByUsername(String username);

    /**
     * 查找用户的菜单权限标识集合
     * @param userName
     * @return
     */
    Set<String> findPermissions(String username);
}
