package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.constant.DeviceStatus;
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
import org.springframework.web.util.HtmlUtils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

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
            // 新建设备统一为“在用”，由 service 写入建档履历；后续状态变更只能走状态机
            sysDevice.setStatus(DeviceStatus.IN_USE);
            return toAjax(sysDeviceService.insertTSysDeviceWithLog(sysDevice));
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
        // 删除同样写一条履历，档案删了也能追溯该设备曾处于什么状态
        return toAjax(sysDeviceService.deleteTSysDeviceWithLog(ids));
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
        TSysDevice sysDevice = sysDeviceService.selectTSysDeviceById(id);
        mmap.put("SysDevice", sysDevice);
        // 当前状态允许选择的目标状态（用于编辑页下拉放行/禁用非法流转）
        mmap.put("allowedStatus", DeviceStatus.allowedCodes(sysDevice == null ? null : sysDevice.getStatus()));
        return prefix + "/edit";
    }

    /**
     * 修改保存
     * <p>
     * 三层防线：
     * 1. 状态机校验——非法流转直接拦截并给出提示；
     * 2. 版本号乐观锁——打开页面后若被他人抢先保存过，本次保存拒绝，防止互相覆盖；
     * 3. 状态变化时由 service 落一条履历。
     */
    @Log(title = "设备档案修改", action = "editSave")
    @ApiOperation(value = "修改保存", notes = "修改保存")
    @RequiresPermissions("gen:sysDevice:edit")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(TSysDevice record) {
        if (record.getId() == null) {
            return error("参数错误：缺少设备ID");
        }
        TSysDevice current = sysDeviceService.selectTSysDeviceById(record.getId());
        if (current == null) {
            return error("设备不存在或已被删除，请刷新后重试");
        }
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

        // 状态机：只允许合法流转；未提交状态（null）视为保持不变
        Integer fromStatus = current.getStatus();
        Integer toStatus = record.getStatus();
        if (toStatus == null) {
            // 表单未提交状态：保持原状；数据库状态为空的历史脏数据在此顺带修正回“在用”
            toStatus = fromStatus == null ? DeviceStatus.IN_USE : fromStatus;
        }
        if (!DeviceStatus.canChange(fromStatus, toStatus)) {
            if (fromStatus == null) {
                // 空状态没有流转依据，不允许一步跳到维修/停机/报废，只能先修正为在用
                return error("设备状态为空（数据异常），请先将状态修正为「在用」后再操作");
            }
            return error(DeviceStatus.changeDeniedMessage(fromStatus, toStatus));
        }
        record.setStatus(toStatus);

        // 版本号乐观锁——页面携带的是打开编辑页时的 version：
        // 1) 库中已有版本基线、但与页面携带版本不一致：说明打开页面后被他人抢先保存过，直接拒绝并给出上下文提示；
        // 2) 库中无基线（老数据 version 为 NULL）：这里不再放行，交由 service 用 version IS NULL 原子抢占兜底，
        //    避免两台电脑同时对一条无基线记录首次保存而互相覆盖。
        Integer currentVersion = current.getVersion();
        if (currentVersion != null && !currentVersion.equals(record.getVersion())) {
            return buildConflict(current, "设备信息已被其他用户抢先修改，本次保存未执行。");
        }
        try {
            int rows = sysDeviceService.updateTSysDeviceWithLog(record, current);
            if (rows == 0) {
                // 真正裁决在 service 的带版本条件 UPDATE（影响 0 行即冲突，校验与提交之间存在并发窗口）。
                // 重查一次最新数据，把准确的“当前状态/最近操作人”带给用户确认，而不是一句笼统提示。
                TSysDevice latest = sysDeviceService.selectTSysDeviceById(record.getId());
                return buildConflict(latest == null ? current : latest,
                        "提交瞬间设备被其他用户抢先保存，本次保存未执行（已放弃覆盖）。");
            }
            return success();
        } catch (DuplicateKeyException e) {
            return error("设备编号已存在");
        }
    }

    /**
     * 构造并发冲突响应（业务码 409）。
     *
     * <p>被拦截的保存不会落库、也不会写状态履历；msg 内给出对方保存后的设备现状，
     * 前端弹层提示用户“重新确认/刷新”，避免拿过期页面的改动整体覆盖对方的结果。</p>
     *
     * @param latest 数据库当前最新记录（用于给用户展示现状）
     * @param reason 冲突原因
     * @return AjaxResult(code=409)
     */
    private AjaxResult buildConflict(TSysDevice latest, String reason) {
        StringBuilder msg = new StringBuilder(reason);
        if (latest != null) {
            String statusName = DeviceStatus.name(latest.getStatus());
            String updateBy = StringUtils.isEmpty(latest.getUpdateBy()) ? "未知" : latest.getUpdateBy();
            String updateTime = latest.getUpdateTime() == null
                    ? "-"
                    : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(latest.getUpdateTime());
            msg.append("该设备当前状态为【").append(statusName).append("】，")
                    .append("最近由「").append(HtmlUtils.htmlEscape(updateBy)).append("」于 ")
                    .append(updateTime).append(" 保存。");
        }
        msg.append("请刷新页面查看最新数据后，再重新编辑提交。");
        AjaxResult result = AjaxResult.error(409, msg.toString());
        if (latest != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("deviceId", latest.getId());
            data.put("deviceNo", latest.getDeviceNo());
            data.put("status", latest.getStatus());
            data.put("statusName", DeviceStatus.name(latest.getStatus()));
            data.put("updateBy", StringUtils.isEmpty(latest.getUpdateBy()) ? "未知" : latest.getUpdateBy());
            data.put("updateTime", latest.getUpdateTime() == null
                    ? null
                    : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(latest.getUpdateTime()));
            result.put("data", data);
        }
        return result;
    }
}
