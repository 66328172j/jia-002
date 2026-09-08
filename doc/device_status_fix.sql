-- ----------------------------------------------------------------------------
-- 设备状态脏数据诊断与订正脚本（面向已上线的库，手工执行一次）
--
-- 背景：
--   设备状态应恒为 0在用 / 1停机 / 2维修 / 3报废，且“报废”是终态。
--   本脚本用于排查并修复两类历史脏数据：
--     1) status 为空(NULL)或非法值（如被清成 NULL、写进 0-3 以外的数）；
--        这类记录在旧代码里会被当“在用”继续流转，导致“报废后还能改回维修”等
--        看似状态机漏拦的现象；
--     2) 状态流转履历与档案当前状态矛盾（历史无状态机阶段乱改留下的）。
--
-- 建议执行顺序：先跑【1. 诊断】看清问题数据；确认后再执行【2. 订正】；最后用【3. 校验】核对。
-- 执行订正前请先备份（mysqldump t_sys_device / t_sys_device_log）。
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 1. 诊断
-- ----------------------------------------------------------------------------

-- ① 状态为空或非法的设备（状态栏不显示颜色块的就是这类）
SELECT id, device_no, status, update_by, update_time
FROM t_sys_device
WHERE status IS NULL OR status NOT IN (0, 1, 2, 3)
ORDER BY update_time DESC;

-- ② 非法流转履历：曾经从“报废(3)”流转去其它状态，或旧状态为空却流转到非“在用”
--    这些就是“报废被改回来”“空状态任意变”的直接证据
SELECT id, device_id, device_no, old_status, new_status, change_type, create_by, create_time
FROM t_sys_device_log
WHERE change_type = 2
  AND ( (old_status = 3 AND new_status <> 3)
     OR (old_status IS NULL AND new_status NOT IN (0)) )
ORDER BY create_time DESC;

-- ③ 档案当前状态与其最后一条状态履历(新增/流转)不一致的设备
--    注意：完全没有履历的旧设备也会出现在结果里，属正常，请人工分辨
SELECT d.id, d.device_no, d.status AS cur_status,
       l.new_status AS last_log_status, l.change_type, l.create_time
FROM t_sys_device d
LEFT JOIN t_sys_device_log l ON l.id = (
    SELECT ll.id FROM t_sys_device_log ll
    WHERE ll.device_id = d.id AND ll.change_type IN (1, 2)
    ORDER BY ll.create_time DESC, ll.id DESC
    LIMIT 1)
WHERE NOT (d.status <=> l.new_status);

-- ----------------------------------------------------------------------------
-- 2. 订正
-- ----------------------------------------------------------------------------

-- ① 空/非法状态统一修正为“在用(0)”
UPDATE t_sys_device
SET status = 0,
    update_by = 'system-fix',
    update_time = NOW()
WHERE status IS NULL OR status NOT IN (0, 1, 2, 3);

-- ② 收紧列约束：状态不允许为空，默认在用。杜绝脏数据再次出现。
ALTER TABLE `t_sys_device`
    MODIFY COLUMN `status` int(11) NOT NULL DEFAULT 0 COMMENT '状态(0在用 1停机 2维修 3报废)';

-- ③ 并发防覆盖所依赖的 version 列/基线迁移，请单独执行 doc/device_concurrency_fix.sql。
--    旧建表脚本 doc/device.sql 没有 version 列；缺列或列值全为 NULL 都会让乐观锁失效，
--    重新出现“后保存覆盖先保存”的问题。该脚本负责补列、回填 NULL 为 0、收紧非空约束。

-- ----------------------------------------------------------------------------
-- 3. 校验：以下查询应返回 0 行
-- ----------------------------------------------------------------------------
SELECT id, device_no, status FROM t_sys_device
WHERE status IS NULL OR status NOT IN (0, 1, 2, 3);

-- 校验：不应再存在从“报废”出发的流转履历（历史非法数据建议人工复核后自行清理/留痕）
SELECT id, device_no, old_status, new_status, create_time FROM t_sys_device_log
WHERE change_type = 2 AND old_status = 3 AND new_status <> 3;
