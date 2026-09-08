package com.fc.v2.model.auto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fc.v2.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.baomidou.mybatisplus.annotation.*;

/**
 * 设备档案对象 t_sys_device
 *
 * @author jiabo
 * @date 2026-09-08
 */
@TableName("t_sys_device")
@ApiModel(value = "SysDevice", description = "设备档案表")
public class TSysDevice extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 设备编号 */
    @ApiModelProperty(value = "设备编号")
    private String deviceNo;

    /** 设备名称 */
    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    /** 型号 */
    @ApiModelProperty(value = "型号")
    private String deviceModel;

    /** 生产厂家 */
    @ApiModelProperty(value = "生产厂家")
    private String manufacturer;

    /** 出厂日期 */
    @ApiModelProperty(value = "出厂日期")
    private String productionDate;

    /** 所在车间 */
    @ApiModelProperty(value = "所在车间")
    private String workshop;

    /** 责任人 */
    @ApiModelProperty(value = "责任人")
    private String principal;

    /** 状态(0在用 1停机 2维修 3报废) */
    @ApiModelProperty(value = "状态(0在用 1停机 2维修 3报废)")
    private Integer status;

    /** 乐观锁版本号，编辑保存时校验，防止多人同时操作互相覆盖 */
    @ApiModelProperty(value = "乐观锁版本号")
    private Integer version;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
    }

    public String getProductionDate() {
        return productionDate;
    }

    public void setWorkshop(String workshop) {
        this.workshop = workshop;
    }

    public String getWorkshop() {
        return workshop;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("deviceNo", getDeviceNo())
                .append("deviceName", getDeviceName())
                .append("deviceModel", getDeviceModel())
                .append("manufacturer", getManufacturer())
                .append("productionDate", getProductionDate())
                .append("workshop", getWorkshop())
                .append("principal", getPrincipal())
                .append("status", getStatus())
                .append("version", getVersion())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
