package com.zzc.cache.mapper;

import com.zzc.cache.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author zzc
 * @since 2020-09-10
 */
@Mapper
public interface UserMapper {

    @Select("select * from user where uid = #{id}")
    public User getUser(Integer id);
}
