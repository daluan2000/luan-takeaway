USE `pig`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 外卖平台角色扩展：商家 / 配送员 / 客户
-- =====================================================

-- ----------------------------
-- 地址主表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wm_address` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '创建用户ID(sys_user.user_id)',
  `province` varchar(64) DEFAULT NULL COMMENT '省',
  `city` varchar(64) DEFAULT NULL COMMENT '市',
  `district` varchar(64) DEFAULT NULL COMMENT '区县',
  `detail_address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wm_address_user` (`user_id`) USING BTREE,
  KEY `idx_wm_address_area` (`province`, `city`, `district`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖平台-地址表';

-- ----------------------------
-- 商家扩展表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wm_merchant_user_ext` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID(sys_user.user_id)',
  `merchant_name` varchar(128) DEFAULT NULL COMMENT '商家名称',
  `contact_name` varchar(64) DEFAULT NULL COMMENT '联系人',
  `store_address_id` bigint DEFAULT NULL COMMENT '门店地址ID(wm_address.id)',
  `business_status` char(1) DEFAULT '1' COMMENT '营业状态 0休息 1营业',
  `audit_status` char(1) DEFAULT '0' COMMENT '审核状态 0待审 1通过 2驳回',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_wm_merchant_user` (`user_id`) USING BTREE,
  KEY `idx_wm_merchant_status` (`business_status`, `audit_status`) USING BTREE,
  KEY `idx_wm_merchant_store_address` (`store_address_id`) USING BTREE,
  CONSTRAINT `fk_wm_merchant_store_address` FOREIGN KEY (`store_address_id`) REFERENCES `wm_address` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖平台-商家用户扩展表';


-- ----------------------------
-- 配送员扩展表（骑手）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wm_delivery_user_ext` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID(sys_user.user_id)',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `delivery_scope_km` decimal(5,2) DEFAULT '3.00' COMMENT '配送范围(公里)',
  `online_status` char(1) DEFAULT '0' COMMENT '在线状态 0离线 1在线',
  `employment_status` char(1) DEFAULT '1' COMMENT '在职状态 0离职 1在职',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_wm_delivery_user` (`user_id`) USING BTREE,
  KEY `idx_wm_delivery_status` (`online_status`, `employment_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖平台-配送员用户扩展表';

-- ----------------------------
-- 客户扩展表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wm_customer_user_ext` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID(sys_user.user_id)',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `default_address_id` bigint DEFAULT NULL COMMENT '默认收货地址ID(wm_address.id)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_wm_customer_user` (`user_id`) USING BTREE,
  KEY `idx_wm_customer_default_address` (`default_address_id`) USING BTREE,
  CONSTRAINT `fk_wm_customer_default_address` FOREIGN KEY (`default_address_id`) REFERENCES `wm_address` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖平台-客户用户扩展表';

