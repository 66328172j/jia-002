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
 * 设备履历记录对象 t_sys_device_log
 *
 * <p>记录设备的关键变动：建档、状态流转、删除。
 * 目标是能说清楚一台设备在什么时间处于什么状态、由谁操作。</p>
 *
 * @author jiabo
 * @date 2026-09-08
 */
@TableName("t_sys_device_log")
@ApiModel(value = "TSysDeviceLog", description = "设备履历记录表")
public class TSysDeviceLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 变更类型：新增建档 */
    public static final int CHANGE_CREATE = 1;
    /** 变更类型：状态流转 */
    public static final int CHANGE_STATUS = 2;
    /** 变更类型：删除档案 */
    public static final int CHANGE_DELETE = 3;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 设备ID（档案删除后仍保留，用于追溯） */
    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    /** 设备编号（操作时的快照，编号后续被改也能对上历史） */
    @ApiModelProperty(value = "设备编号")
    private String deviceNo;

    /** 变更前状态(0在用 1停机 2维修 3报废) */
    @ApiModelProperty(value = "变更前状态(0在用 1停机 2维修 3报废)")
    private Integer oldStatus;

    /** 变更后状态(0在用 1停机 2维修 3报废；删除时为空) */
    @ApiModelProperty(value = "变更后状态(0在用 1停机 2维修 3报废)")
    private Integer newStatus;

    /** 变更类型(1新增 2状态变更 3删除) */
    @ApiModelProperty(value = "变更类型(1新增 2状态变更 3删除)")
    private Integer changeType;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setOldStatus(Integer oldStatus) {
        this.oldStatus = oldStatus;
    }

    public Integer getOldStatus() {
        return oldStatus;
    }

    public void setNewStatus(Integer newStatus) {
        this.newStatus = newStatus;
    }

    public Integer getNewStatus() {
        return newStatus;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    public Integer getChangeType() {
        return changeType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("deviceId", getDeviceId())
                .append("deviceNo", getDeviceNo())
                .append("oldStatus", getOldStatus())
                .append("newStatus", getNewStatus())
                .append("changeType", getChangeType())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
