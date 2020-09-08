package com.zzc.mybatis.springbootmybaits.mapper;

import com.zzc.mybatis.springbootmybaits.entity.User;
import org.springframework.stereotype.Repository;

/**
 * @author zzc
 * @since 2020-09-02
 */

@Repository
public interface UserMapper {

    User Sel(int id);

}
