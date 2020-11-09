package com.zzc.springbootjpa.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

/**
 * @author zzc
 * @since 2020-10-30
 */
@Entity
@Data
@Table(name = "ra_tbl_device")
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HaiyanDevice extends BaseEntity {

    /**
     * 设备码
     */
    @Basic
    private String deviceCode;

    /**
     * 设备名称
     */
    @Basic
    private String deviceName;
    /**
     * 业务自定义类型 具体的类型
     * deviceType
     */
    @Basic
    private String deviceType;

    /**
     * 纬度
     */
    @Basic
    private Double lat;
    /**
     * 经度
     */
    @Basic
    private Double lng;

    /**
     * 国标ID
     */
    @Basic
    private String gbId;

    /**
     * 父编号
     * RaTblDevice
     */
    @Basic
    private String parentId;

    /**
     * 行政区划
     */
    @Basic
    private String civilCode;

    /**
     * 安装地址
     */
    @Basic
    private String address;

    /**
     * 分辨率
     */
    @Basic
    private String resolution;

    /**
     * 车道上对应的采集处理设备ID
     */
    @Basic
    private String laneApeViidId;

    @Basic
    private String viidId;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
