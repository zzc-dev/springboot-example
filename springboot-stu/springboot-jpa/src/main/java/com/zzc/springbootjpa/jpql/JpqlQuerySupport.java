package com.zzc.springbootjpa.jpql;

import com.zzc.springbootjpa.jpql.parse.JpqlHelper;
import com.zzc.springbootjpa.jpql.parse.JpqlParser;
import com.zzc.springbootjpa.jpql.parse.ParsedJpql;
import com.zzc.springbootjpa.jpql.scroll.MapScrollResult;
import com.zzc.springbootjpa.jpql.scroll.ScrollResult;
import com.zzc.springbootjpa.jpql.scroll.TupleScrollResult;
import org.apache.commons.beanutils.BeanMap;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.transform.Transformers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zzc.springbootjpa.jpql.parse.JpqlHelper.*;

/**
 * @author zzc
 * @since 2020-09-03
 */
@Component
public class JpqlQuerySupport {

    private EntityManager entityManager;
    private JpqlParser jpqlParser;

    public JpqlQuerySupport(EntityManager entityManager, JpqlParser jpqlParser){
        this.entityManager = entityManager;
        this.jpqlParser = jpqlParser;
    }

    public List findAll(String id, Object parameter) {
        Map<String, Object> beanMap = new HashMap(new BeanMap(parameter));
        return findAll(id, beanMap);
    }

    public Page findPage(String id, Object parameter, Pageable pageable) {
        Map<String, Object> beanMap = new HashMap(new BeanMap(parameter));
        return findPage(id, beanMap, pageable);
    }

    public Object findOne(String id, Object parameter) {
        Map<String, Object> beanMap = new HashMap(new BeanMap(parameter));
        return findOne(id, beanMap);
    }
    public int update(String id,Object parameter){
        Map<String, Object> beanMap = new HashMap(new BeanMap(parameter));
        return update(id, beanMap);
    }
    public int update(String id,Map<String,Object> parameters){
        ParsedJpql jpql = jpqlParser.parse(id, parameters, null);
        if (jpql.isNative()) {
            Query query = entityManager.createNativeQuery(jpql.getParsed());
            setQueryParameter(jpql.getParameterMap(), query, null);
            int count = query.executeUpdate();
            entityManager.flush();
            return count;
        } else {
            Query query=entityManager.createQuery(jpql.getParsed());
            setQueryParameter(jpql.getParameterMap(),query,null);
            int count  = query.executeUpdate();
            entityManager.flush();
            return count;
        }
    }

    public List findAll(String id, Map<String, Object> parameters) {

        ParsedJpql jpql = jpqlParser.parse(id, parameters, null);
        Class resultClass = getResultClass(jpql);
        if (jpql.isNative()) {
            Query query = entityManager.createNativeQuery(jpql.getParsed());
            setQueryParameter(jpql.getParameterMap(), query, null);
            query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            List result = query.getResultList();
            return convertMap(resultClass, result);
        } else {
            TypedQuery<Tuple> tupleTypedQuery = entityManager.createQuery(jpql.getParsed(), Tuple.class);
            setQueryParameter(jpql.getParameterMap(), tupleTypedQuery, null);
            return convertTuple(resultClass, tupleTypedQuery.getResultList());
        }

    }

    public ScrollResult findScrollResult(String id, Object parameters){
        Map<String, Object> beanMap = new HashMap(new BeanMap(parameters));
        return findScrollResult(id,beanMap);
    }
    public ScrollResult findScrollResult(String id, Map<String, Object> parameters) {

        ParsedJpql jpql = jpqlParser.parse(id, parameters, null);
        Class resultClass = getResultClass(jpql);
        if (jpql.isNative()) {
            Query query = entityManager.createNativeQuery(jpql.getParsed());
            setQueryParameter(jpql.getParameterMap(), query, null);
            SQLQuery sqlQuery=query.unwrap(SQLQuery.class);
            return new MapScrollResult(sqlQuery.scroll(),resultClass,JpqlHelper.getUnwrappedEntityManager(query));
        } else {
            TypedQuery<Tuple> tupleTypedQuery = entityManager.createQuery(jpql.getParsed(), Tuple.class);
            setQueryParameter(jpql.getParameterMap(), tupleTypedQuery, null);
            org.hibernate.Query hquery = tupleTypedQuery.unwrap(org.hibernate.Query.class);
            ScrollableResults results = hquery.scroll(ScrollMode.FORWARD_ONLY);
            return new TupleScrollResult(results,resultClass,JpqlHelper.getUnwrappedEntityManager(tupleTypedQuery));
        }

    }

    public Page findPage(String id, Map<String, Object> parameters, Pageable pageable) {
        ParsedJpql jpql = jpqlParser.parse(id, parameters, pageable);
        Class resultClass = getResultClass(jpql);
        if (jpql.isNative()) {
            Query query = entityManager.createNativeQuery(jpql.getParsed());
            Query countQuery = entityManager.createNativeQuery("select count(1) from (" + jpql.getParsed()+") a");
            setQueryParameter(jpql.getParameterMap(), query, countQuery);
            Long count = ((Number) countQuery.getSingleResult()).longValue();
            query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            query.setFirstResult((int)pageable.getOffset()).setMaxResults(pageable.getPageSize());
            List result = query.getResultList();
            return new PageImpl(convertMap(resultClass, result), pageable, count);
        } else {
            TypedQuery<Tuple> tupleTypedQuery = entityManager.createQuery(jpql.getParsed(), Tuple.class);
            TypedQuery<Long> countQuery = entityManager.createQuery(QueryUtils.createCountQueryFor(jpql.getParsed()), Long.class);
            setQueryParameter(jpql.getParameterMap(), tupleTypedQuery, countQuery);
            Long count = countQuery.getSingleResult();
            tupleTypedQuery.setFirstResult((int)pageable.getOffset()).setMaxResults(pageable.getPageSize());
            List<Tuple> result = tupleTypedQuery.getResultList();
            return new PageImpl(convertTuple(resultClass, result), pageable, count);
        }

    }

    public Object findOne(String id, Map<String, Object> parameters) {
        List result = findAll(id, parameters);
        return result == null || result.isEmpty() ? null : result.iterator().next();
    }
}
