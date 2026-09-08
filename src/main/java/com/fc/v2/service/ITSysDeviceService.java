package com.fc.v2.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import com.fc.v2.model.auto.TSysDevice;

/**
 * 设备档案Service接口
 *
 * @author jiabo
 * @date 2026-09-08
 */
public interface ITSysDeviceService extends IService<TSysDevice> {

    /**
     * 查询设备档案
     *
     * @param id 设备档案ID
     * @return 设备档案
     */
    public TSysDevice selectTSysDeviceById(Long id);

    /**
     * 查询设备档案列表
     *
     * @param queryWrapper 设备档案
     * @return 设备档案集合
     */
    public List<TSysDevice> selectTSysDeviceList(Wrapper<TSysDevice> queryWrapper);

    /**
     * 新增设备档案
     *
     * @param tSysDevice 设备档案
     * @return 结果
     */
    public int insertTSysDevice(TSysDevice tSysDevice);

    /**
     * 修改设备档案
     *
     * @param tSysDevice 设备档案
     * @return 结果
     */
    public int updateTSysDevice(TSysDevice tSysDevice);

    /**
     * 批量删除设备档案
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteTSysDeviceByIds(String ids);

    /**
     * 删除设备档案信息
     *
     * @param id 设备档案ID
     * @return 结果
     */
    public int deleteTSysDeviceById(Long id);

    /**
     * 检查设备编号是否唯一（编辑时排除自身）
     *
     * @param tSysDevice 设备档案
     * @return 重复数量
     */
    int checkDeviceNoUnique(TSysDevice tSysDevice);
}