-- ----------------------------
-- 菜品表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wm_dish` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `merchant_user_id` bigint NOT NULL COMMENT '商家用户ID(sys_user.user_id)',
  `dish_name` varchar(128) NOT NULL COMMENT '菜品名称',
  `dish_desc` varchar(255) DEFAULT NULL COMMENT '菜品描述',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `stock` int DEFAULT '0' COMMENT '库存',
  `sale_status` char(1) DEFAULT '1' COMMENT '上架状态 0下架 1上架',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wm_dish_merchant` (`merchant_user_id`) USING BTREE,
  KEY `idx_wm_dish_sale_status` (`sale_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖平台-菜品表';

-- ----------------------------
-- 订单主表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wm_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `customer_user_id` bigint NOT NULL COMMENT '客户用户ID(sys_user.user_id)',
  `merchant_user_id` bigint NOT NULL COMMENT '商家用户ID(sys_user.user_id)',
  `delivery_user_id` bigint DEFAULT NULL COMMENT '配送员用户ID(sys_user.user_id)',
  `delivery_address_id` bigint NOT NULL COMMENT '收货地址ID(wm_address.id)',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `order_status` char(1) NOT NULL COMMENT '订单状态 0待支付 1已支付 2已接单 3配送中 4已完成 5已取消',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `accept_time` datetime DEFAULT NULL COMMENT '商家接单时间',
  `delivery_start_time` datetime DEFAULT NULL COMMENT '开始配送时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_wm_order_no` (`order_no`) USING BTREE,
  KEY `idx_wm_order_customer` (`customer_user_id`) USING BTREE,
  KEY `idx_wm_order_merchant` (`merchant_user_id`) USING BTREE,
  KEY `idx_wm_order_delivery` (`delivery_user_id`) USING BTREE,
  KEY `idx_wm_order_status` (`order_status`) USING BTREE,
  KEY `idx_wm_order_delivery_address` (`delivery_address_id`) USING BTREE,
  CONSTRAINT `fk_wm_order_delivery_address` FOREIGN KEY (`delivery_address_id`) REFERENCES `wm_address` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖平台-订单主表';

-- ----------------------------
-- 订单明细表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wm_order_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID(wm_order.id)',
  `dish_id` bigint NOT NULL COMMENT '菜品ID(wm_dish.id)',
  `dish_name` varchar(128) NOT NULL COMMENT '下单时菜品名称快照',
  `dish_price` decimal(10,2) NOT NULL COMMENT '下单时单价快照',
  `quantity` int NOT NULL COMMENT '购买数量',
  `item_amount` decimal(10,2) NOT NULL COMMENT '明细金额',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wm_order_item_order` (`order_id`) USING BTREE,
  KEY `idx_wm_order_item_dish` (`dish_id`) USING BTREE,
  CONSTRAINT `fk_wm_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `wm_order` (`id`),
  CONSTRAINT `fk_wm_order_item_dish` FOREIGN KEY (`dish_id`) REFERENCES `wm_dish` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖平台-订单明细表';

-- ----------------------------
-- 支付记录表（模拟支付）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wm_order_pay` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID(wm_order.id)',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `pay_no` varchar(64) DEFAULT NULL COMMENT '支付流水号',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `pay_status` char(1) NOT NULL COMMENT '支付状态 0待支付 1支付成功 2支付失败',
  `pay_channel` char(1) DEFAULT '0' COMMENT '支付渠道 0模拟支付',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `fail_reason` varchar(255) DEFAULT NULL COMMENT '失败原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wm_order_pay_order` (`order_id`) USING BTREE,
  KEY `idx_wm_order_pay_status` (`pay_status`) USING BTREE,
  CONSTRAINT `fk_wm_order_pay_order` FOREIGN KEY (`order_id`) REFERENCES `wm_order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖平台-支付记录表';

-- ----------------------------
-- 配送单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `wm_delivery_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID(wm_order.id)',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `merchant_user_id` bigint NOT NULL COMMENT '商家用户ID(sys_user.user_id)',
  `delivery_user_id` bigint DEFAULT NULL COMMENT '配送员用户ID(sys_user.user_id)',
  `delivery_status` char(1) NOT NULL COMMENT '配送状态 0待接单 1已接单 2配送中 3已送达 4已取消',
  `accept_time` datetime DEFAULT NULL COMMENT '骑手接单时间',
  `pickup_time` datetime DEFAULT NULL COMMENT '取餐时间',
  `delivered_time` datetime DEFAULT NULL COMMENT '送达时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_wm_delivery_order_id` (`order_id`) USING BTREE,
  KEY `idx_wm_delivery_order_delivery_user` (`delivery_user_id`) USING BTREE,
  KEY `idx_wm_delivery_order_status` (`delivery_status`) USING BTREE,
  CONSTRAINT `fk_wm_delivery_order_order` FOREIGN KEY (`order_id`) REFERENCES `wm_order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖平台-配送单表';

-- ----------------------------
-- 初始化三种角色
-- ----------------------------
-- role_id 使用高位ID，避免与现有角色冲突
INSERT INTO `sys_role`
(`role_id`, `role_name`, `role_code`, `role_desc`, `create_by`, `update_by`, `create_time`, `update_time`, `del_flag`)
VALUES
(3000000000000000101, '商家', 'ROLE_MERCHANT', '外卖平台商家角色', 'admin', 'admin', NOW(), NOW(), '0')
ON DUPLICATE KEY UPDATE
`role_name` = VALUES(`role_name`),
`role_desc` = VALUES(`role_desc`),
`update_by` = 'admin',
`update_time` = NOW(),
`del_flag` = '0';


INSERT INTO `sys_role`
(`role_id`, `role_name`, `role_code`, `role_desc`, `create_by`, `update_by`, `create_time`, `update_time`, `del_flag`)
VALUES
(3000000000000000102, '客户', 'ROLE_CUSTOMER', '外卖平台客户角色', 'admin', 'admin', NOW(), NOW(), '0')
ON DUPLICATE KEY UPDATE
`role_name` = VALUES(`role_name`),
`role_desc` = VALUES(`role_desc`),
`update_by` = 'admin',
`update_time` = NOW(),
`del_flag` = '0';


INSERT INTO `sys_role`
(`role_id`, `role_name`, `role_code`, `role_desc`, `create_by`, `update_by`, `create_time`, `update_time`, `del_flag`)
VALUES
(3000000000000000103, '配送员', 'ROLE_DELIVERY', '外卖平台配送员角色', 'admin', 'admin', NOW(), NOW(), '0')
ON DUPLICATE KEY UPDATE
`role_name` = VALUES(`role_name`),
`role_desc` = VALUES(`role_desc`),
`update_by` = 'admin',
`update_time` = NOW(),
`del_flag` = '0';

-- ----------------------------
-- 初始化外卖平台菜单与按钮权限
-- menu_type: 0目录/菜单 1按钮
-- ----------------------------
INSERT INTO `sys_menu`
(`menu_id`, `name`, `en_name`, `permission`, `path`, `parent_id`, `icon`, `visible`, `sort_order`, `keep_alive`, `embedded`, `menu_type`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`)
VALUES
(3000000000000010000, '外卖平台', 'takeaway', NULL, '/takeaway', -1, 'iconfont icon-gongju', '1', 10, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),

