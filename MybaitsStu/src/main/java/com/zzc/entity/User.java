package com.zzc.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {

    private long uid;
    private String[] username;
    private String password;
    private Date time;
}
