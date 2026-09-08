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

    /**
     * 新增设备档案并写一条建档履历
     *
     * @param tSysDevice 设备档案
     * @return 结果
     */
    int insertTSysDeviceWithLog(TSysDevice tSysDevice);

    /**
     * 编辑保存设备档案：按 version 乐观锁更新，状态变化时写一条流转履历
     *
     * @param tSysDevice 编辑后的设备档案（status 已按状态机校验过）
     * @param dbRow      编辑前数据库中的原始记录（用于旧状态与版本比对）
     * @return 更新行数（0 表示已被他人抢先修改，乐观锁冲突）
     */
    int updateTSysDeviceWithLog(TSysDevice tSysDevice, TSysDevice dbRow);

    /**
     * 批量删除设备档案，删除前为每台设备写一条删除履历
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteTSysDeviceWithLog(String ids);
}
