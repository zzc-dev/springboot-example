package com.zzc.springbootjpa.jpql.parse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.jpa.spi.BaseQueryImpl;
import org.reflections.ReflectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

import javax.persistence.*;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zzc
 * @since 2020-09-03
 */
@Slf4j
public class JpqlHelper {
    public static void setQueryParameter(Map<String, Object> parameters, Query query, Query countQuery) {
        List<String> parameterNames = query.getParameters().stream().map(parameter -> parameter.getName()).collect(Collectors.toList());
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (parameterNames.contains(entry.getKey())) {
                    query.setParameter(entry.getKey(), entry.getValue());
                    if (countQuery != null) {
                        countQuery.setParameter(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    public static List convertMap(Class resultClass, List<Map<String, Object>> result) {
        if (Map.class.isAssignableFrom(resultClass)) {
            return result.stream().map(row->{
                Map data = (Map)BeanUtils.instantiate(resultClass);
                row.entrySet().stream().forEach(entry->{
                    String field = getFieldNameFromMapResult(entry);
                    data.put(field,entry.getValue());
                });
                return data;
            }).collect(Collectors.toList());
        }
        List resultList = new ArrayList();
        for (Map<String, Object> map : result) {
            Object data = BeanUtils.instantiate(resultClass);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                NameMapping mapping=AnnotationUtils.findAnnotation(resultClass,NameMapping.class);
                if(mapping==null||mapping.value().equals(NameStrategy.UNDERSCORE_TO_CAMMELCASE)){
                    String field = getFieldNameFromMapResult(entry);
                    setProperty(data, field, entry.getValue());
                }else {
                    setProperty(data, entry.getKey(), entry.getValue());
                }
            }
            resultList.add(data);
        }
        return resultList;
    }

    public static String getFieldNameFromMapResult(Map.Entry<String, Object> entry) {
        String[] column = StringUtils.split(entry.getKey().toLowerCase(), "_");
        StringBuilder stringBuilder = new StringBuilder(column[0]);
        if (column.length > 0) {
            for (int i = 1; i < column.length; i++) {
                stringBuilder.append(StringUtils.capitalize(column[i]));
            }
        }
        return stringBuilder.toString();
    }

    public static List convertTuple(Class resultClass, List<Tuple> result) {
        if (Tuple.class.isAssignableFrom(resultClass)) {
            return result;
        }
        List resultList = new ArrayList();
        for (Tuple tuple : result) {
            Object data = BeanUtils.instantiate(resultClass);
            //copy the properties to DTO if it is directly used
            copyPropertiesFromTuple(tuple, data);
            setPropertyWithAlias(resultClass, tuple, data);

            resultList.add(data);
        }
        return resultList;
    }

    public static void copyPropertiesFromTuple(Tuple tuple, Object data) {
        for (TupleElement tupleElement : tuple.getElements()) {
            Object object=tuple.get(tupleElement);
            copyProperties(data, object);
        }
    }

    private static void copyProperties(Object data, Object object) {
        if (object != null) {
            try {
                for(PropertyDescriptor propertyDescriptor :
                        Introspector.getBeanInfo(data.getClass()).getPropertyDescriptors()){
                    BeanMap test=new BeanMap(object);
                    if(propertyDescriptor.getWriteMethod()!=null){
                        try {
                            setProperty(data, propertyDescriptor.getName(), test.get(propertyDescriptor.getName()));
                        }catch (Exception e){
                            log.debug("error to copy property {} value from {}",propertyDescriptor.getName(),object.getClass());
                        }
                    }

                }
            } catch (Exception e) {
                log.debug("error to convert value from {}",object.getClass());
            }
        }
    }

    public static void setPropertyWithAlias(Class resultClass, Tuple tuple, Object data) {
        List<String> aliasNames=tuple.getElements().stream().map(t-> t.getAlias()).collect(Collectors.toList());
        for (Field field :  ReflectionUtils.getAllFields(resultClass)) {
            if (field.isAnnotationPresent(Alias.class)) {
                Alias alias = field.getAnnotation(Alias.class);
                if(aliasNames.contains(alias.value())){
                    Object tupleValue = tuple.get(alias.value());
                    if (tupleValue != null) {
                        Object value = null;
                        //if it is subclass of field, then
                        if (field.getType().isAssignableFrom(tupleValue.getClass())) {
                            value = tupleValue;
                        } else if(Number.class.isAssignableFrom(field.getType())){
                            value=convertValue(tupleValue,field.getType());
                        }
                        else {
                            value = BeanUtils.instantiate(field.getType());
                            copyProperties(tupleValue,value);
                        }
                        setProperty(data, field.getName(), value);
                    } else {
                        setProperty(data, field.getName(), null);
                    }
                }else {
                    log.debug("alias {} in query cannot be found",alias);
                }

            }
        }
    }
    public static Object convertValue(Object data, Class type){
        try {
            return ConvertUtils.convert(data,type);
        }catch (ConversionException e){
            log.debug("convert error for {}",type);
            return null;
        }

    }
    public static void setProperty(Object data, String field, Object value) {
        try {
            Field fieldInstance= ReflectionUtils.getAllFields(data.getClass()).stream().filter((field1 -> {
                return field1.getName().equals(field);
            })).collect(Collectors.toList()).get(0);
            Class fieldType=fieldInstance.getType();
            if (handlerEnum(data, field, value, fieldInstance, fieldType)) {
                return;
            }
            if (handlerClobAndBlob(data, field, value, fieldType)) {
                return;
            }
            if (handlerLong(data, field, value, fieldType)) {
                return;
            }
            org.apache.commons.beanutils.BeanUtils.setProperty(data, field, value);
        } catch (Exception e) {
            log.debug("error to set property {} on {}", field, data.getClass());
        }
    }

    private static boolean handlerLong(Object data, String field, Object value, Class fieldType) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if(Long.class.isAssignableFrom(fieldType)){
            if(value!=null){
                BigDecimal val=new BigDecimal(value.toString());
                PropertyUtils.setProperty(data,field,val.longValue());
                return true;
            }
        }
        return false;
    }

    private static boolean handlerEnum(Object data, String field, Object value, Field fieldInstance, Class fieldType) throws IllegalAccessException, InvocationTargetException {
        if (Enum.class.isAssignableFrom(fieldType)&&value!=null){
            if(fieldInstance.isAnnotationPresent(Enumerated.class)){
                Enumerated enumerated=fieldInstance.getAnnotation(Enumerated.class);
                switch (enumerated.value()){
                    case STRING:
                        org.apache.commons.beanutils.BeanUtils.setProperty(data, field, Enum.valueOf(fieldType,String.valueOf(value)));
                        break;
                    default:
                        Object[] array= fieldType.getEnumConstants();
                        BigDecimal val=new BigDecimal(value.toString());
                        org.apache.commons.beanutils.BeanUtils.setProperty(data, field, array[val.intValue()]);
                        break;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean handlerClobAndBlob(Object data, String field, Object value, Class fieldType) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SQLException {
        if(value!=null&&Clob.class.isAssignableFrom(value.getClass())){
            Clob clob=(Clob)value;
            if(Clob.class.isAssignableFrom(fieldType)){
                PropertyUtils.setProperty(data,field,clob);
            }else {
                org.apache.commons.beanutils.BeanUtils.setProperty(data, field, clob.getSubString(1,(int) clob.length()));
            }
            return true;
        }
        if(value!=null&&Blob.class.isAssignableFrom(value.getClass())){
            Blob blob=(Blob)value;
            if(Blob.class.isAssignableFrom(fieldType)){
                PropertyUtils.setProperty(data,field,blob);
            }else {
                org.apache.commons.beanutils.BeanUtils.setProperty(data, field, blob.getBytes(1,(int)blob.length()));
            }
            return true;
        }
        return false;
    }

    public static Class getResultClass(ParsedJpql jpql) {
        try {
            if (StringUtils.isEmpty(jpql.getResultClass())) {
                if (jpql.isNative()) {
                    return HashMap.class;
                } else {
                    return Tuple.class;
                }
            }
            return Class.forName(jpql.getResultClass());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("class not found: " + jpql.getResultClass());
        }

    }


    /**
     * 获取原生的EntityManager
     * @param query
     * @return
     */
    public static EntityManager getUnwrappedEntityManager(Query query) {
        try {
            BaseQueryImpl baseQuery = null;
            if(!Proxy.isProxyClass(query.getClass())){
                baseQuery = (BaseQueryImpl) query;
            }else {
                Object o=Proxy.getInvocationHandler(query);
                Field field=ReflectionUtils.getAllFields(o.getClass(),f -> f.getName().equals("target")).iterator().next();
                field.setAccessible(true);
                baseQuery=(BaseQueryImpl)field.get(o);
            }
            Field entityManagerField=ReflectionUtils.getAllFields(baseQuery.getClass(),f -> f.getName().equals("entityManager")).iterator().next();
            entityManagerField.setAccessible(true);
            return (EntityManager) entityManagerField.get(baseQuery);
        } catch (Exception e) {
            log.error("error to get entity manager");
            throw new RuntimeException(e);
        }

    }
}
