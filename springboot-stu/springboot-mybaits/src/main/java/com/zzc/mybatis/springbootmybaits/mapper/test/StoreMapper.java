package com.zzc.mybatis.springbootmybaits.mapper.test;

import com.zzc.mybatis.springbootmybaits.entity.Store;
import org.springframework.stereotype.Repository;

/**
 * @author zzc
 * @since 2020-09-02
 */
@Repository
public interface StoreMapper {
    Store sel(int id);
}
