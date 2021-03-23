package com.zzc;

import com.zzc.dao.UserMapper;
import com.zzc.entity.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Application {
    private final static String RESOURCE_PATH = "mybatis-conf.xml";
    private static SqlSessionFactory sessionFactory;

    static {
        try(InputStream inputStream = Resources.getResourceAsStream(RESOURCE_PATH)) {
            sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private final static ThreadLocal<SqlSession> SQL_SESSION_THREAD_LOCAL = new ThreadLocal<SqlSession>();

    public static void main(String[] args) throws IOException {
        String[] strings = new String[]{};
        InputStream inputStream = Resources.getResourceAsStream(RESOURCE_PATH);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sessionFactory.openSession();
        User user = sqlSession.selectOne("com.zzc.dao.UserMapper.getUserByUid", 1);
        sqlSession.commit();
        User user1 = sqlSession.selectOne("com.zzc.dao.UserMapper.getUserByUid", 1);
//        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
//        UserMapper userMapper1 = sqlSession.getMapper(UserMapper.class);
//        User user = userMapper.getUserByUid(1);
//        User user1 = userMapper.getUserByUid(1);
        System.out.println(user);
        System.out.println(user1);
    }

    public static SqlSession getSqlSession(){
        if(SQL_SESSION_THREAD_LOCAL.get() != null){
            return SQL_SESSION_THREAD_LOCAL.get();
        }
        SqlSession sqlSession = sessionFactory.openSession();
        SQL_SESSION_THREAD_LOCAL.set(sqlSession);
        return sqlSession;
    }

    public static void closeSession(){
        if(SQL_SESSION_THREAD_LOCAL.get() != null){
            SqlSession sqlSession = SQL_SESSION_THREAD_LOCAL.get();
            sqlSession.close();
            SQL_SESSION_THREAD_LOCAL.set(null);
        }
    }
}
