package com.zzc.cache.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zzc
 * @since 2020-09-02
 */
@Data
@AllArgsConstructor
public class User implements Serializable {
    private Long uid;
    private String username;
    private String password;
}
