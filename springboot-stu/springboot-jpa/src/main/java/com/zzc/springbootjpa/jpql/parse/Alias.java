package com.zzc.springbootjpa.jpql.parse;

import java.lang.annotation.*;

/**
 * 定义查询结果的里的别名
 * JPA默认别名是select后面的位置，如select a from User a, a的别名是0
 * 你可以通过as指定别名
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.FIELD)
public @interface Alias {
    String value();
}