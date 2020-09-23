package com.zzc.springbootjpa.dao;

import com.zzc.springbootjpa.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author zzc
 * @since 2020-09-03
 */
public interface UserDao extends JpaRepository<User, Integer> {
    List<User> findAll();

    @Query(value = "select * from user where uid = :uid", nativeQuery = true)
    User getById(@Param("uid")Integer id);

}
