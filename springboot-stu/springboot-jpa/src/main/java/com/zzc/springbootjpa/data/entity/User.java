package com.zzc.springbootjpa.data.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author zzc
 * @since 2020-09-02
 */
@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;
    private String username;
    private String password;
    @Version
    private Long version;
}
