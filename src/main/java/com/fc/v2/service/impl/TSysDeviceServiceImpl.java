package com.fc.v2.service.impl;

import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.service.ITSysDeviceService;

import org.springframework.stereotype.Service;
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
}
