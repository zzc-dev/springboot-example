package com.zzc.springbootjpa.dao;

import com.zzc.springbootjpa.data.entity.HaiyanDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zzc
 * @since 2020-10-30
 */
@Repository
public interface HaiyanDeviceDao extends JpaRepository<HaiyanDevice,Integer> {

    HaiyanDevice findByViidId(@Param("viidId") String viidId);

    List<HaiyanDevice> findByViidIdIn(@Param("viidIds") List<String> viidIds);

    HaiyanDevice findByGbId(@Param("gbId") String gbId);

    List<HaiyanDevice> findByGbIdIn(@Param("gbIds") List<String> gbIds);

    @Query(value = "select device_code from ra_tbl_device", nativeQuery = true)
    List<String> queryDeviceCodes();

    HaiyanDevice findByDeviceCode(@Param("deviceCode") String deviceCode);

    void deleteByDeviceCode(@Param("deviceCode") String deviceCode);

}
