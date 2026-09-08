-- ----------------------------------------------------------------------------
-- 设备档案模块初始化脚本（增量执行，不影响已有数据）
-- 内容：1. 建表 t_sys_device   2. 菜单/按钮权限   3. 绑定到“管理员”角色
-- 说明：权限标识 gen:sysDevice:* 需与 SysDeviceController 中的 @RequiresPermissions 一致
-- ----------------------------------------------------------------------------

-- ----------------------------
-- Table structure for t_sys_device
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_device`;
CREATE TABLE `t_sys_device`  (
  `id` bigint(30) NOT NULL COMMENT '主键',
  `device_no` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '设备编号',
  `device_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备名称',
  `device_model` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '型号',
  `manufacturer` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '生产厂家',
  `production_date` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '出厂日期',
  `workshop` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所在车间',
  `principal` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '责任人',
  `status` int(11) NULL DEFAULT 0 COMMENT '状态(0在用 1停机 2维修 3报废)',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_no`(`device_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '设备档案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_sys_permission（设备管理目录 + 设备档案菜单 + 按钮权限）
-- 若重复执行，请先手动删除以下固定ID的权限记录
-- ----------------------------
INSERT INTO `t_sys_permission` (`id`, `name`, `descripion`, `url`, `is_blank`, `pid`, `perms`, `type`, `icon`, `order_num`, `visible`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
(631000000000000001, '设备管理', '设备管理', '', 0, 0, '', 0, 'layui-icon layui-icon-component', 60, 0, 'admin', NOW(), NULL, NULL, NULL),
(631000000000000002, '设备档案', '设备档案列表', '/SysDeviceController/view', 0, 631000000000000001, 'gen:sysDevice:view', 1, 'layui-icon layui-icon-component', 1, 0, 'admin', NOW(), NULL, NULL, NULL),
(631000000000000003, '设备列表', '设备列表查询', '/SysDeviceController/list', 0, 631000000000000002, 'gen:sysDevice:list', 2, '', 1, 0, 'admin', NOW(), NULL, NULL, NULL),
(631000000000000004, '设备新增', '设备新增', '/SysDeviceController/add', 0, 631000000000000002, 'gen:sysDevice:add', 2, '', 2, 0, 'admin', NOW(), NULL, NULL, NULL),
(631000000000000005, '设备修改', '设备修改', '/SysDeviceController/edit', 0, 631000000000000002, 'gen:sysDevice:edit', 2, '', 3, 0, 'admin', NOW(), NULL, NULL, NULL),
(631000000000000006, '设备删除', '设备删除', '/SysDeviceController/remove', 0, 631000000000000002, 'gen:sysDevice:remove', 2, '', 4, 0, 'admin', NOW(), NULL, NULL, NULL);

-- ----------------------------
-- Records of t_sys_permission_role（绑定到“管理员”角色 role_id=488243256161730560）
-- 若你的登录账号所属角色不是“管理员”，请登录系统后在“权限管理”中把“设备管理-设备档案”的
-- view/list/add/edit/remove 权限勾选给对应角色，或在下方补充绑定语句。
-- ----------------------------
INSERT INTO `t_sys_permission_role` (`id`, `role_id`, `permission_id`) VALUES
(631000000000000101, 488243256161730560, 631000000000000001),
(631000000000000102, 488243256161730560, 631000000000000002),
(631000000000000103, 488243256161730560, 631000000000000003),
(631000000000000104, 488243256161730560, 631000000000000004),
(631000000000000105, 488243256161730560, 631000000000000005),
(631000000000000106, 488243256161730560, 631000000000000006);

-- ----------------------------------------------------------------------------
-- （可选）若内置管理员账号(admin)仍未绑定任何角色，执行下面这句把它挂到“管理员”角色，
-- 否则请跳过。此语句仅用于全新库初始化场景，重复执行会新增一条冗余关联(无实际影响)。
-- ----------------------------------------------------------------------------
-- INSERT INTO `t_sys_role_user` (`id`, `sys_user_id`, `sys_role_id`) VALUES
-- (631000000000000201, 1, 488243256161730560);
