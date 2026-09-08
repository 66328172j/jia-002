package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.model.auto.TSysDeviceLog;
import com.fc.v2.service.ITSysDeviceLogService;
import com.fc.v2.util.StringUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 设备履历controller
 * <p>履历从设备列表的“履历”入口打开，按设备ID查询，读操作无独立写权限。</p>
 *
 * @author jiabo
 * @date 2026-09-08
 */
@Controller
@RequestMapping("/SysDeviceLogController")
@Api(value = "设备履历表")
public class SysDeviceLogController extends BaseController {

    private final String prefix = "admin/sysDeviceLog";

    @Autowired
    private ITSysDeviceLogService deviceLogService;

    /**
     * 履历查看页面
     *
     * @param deviceId 设备ID（来自设备列表行内“履历”按钮）
     * @param mmap
     * @return
     */
    @ApiOperation(value = "履历页面跳转", notes = "履历页面跳转")
    @GetMapping("/view")
    @RequiresPermissions("gen:sysDevice:list")
    public String view(Long deviceId, ModelMap mmap) {
        mmap.put("deviceId", deviceId);
        return prefix + "/list";
    }

    /**
     * 履历列表查询（按设备倒序显示，最新在前）
     */
    @ApiOperation(value = "履历分页查询", notes = "履历分页查询")
    @PostMapping("/list")
    @RequiresPermissions("gen:sysDevice:list")
    @ResponseBody
    public ResultTable list(Long deviceId, TSysDeviceLog tSysDeviceLog) {
        QueryWrapper<TSysDeviceLog> queryWrapper = new QueryWrapper<TSysDeviceLog>();
        // 按设备定位履历；未指定设备ID时可再按编号模糊查（用于追溯已删除设备的档案）
        queryWrapper.eq(deviceId != null, "device_id", deviceId);
        queryWrapper.like(deviceId == null && StringUtils.isNotEmpty(tSysDeviceLog.getDeviceNo()),
                "device_no", tSysDeviceLog.getDeviceNo());
        queryWrapper.orderByDesc("create_time");

        startPage();
        PageInfo<TSysDeviceLog> page = new PageInfo<TSysDeviceLog>(deviceLogService.list(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }
}
