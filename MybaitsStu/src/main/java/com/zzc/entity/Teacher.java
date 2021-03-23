package com.zzc.entity;

import lombok.Data;

@Data
public class Teacher {
    private int tid;
    private String tname;
    private Classes classes;
}
