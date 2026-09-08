package com.fc.v2.common.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 设备状态常量与状态流转规则
 *
 * <p>状态编码与 t_sys_device.status 字段约定一致：
 * 0-在用、1-停机、2-维修、3-报废。</p>
 *
 * <p>流转规则（只允许以下路径，同状态保存不算流转，始终放行）：</p>
 * <pre>
 *   在用(0) → 停机(1) / 维修(2) / 报废(3)      # 在用的设备可停机、报维修；寿命到期/淘汰可报废
 *   停机(1) → 在用(0) / 维修(2)                # 停机只能恢复在用或转维修，不能直接报废
 *   维修(2) → 在用(0) / 报废(3)                # 维修只能恢复在用（或修不了报废），不能同时改停机
 *   报废(3) → 报废(3)                          # 终态，一旦报废不能再改其他状态
 * </pre>
 *
 * <p>若业务上要收紧/放宽（例如报废只能从维修进入、或在用不允许直接报废），
 * 只需调整下方 NEXT 集合即可，其余逻辑自动跟随。</p>
 *
 * @author jiabo
 * @date 2026-09-08
 */
public final class DeviceStatus {

    private DeviceStatus() {
    }

    /** 在用 */
    public static final Integer IN_USE = 0;
    /** 停机 */
    public static final Integer STOP = 1;
    /** 维修 */
    public static final Integer REPAIR = 2;
    /** 报废（终态） */
    public static final Integer SCRAP = 3;

    /** 状态显示名 */
    private static final Map<Integer, String> NAMES = new HashMap<>();

    /** 每个状态允许流转到的目标状态集合 */
    private static final Map<Integer, Set<Integer>> NEXT = new HashMap<>();

    static {
        NAMES.put(IN_USE, "在用");
        NAMES.put(STOP, "停机");
        NAMES.put(REPAIR, "维修");
        NAMES.put(SCRAP, "报废");

        NEXT.put(IN_USE, setOf(IN_USE, STOP, REPAIR, SCRAP));
        NEXT.put(STOP, setOf(STOP, IN_USE, REPAIR));
        NEXT.put(REPAIR, setOf(REPAIR, IN_USE, SCRAP));
        NEXT.put(SCRAP, setOf(SCRAP));
    }

    private static Set<Integer> setOf(Integer... codes) {
        return new HashSet<>(Arrays.asList(codes));
    }

    /**
     * 判断 from 状态能否流转到 to 状态
     *
     * <p>状态为空（NULL）属于脏数据，不代表“在用”，不允许借它跳到维修/报废等任意状态；
     * 只能保持原样（to 为空）或显式修正回“在用(0)”。</p>
     *
     * @param from 当前状态（为空视为脏数据，只允许修正回在用）
     * @param to   目标状态（为空视为保持不变）
     * @return true 允许
     */
    public static boolean canChange(Integer from, Integer to) {
        if (from == null) {
            return to == null || IN_USE.equals(to);
        }
        if (to == null) {
            to = from;
        }
        Set<Integer> allowed = NEXT.get(from);
        return allowed != null && allowed.contains(to);
    }

    /**
     * 取某个状态下允许选择的目标状态（含自身，按 0-3 固定顺序返回）
     * 用于编辑页下拉框放行/禁用
     */
    public static List<Integer> allowedCodes(Integer from) {
        if (from == null) {
            // 空状态脏数据：仅允许修正为“在用”，其余流转一律不给选项
            return new ArrayList<>(Arrays.asList(IN_USE));
        }
        Set<Integer> allowed = NEXT.get(from);
        if (allowed == null) {
            return Collections.emptyList();
        }
        List<Integer> list = new ArrayList<>();
        for (Integer code : Arrays.asList(IN_USE, STOP, REPAIR, SCRAP)) {
            if (allowed.contains(code)) {
                list.add(code);
            }
        }
        return list;
    }

    /**
     * 状态显示名
     */
    public static String name(Integer status) {
        return status == null ? "" : NAMES.getOrDefault(status, String.valueOf(status));
    }

    /**
     * 非法流转的友好提示
     */
    public static String changeDeniedMessage(Integer from, Integer to) {
        String fromName = name(from);
        String toName = name(to);
        if (SCRAP.equals(from)) {
            return "设备已报废，状态不能再变更";
        }
        if (STOP.equals(from) && SCRAP.equals(to)) {
            return "停机的设备不能直接报废，请先恢复在用或转维修";
        }
        if (REPAIR.equals(from) && STOP.equals(to)) {
            return "维修中的设备只能恢复在用，不能改为停机";
        }
        return "设备状态不能从「" + fromName + "」变更为「" + toName + "」";
    }
}