(3000000000000010100, '商家中心', 'merchant', NULL, '/takeaway/merchant/index', 3000000000000010000, 'ele-Shop', '1', 1, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010101, '商家查询', NULL, 'wm_merchant_view', NULL, 3000000000000010100, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010102, '商家入驻', NULL, 'wm_merchant_apply', NULL, 3000000000000010100, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010103, '商家审核', NULL, 'wm_merchant_audit', NULL, 3000000000000010100, NULL, '1', 3, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010104, '商家编辑', NULL, 'wm_merchant_edit', NULL, 3000000000000010100, NULL, '1', 4, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010105, '营业状态', NULL, 'wm_merchant_business', NULL, 3000000000000010100, NULL, '1', 5, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010106, '商家接单', NULL, 'wm_merchant_accept', NULL, 3000000000000010100, NULL, '1', 6, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),

(3000000000000010200, '菜品管理', 'dish', NULL, '/takeaway/dish/index', 3000000000000010000, 'ele-Food', '1', 2, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010201, '菜品查询', NULL, 'wm_dish_view', NULL, 3000000000000010200, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010202, '菜品新增', NULL, 'wm_dish_add', NULL, 3000000000000010200, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010203, '菜品编辑', NULL, 'wm_dish_edit', NULL, 3000000000000010200, NULL, '1', 3, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010204, '菜品删除', NULL, 'wm_dish_del', NULL, 3000000000000010200, NULL, '1', 4, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010205, '菜品上架', NULL, 'wm_dish_sale_on', NULL, 3000000000000010200, NULL, '1', 5, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010206, '菜品下架', NULL, 'wm_dish_sale_off', NULL, 3000000000000010200, NULL, '1', 6, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),

(3000000000000010300, '订单中心', 'order', NULL, '/takeaway/order/index', 3000000000000010000, 'ele-List', '1', 3, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010301, '订单查询', NULL, 'wm_order_view', NULL, 3000000000000010300, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010302, '订单创建', NULL, 'wm_order_create', NULL, 3000000000000010300, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010303, '订单详情', NULL, 'wm_order_detail', NULL, 3000000000000010300, NULL, '1', 3, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010304, '订单取消', NULL, 'wm_order_cancel', NULL, 3000000000000010300, NULL, '1', 4, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),

