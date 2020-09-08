package com.zzc.springbootjpa.jpql.scroll;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.ScrollableResults;
import org.springframework.beans.BeanUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zzc.springbootjpa.jpql.parse.JpqlHelper.getFieldNameFromMapResult;
import static com.zzc.springbootjpa.jpql.parse.JpqlHelper.setProperty;


/**
 * @author zzc
 * @since 2020-09-03
 */
@Slf4j
public class MapScrollResult extends AbstractScrollResult {

    protected List<String> columns=new ArrayList<>();

    public MapScrollResult(ScrollableResults scrollableResults, Class resultClass, EntityManager entityManager){
        super(scrollableResults,resultClass,entityManager);
        try {
            Field field=scrollableResults.getClass().getSuperclass().getDeclaredField("resultSet");
            field.setAccessible(true);
            ResultSet resultSet=(ResultSet)field.get(scrollableResults);
            ResultSetMetaData resultSetMetaData=resultSet.getMetaData();
            for (int i=0;i<resultSetMetaData.getColumnCount();i++){
                //fix select as bug
                columns.add(resultSetMetaData.getColumnLabel(i+1));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getOriginal() {
        Map<String,Object> resultMap=new HashMap();
        Object[] result=scrollableResults.get();
        for (String column:columns){
            resultMap.put(column,result[columns.indexOf(column)]);
        }
        return resultMap;
    }

    public Object get(){
        Map<String,Object> resultMap=new HashMap();
        Object[] result=scrollableResults.get();
        for (String column:columns){
            resultMap.put(column,result[columns.indexOf(column)]);
        }
        if(Map.class.isAssignableFrom(resultClass)){
            Map data=new HashMap();
            for (Map.Entry<String, Object> entry : resultMap.entrySet()){
                String field = getFieldNameFromMapResult(entry);
                data.put(field,entry.getValue());
            }
            return data;
        }

        Object data = BeanUtils.instantiate(resultClass);
        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
            String field = getFieldNameFromMapResult(entry);
            setProperty(data, field, entry.getValue());
        }
        return data;
    }

}

