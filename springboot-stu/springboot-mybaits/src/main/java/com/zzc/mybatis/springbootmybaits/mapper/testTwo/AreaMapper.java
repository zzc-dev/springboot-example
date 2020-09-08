package com.zzc.mybatis.springbootmybaits.mapper.testTwo;

import com.zzc.mybatis.springbootmybaits.entity.Area;
import org.springframework.stereotype.Repository;

/**
 * @author zzc
 * @since 2020-09-02
 */
@Repository
public interface AreaMapper {
    Area sel(int id);
}
