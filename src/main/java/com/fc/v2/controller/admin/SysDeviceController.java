package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TSysDevice;
import com.fc.v2.service.ITSysDeviceService;
import com.fc.v2.util.StringUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 设备档案 controller
 *
 * @author jiabo
 * @date 2026-09-08
 */
@Controller
@RequestMapping("/SysDeviceController")
@Api(value = "设备档案表")
public class SysDeviceController extends BaseController {

    private final String prefix = "admin/sysDevice";

    @Autowired
    private ITSysDeviceService sysDeviceService;

    /**
     * list展示
     */
    @ApiOperation(value = "分页跳转", notes = "分页跳转")
    @GetMapping("/view")
    @RequiresPermissions("gen:sysDevice:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    /**
     * 设备档案查询
     *
     * @param tSysDevice
     * @return
     */
    @Log(title = "设备档案集合查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @PostMapping("/list")
    @RequiresPermissions("gen:sysDevice:list")
    @ResponseBody
    public ResultTable list(TSysDevice tSysDevice) {
        QueryWrapper<TSysDevice> queryWrapper = new QueryWrapper<TSysDevice>();
        //设备编号模糊搜索
        queryWrapper.like(StringUtils.isNotEmpty(tSysDevice.getDeviceNo()), "device_no", tSysDevice.getDeviceNo());
        //设备名称模糊搜索
        queryWrapper.like(StringUtils.isNotEmpty(tSysDevice.getDeviceName()), "device_name", tSysDevice.getDeviceName());
        //所在车间精确匹配（先去空格，避免录入/筛选时首尾空格不一致导致筛不出）
        String workshop = StringUtils.trim(tSysDevice.getWorkshop());
        queryWrapper.eq(StringUtils.isNotEmpty(workshop), "workshop", workshop);

        startPage();
        PageInfo<TSysDevice> page = new PageInfo<TSysDevice>(sysDeviceService.selectTSysDeviceList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    /**
     * 新增跳转
     *
     * @param modelMap
     * @return
     */
    @ApiOperation(value = "新增跳转", notes = "新增跳转")
    @GetMapping("/add")
    public String add(ModelMap modelMap) {
        return prefix + "/add";
    }

    /**
     * 新增
     *
     * @param sysDevice
     * @return
     */
    @Log(title = "设备档案新增", action = "add")
    @ApiOperation(value = "新增", notes = "新增")
    @PostMapping("add")
    @RequiresPermissions("gen:sysDevice:add")
    @ResponseBody
    public AjaxResult add(TSysDevice sysDevice) {
        if (sysDevice.getDeviceNo() == null) {
            return error("设备编号不能为空");
        }
        // 去首尾空格，避免空格造成“同号不同串”
        sysDevice.setDeviceNo(sysDevice.getDeviceNo().trim());
        if (StringUtils.isEmpty(sysDevice.getDeviceNo())) {
            return error("设备编号不能为空");
        }
        if (sysDeviceService.checkDeviceNoUnique(sysDevice) > 0) {
            return error("设备编号已存在");
        }
        try {
            return toAjax(sysDeviceService.insertTSysDevice(sysDevice));
        } catch (DuplicateKeyException e) {
            // 并发下预检与插入之间可能被另一条相同编号抢插，靠数据库唯一索引兜底拦截
            return error("设备编号已存在");
        }
    }

    /**
     * 删除设备
     *
     * @param ids
     * @return
     */
    @Log(title = "设备档案删除", action = "remove")
    @ApiOperation(value = "删除", notes = "删除")
    @DeleteMapping("/remove")
    @RequiresPermissions("gen:sysDevice:remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(sysDeviceService.deleteTSysDeviceByIds(ids));
    }

    /**
     * 检查设备编号唯一
     *
     * @param sysDevice
     * @return 1 重复 0 可用
     */
    @ApiOperation(value = "检查设备编号唯一", notes = "检查设备编号唯一")
    @PostMapping("/checkDeviceNoUnique")
    @ResponseBody
    public int checkDeviceNoUnique(TSysDevice sysDevice) {
        return sysDeviceService.checkDeviceNoUnique(sysDevice) > 0 ? 1 : 0;
    }

    /**
     * 修改跳转
     *
     * @param id
     * @param mmap
     * @return
     */
    @ApiOperation(value = "修改跳转", notes = "修改跳转")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        mmap.put("SysDevice", sysDeviceService.selectTSysDeviceById(id));
        return prefix + "/edit";
    }

    /**
     * 修改保存
     */
    @Log(title = "设备档案修改", action = "editSave")
    @ApiOperation(value = "修改保存", notes = "修改保存")
    @RequiresPermissions("gen:sysDevice:edit")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(TSysDevice record) {
        if (record.getDeviceNo() == null) {
            return error("设备编号不能为空");
        }
        record.setDeviceNo(record.getDeviceNo().trim());
        if (StringUtils.isEmpty(record.getDeviceNo())) {
            return error("设备编号不能为空");
        }
        if (sysDeviceService.checkDeviceNoUnique(record) > 0) {
            return error("设备编号已存在");
        }
        try {
            return toAjax(sysDeviceService.updateTSysDevice(record));
        } catch (DuplicateKeyException e) {
            return error("设备编号已存在");
        }
    }
}
