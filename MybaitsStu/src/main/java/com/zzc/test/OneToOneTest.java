package com.zzc.test;

import com.zzc.Application;
import com.zzc.entity.Classes;

public class OneToOneTest {

    public static void main(String[] args) {
        query1();
    }

    public static void query1(){
        Classes classes = Application.getSqlSession().selectOne("one.to.one.classesMapper.getClasses",1);
        System.out.println(classes.getTeacher().getTid());
    }
}
