-- ----------------------------------------------------------------------------
-- 设备档案“并发编辑防覆盖”所依赖的 version 列迁移（面向已上线的库，手工执行一次）
--
-- 背景：
--   设备档案的编辑保存采用“版本号乐观锁”（t_sys_device.version）防止两台电脑同时编辑
--   同一台设备时互相覆盖。但旧建表脚本 doc/device.sql 里没有 version 列；部分线上表
--   要么缺列，要么列值全为 NULL。这两种情况都会让乐观锁失效，表现为：
--     A 把设备改成“停机”保存后，B 拿着保存前打开的旧页面再保存，状态又被覆盖回旧值，
--     且两次操作在履历里都留了记录，看不出哪次是对的。
--
-- 本脚本做三件事：
--   ① 补列：表里还没有 version 列时新增（int NOT NULL DEFAULT 0）；
--   ② 回填：已有列但历史数据为 NULL 的，统一置为 0，建立乐观锁基线；
--   ③ 约束：把列收紧为 NOT NULL DEFAULT 0，杜绝后续写入再次出现 NULL。
--
-- 执行前建议备份：mysqldump 你的库名 t_sys_device > t_sys_device_bak.sql
-- ----------------------------------------------------------------------------

-- ① 先检查当前表结构里是否已有 version 列
SHOW COLUMNS FROM `t_sys_device` LIKE 'version';
-- 期望：返回 1 行（Field = version）。若返回 0 行 → 表缺列，请执行下面的 ②。

-- ② 补列：仅当 ① 查询为空（缺列）时执行。
--    MySQL 不支持 ADD COLUMN IF NOT EXISTS，人工确认 ① 为空后再执行本句。
ALTER TABLE `t_sys_device`
    ADD COLUMN `version` int(11) NOT NULL DEFAULT 0
        COMMENT '乐观锁版本号，编辑保存时校验，防止多人同时操作互相覆盖'
        AFTER `status`;

-- ③ 回填历史 NULL 基线（列已存在、允许 NULL 且老数据为 NULL 时执行；幂等无副作用）
UPDATE `t_sys_device` SET `version` = 0 WHERE `version` IS NULL;

-- ④ 收紧列：允许 NULL 的旧结构改为 NOT NULL DEFAULT 0（已是该结构则无变化，幂等）
ALTER TABLE `t_sys_device`
    MODIFY COLUMN `version` int(11) NOT NULL DEFAULT 0
        COMMENT '乐观锁版本号，编辑保存时校验，防止多人同时操作互相覆盖';

-- ⑤ 校验：version 为 NULL 的行应为 0；且表结构中存在 version 字段
SELECT id, device_no, status, version FROM `t_sys_device` WHERE `version` IS NULL;
SHOW COLUMNS FROM `t_sys_device` LIKE 'version';

-- ----------------------------------------------------------------------------
-- 部署提示
--   代码侧已同步修改（TSysDevice.version、编辑页隐藏域、保存接口按 version CAS 更新）。
--   未执行本脚本前，含 version 的新代码在“列表 / 编辑打开”阶段就会报
--   unknown column 'version'——这是有意为之的快速失败：宁可明确报错，
--   也不要回到“缺列仍能保存、静默互相覆盖”的隐患状态。
-- ----------------------------------------------------------------------------
