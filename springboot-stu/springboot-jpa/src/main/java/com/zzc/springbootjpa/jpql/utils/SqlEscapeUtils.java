package com.zzc.springbootjpa.jpql.utils;

import org.apache.commons.lang.StringEscapeUtils;


/**
 * @author zzc
 * @since 2020-09-03
 * 用于过滤sql的特殊字符等
 */
public class SqlEscapeUtils {

    public static String escapeSingleQuote(String name){
        return StringEscapeUtils.escapeSql(name);
    }

    public static String escapeLike(String name){
        if(name==null)
            return "";
        StringBuilder stringBuilder=new StringBuilder();
        for (char c :name.toCharArray()){
            switch (c){
                case '/':
                    stringBuilder.append("//");
                    break;
                case '%':
                    stringBuilder.append("/%");
                    break;
                case '_':
                    stringBuilder.append("/_");
                    break;
                default:
                    stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }
}