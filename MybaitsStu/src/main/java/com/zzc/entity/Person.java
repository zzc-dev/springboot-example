package com.zzc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    private int pid;
    private String pname;
    private int page;

    @Override
    public String toString() {
        return "Person [pid=" + pid + ", pname=" + pname + ", page=" + page
                + "]";
    }
}
