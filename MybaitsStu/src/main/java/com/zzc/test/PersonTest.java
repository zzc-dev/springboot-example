package com.zzc.test;

import com.zzc.Application;
import com.zzc.entity.Person;

import java.util.List;

public class PersonTest {
    public static void main(String[] args) {
        insert();
        query();
    }

    public static void query(){
        List<Person> list = Application.getSqlSession().selectList("com.zzc.entity.PersonMapper.getAllPerson");
        System.out.println(list.size());
//        Application.getSqlSession().close();
    }

    public static void insert(){
        Person person = new Person(1, "zzc", 1);
        Application.getSqlSession().insert("com.zzc.entity.PersonMapper.addPerson", person);
//        Application.getSqlSession().close();
    }
}
