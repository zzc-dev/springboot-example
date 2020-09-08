package com.zzc.springbootjpa.jpql.scroll;

import org.hibernate.ScrollableResults;
import org.springframework.beans.BeanUtils;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import static com.zzc.springbootjpa.jpql.parse.JpqlHelper.copyPropertiesFromTuple;
import static com.zzc.springbootjpa.jpql.parse.JpqlHelper.setPropertyWithAlias;

/**
 * @author zzc
 * @since 2020-09-03
 */
public class TupleScrollResult extends AbstractScrollResult {
    public TupleScrollResult(ScrollableResults scrollableResults, Class resultClass, EntityManager entityManager){
        super(scrollableResults,resultClass,entityManager);
    }

    public Object get(){
        if(Tuple.class.isAssignableFrom(resultClass)){
            return scrollableResults.get(0);
        }

        Object data = BeanUtils.instantiate(resultClass);
        //copy the properties to DTO if it is directly used
        Tuple tuple=(Tuple)scrollableResults.get(0);
        copyPropertiesFromTuple(tuple, data);
        setPropertyWithAlias(resultClass, tuple, data);
        return data;
    }

    @Override
    public Object getOriginal() {
        return scrollableResults.get(0);
    }
}