(3000000000000010400, '配送中心', 'delivery', NULL, '/takeaway/delivery/index', 3000000000000010000, 'ele-Bicycle', '1', 4, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010401, '骑手信息', NULL, 'wm_delivery_rider', NULL, 3000000000000010400, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010402, '配送查询', NULL, 'wm_delivery_view', NULL, 3000000000000010400, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010403, '骑手接单', NULL, 'wm_delivery_accept', NULL, 3000000000000010400, NULL, '1', 3, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010404, '配送完成', NULL, 'wm_delivery_complete', NULL, 3000000000000010400, NULL, '1', 4, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),

(3000000000000010500, '支付管理', 'pay', NULL, '/takeaway/pay/index', 3000000000000010000, 'ele-Wallet', '1', 5, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010501, '支付查询', NULL, 'wm_pay_view', NULL, 3000000000000010500, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(3000000000000010502, '模拟支付', NULL, 'wm_pay_mock', NULL, 3000000000000010500, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`en_name` = VALUES(`en_name`),
`permission` = VALUES(`permission`),
`path` = VALUES(`path`),
`parent_id` = VALUES(`parent_id`),
`icon` = VALUES(`icon`),
`visible` = VALUES(`visible`),
`sort_order` = VALUES(`sort_order`),
`keep_alive` = VALUES(`keep_alive`),
`embedded` = VALUES(`embedded`),
`menu_type` = VALUES(`menu_type`),
`update_by` = 'admin',
`update_time` = NOW(),
`del_flag` = '0';

-- ----------------------------
-- 初始化外卖平台角色菜单权限
-- 角色：管理员(1)、商家(3000000000000000101)、客户(3000000000000000102)、配送员(3000000000000000103)
-- ----------------------------
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
-- 管理员：拥有外卖平台全部权限
(1, 3000000000000010000),
(1, 3000000000000010100),(1, 3000000000000010101),(1, 3000000000000010102),(1, 3000000000000010103),(1, 3000000000000010104),(1, 3000000000000010105),(1, 3000000000000010106),
(1, 3000000000000010200),(1, 3000000000000010201),(1, 3000000000000010202),(1, 3000000000000010203),(1, 3000000000000010204),(1, 3000000000000010205),(1, 3000000000000010206),
(1, 3000000000000010300),(1, 3000000000000010301),(1, 3000000000000010302),(1, 3000000000000010303),(1, 3000000000000010304),
(1, 3000000000000010400),(1, 3000000000000010401),(1, 3000000000000010402),(1, 3000000000000010403),(1, 3000000000000010404),
(1, 3000000000000010500),(1, 3000000000000010501),(1, 3000000000000010502),

-- 商家：商家中心 + 菜品管理 + 订单查看
(3000000000000000101, 3000000000000010000),
(3000000000000000101, 3000000000000010100),(3000000000000000101, 3000000000000010101),(3000000000000000101, 3000000000000010102),(3000000000000000101, 3000000000000010104),(3000000000000000101, 3000000000000010105),(3000000000000000101, 3000000000000010106),
(3000000000000000101, 3000000000000010200),(3000000000000000101, 3000000000000010201),(3000000000000000101, 3000000000000010202),(3000000000000000101, 3000000000000010203),(3000000000000000101, 3000000000000010204),(3000000000000000101, 3000000000000010205),(3000000000000000101, 3000000000000010206),
(3000000000000000101, 3000000000000010300),(3000000000000000101, 3000000000000010301),(3000000000000000101, 3000000000000010303),

-- 客户：订单中心 + 支付管理
(3000000000000000102, 3000000000000010000),
(3000000000000000102, 3000000000000010300),(3000000000000000102, 3000000000000010301),(3000000000000000102, 3000000000000010302),(3000000000000000102, 3000000000000010303),(3000000000000000102, 3000000000000010304),
(3000000000000000102, 3000000000000010500),(3000000000000000102, 3000000000000010501),(3000000000000000102, 3000000000000010502),

-- 配送员：配送中心 + 订单查看
(3000000000000000103, 3000000000000010000),
(3000000000000000103, 3000000000000010400),(3000000000000000103, 3000000000000010401),(3000000000000000103, 3000000000000010402),(3000000000000000103, 3000000000000010403),(3000000000000000103, 3000000000000010404),
(3000000000000000103, 3000000000000010300),(3000000000000000103, 3000000000000010301),(3000000000000000103, 3000000000000010303);

SET FOREIGN_KEY_CHECKS = 1;
