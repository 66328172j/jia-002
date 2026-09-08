package com.fc.v2.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.constant.DeviceStatus;
import com.fc.v2.model.auto.TSysDeviceLog;
import com.fc.v2.service.ITSysDeviceLogService;
import com.fc.v2.service.ITSysDeviceService;
import com.fc.v2.shiro.util.ShiroUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fc.v2.mapper.auto.TSysDeviceMapper;
import com.fc.v2.model.auto.TSysDevice;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.util.StringUtils;
/**
 * 设备档案Service业务层处理
 *
 * @author jiabo
 * @date 2026-09-08
 */
@Service
public class TSysDeviceServiceImpl extends ServiceImpl<TSysDeviceMapper, TSysDevice> implements ITSysDeviceService {

    @Autowired
    private ITSysDeviceLogService deviceLogService;

    @Override
    public TSysDevice selectTSysDeviceById(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public List<TSysDevice> selectTSysDeviceList(Wrapper<TSysDevice> queryWrapper) {
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public int insertTSysDevice(TSysDevice tSysDevice) {
        return this.baseMapper.insert(tSysDevice);
    }

    @Override
    public int updateTSysDevice(TSysDevice tSysDevice) {
        return this.baseMapper.updateById(tSysDevice);
    }

    @Override
    public int deleteTSysDeviceByIds(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return 0;
        }
        // 主键为 Long，按 Long 解析后删除，与 @TableId 类型对齐，避免字符串参与 IN 比较的边界问题
        return this.baseMapper.deleteBatchIds(Arrays.asList(ConvertUtil.toLongArray(ids)));
    }

    @Override
    public int deleteTSysDeviceById(Long id) {
        return this.baseMapper.deleteById(id);
    }

    @Override
    public int checkDeviceNoUnique(TSysDevice tSysDevice) {
        QueryWrapper<TSysDevice> queryWrapper = new QueryWrapper<TSysDevice>();
        queryWrapper.eq("device_no", tSysDevice.getDeviceNo());
        //编辑时排除当前记录自身，避免"未修改编号"被误判重复
        queryWrapper.ne(tSysDevice.getId() != null, "id", tSysDevice.getId());
        return this.baseMapper.selectCount(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTSysDeviceWithLog(TSysDevice tSysDevice) {
        // 新建设备统一从"在用"开始，后续状态必须走状态机流转
        if (tSysDevice.getStatus() == null) {
            tSysDevice.setStatus(DeviceStatus.IN_USE);
        }
        // 新建设备即进入乐观锁管理，version 从 0 开始计数
        //（前置条件：t_sys_device 需已执行 doc/device_concurrency_fix.sql 增加 version 列）
        if (tSysDevice.getVersion() == null) {
            tSysDevice.setVersion(0);
        }
        int rows = this.baseMapper.insert(tSysDevice);
        if (rows == 1) {
            deviceLogService.save(buildLog(tSysDevice.getId(), tSysDevice.getDeviceNo(),
                    null, tSysDevice.getStatus(), TSysDeviceLog.CHANGE_CREATE));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTSysDeviceWithLog(TSysDevice tSysDevice, TSysDevice dbRow) {
        // 乐观锁：更新必须命中"数据库当前仍是本次编辑所基于的版本"这一条件。
        // 版本不匹配说明打开编辑页后已被他人抢先保存，本语句影响 0 行，由上层提示冲突并引导重确认，
        // 从而杜绝"后保存的整体覆盖先保存的"。
        //
        // 关键兜底：历史数据无乐观锁基线（version 为 NULL）时，不能像旧实现那样无条件放行，
        // 否则两台电脑对同一条老记录首次保存会互相覆盖且都成功、都写履历。
        // 这里改为把 version IS NULL 也作为匹配条件，利用 UPDATE 的行锁使并发首存只有一个命中。
        Integer dbVersion = dbRow == null ? null : dbRow.getVersion();
        LambdaUpdateWrapper<TSysDevice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TSysDevice::getId, tSysDevice.getId());
        if (dbVersion == null) {
            updateWrapper.isNull(TSysDevice::getVersion);
            // 首次保存建立基线 1，此后进入常规的版本递增
            tSysDevice.setVersion(1);
        } else {
            updateWrapper.eq(TSysDevice::getVersion, dbVersion);
            tSysDevice.setVersion(dbVersion + 1);
        }
        // 主动刷新更新人/更新时间；createBy/createTime 已由 FieldStrategy.NEVER 保护，不会被覆盖
        tSysDevice.setUpdateBy(ShiroUtils.getLoginName());
        tSysDevice.setUpdateTime(new Date());
        int rows = this.baseMapper.update(tSysDevice, updateWrapper);
        if (rows == 0) {
            return 0;
        }
        // 状态确实发生变化时，写一条状态流转履历
        Integer fromStatus = dbRow == null ? null : dbRow.getStatus();
        if (!Objects.equals(fromStatus, tSysDevice.getStatus())) {
            deviceLogService.save(buildLog(tSysDevice.getId(), tSysDevice.getDeviceNo(),
                    fromStatus, tSysDevice.getStatus(), TSysDeviceLog.CHANGE_STATUS));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTSysDeviceWithLog(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return 0;
        }
        List<Long> idList = Arrays.asList(ConvertUtil.toLongArray(ids));
        if (idList.isEmpty()) {
            return 0;
        }
        // 先取删除前的快照，用于写删除履历（设备删除后履历仍可追溯）
        List<TSysDevice> devices = this.baseMapper.selectBatchIds(idList);
        int rows = this.baseMapper.deleteBatchIds(idList);
        for (TSysDevice device : devices) {
            deviceLogService.save(buildLog(device.getId(), device.getDeviceNo(),
                    device.getStatus(), null, TSysDeviceLog.CHANGE_DELETE));
        }
        return rows;
    }

    /**
     * 组装一条设备履历记录
     *
     * @param deviceId   设备ID
     * @param deviceNo   设备编号快照
     * @param oldStatus  变更前状态（新增时为 null）
     * @param newStatus  变更后状态（删除时为 null）
     * @param changeType 变更类型
     */
    private TSysDeviceLog buildLog(Long deviceId, String deviceNo, Integer oldStatus, Integer newStatus, int changeType) {
        TSysDeviceLog log = new TSysDeviceLog();
        log.setDeviceId(deviceId);
        log.setDeviceNo(deviceNo);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangeType(changeType);
        return log;
    }
}
