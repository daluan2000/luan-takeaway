-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- 主机： luan-mysql:3306
-- 生成日期： 2026-03-29 04:17:37
-- 服务器版本： 8.0.32
-- PHP 版本： 8.3.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `luan`
--
CREATE DATABASE IF NOT EXISTS `luan` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `luan`;

-- --------------------------------------------------------

--
-- 表的结构 `sys_dict`
--

DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` bigint NOT NULL COMMENT '编号',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典类型',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注信息',
  `system_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '系统标志',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典表';

--
-- 转存表中的数据 `sys_dict`
--

INSERT INTO `sys_dict` (`id`, `dict_type`, `description`, `create_by`, `update_by`, `create_time`, `update_time`, `remarks`, `system_flag`, `del_flag`) VALUES
(1, 'log_type', '日志类型', ' ', ' ', '2019-03-19 11:06:44', '2019-03-19 11:06:44', '异常、正常', '1', '0'),
(10, 'param_type', '参数配置', ' ', ' ', '2019-04-29 18:20:47', '2019-04-29 18:20:47', '检索、原文、报表、安全、文档、消息、其他', '1', '0'),
(11, 'status_type', '租户状态', ' ', ' ', '2019-05-15 16:31:08', '2019-05-15 16:31:08', '租户状态', '1', '0'),
(12, 'dict_type', '字典类型', ' ', ' ', '2019-05-16 14:16:20', '2019-05-16 14:20:16', '系统类不能修改', '1', '0'),
(14, 'grant_types', '授权类型', ' ', ' ', '2019-08-13 07:34:10', '2019-08-13 07:34:10', NULL, '1', '0'),
(18, 'lock_flag', '用户状态', 'admin', ' ', '2023-02-01 16:55:31', NULL, NULL, '1', '0'),
(20, 'common_status', '通用状态', 'admin', ' ', '2023-02-09 11:02:08', NULL, NULL, '1', '0'),
(27, 'ds_type', '代码生成器支持的数据库类型', 'admin', ' ', '2023-03-12 09:57:59', NULL, NULL, '1', '0'),
(101, 'takeaway_merchant_business_status', '外卖-商家营业状态', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '外卖业务字典', '0', '0'),
(102, 'takeaway_merchant_audit_status', '外卖-商家审核状态', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '外卖业务字典', '0', '0'),
(103, 'takeaway_dish_sale_status', '外卖-菜品销售状态', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '外卖业务字典', '0', '0'),
(104, 'takeaway_order_status', '外卖-订单状态', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '外卖业务字典', '0', '0'),
(105, 'takeaway_pay_channel', '外卖-支付渠道', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '外卖业务字典', '0', '0'),
(106, 'takeaway_delivery_online_status', '外卖-骑手在线状态', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '外卖业务字典', '0', '0'),
(107, 'takeaway_delivery_employment_status', '外卖-骑手在职状态', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '外卖业务字典', '0', '0');

-- --------------------------------------------------------

--
-- 表的结构 `sys_dict_item`
--

DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item` (
  `id` bigint NOT NULL COMMENT '编号',
  `dict_id` bigint NOT NULL COMMENT '字典ID',
  `item_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典项值',
  `label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典项名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典类型',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典项描述',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序（升序）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注信息',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典项';

--
-- 转存表中的数据 `sys_dict_item`
--

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_value`, `label`, `dict_type`, `description`, `sort_order`, `create_by`, `update_by`, `create_time`, `update_time`, `remarks`, `del_flag`) VALUES
(1, 1, '9', '异常', 'log_type', '日志异常', 1, ' ', ' ', '2019-03-19 11:08:59', '2019-03-25 12:49:13', '', '0'),
(2, 1, '0', '正常', 'log_type', '日志正常', 0, ' ', ' ', '2019-03-19 11:09:17', '2019-03-25 12:49:18', '', '0'),
(25, 10, '1', '检索', 'param_type', '检索', 0, ' ', ' ', '2019-04-29 18:22:17', '2019-04-29 18:22:17', '检索', '0'),
(26, 10, '2', '原文', 'param_type', '原文', 0, ' ', ' ', '2019-04-29 18:22:27', '2019-04-29 18:22:27', '原文', '0'),
(27, 10, '3', '报表', 'param_type', '报表', 0, ' ', ' ', '2019-04-29 18:22:36', '2019-04-29 18:22:36', '报表', '0'),
(28, 10, '4', '安全', 'param_type', '安全', 0, ' ', ' ', '2019-04-29 18:22:46', '2019-04-29 18:22:46', '安全', '0'),
(29, 10, '5', '文档', 'param_type', '文档', 0, ' ', ' ', '2019-04-29 18:22:56', '2019-04-29 18:22:56', '文档', '0'),
(30, 10, '6', '消息', 'param_type', '消息', 0, ' ', ' ', '2019-04-29 18:23:05', '2019-04-29 18:23:05', '消息', '0'),
(31, 10, '9', '其他', 'param_type', '其他', 0, ' ', ' ', '2019-04-29 18:23:16', '2019-04-29 18:23:16', '其他', '0'),
(32, 10, '0', '默认', 'param_type', '默认', 0, ' ', ' ', '2019-04-29 18:23:30', '2019-04-29 18:23:30', '默认', '0'),
(33, 11, '0', '正常', 'status_type', '状态正常', 0, ' ', ' ', '2019-05-15 16:31:34', '2019-05-16 22:30:46', '状态正常', '0'),
(34, 11, '9', '冻结', 'status_type', '状态冻结', 1, ' ', ' ', '2019-05-15 16:31:56', '2019-05-16 22:30:50', '状态冻结', '0'),
(35, 12, '1', '系统类', 'dict_type', '系统类字典', 0, ' ', ' ', '2019-05-16 14:20:40', '2019-05-16 14:20:40', '不能修改删除', '0'),
(36, 12, '0', '业务类', 'dict_type', '业务类字典', 0, ' ', ' ', '2019-05-16 14:20:59', '2019-05-16 14:20:59', '可以修改', '0'),
(39, 14, 'password', '密码模式', 'grant_types', '支持oauth密码模式', 0, ' ', ' ', '2019-08-13 07:35:28', '2019-08-13 07:35:28', NULL, '0'),
(40, 14, 'authorization_code', '授权码模式', 'grant_types', 'oauth2 授权码模式', 1, ' ', ' ', '2019-08-13 07:36:07', '2019-08-13 07:36:07', NULL, '0'),
(41, 14, 'client_credentials', '客户端模式', 'grant_types', 'oauth2 客户端模式', 2, ' ', ' ', '2019-08-13 07:36:30', '2019-08-13 07:36:30', NULL, '0'),
(42, 14, 'refresh_token', '刷新模式', 'grant_types', 'oauth2 刷新token', 3, ' ', ' ', '2019-08-13 07:36:54', '2019-08-13 07:36:54', NULL, '0'),
(43, 14, 'implicit', '简化模式', 'grant_types', 'oauth2 简化模式', 4, ' ', ' ', '2019-08-13 07:39:32', '2019-08-13 07:39:32', NULL, '0'),
(57, 14, 'mobile', 'mobile', 'grant_types', '移动端登录', 5, 'admin', ' ', '2023-01-29 17:21:42', NULL, NULL, '0'),
(58, 18, '0', '有效', 'lock_flag', '有效', 0, 'admin', ' ', '2023-02-01 16:56:00', NULL, NULL, '0'),
(59, 18, '9', '禁用', 'lock_flag', '禁用', 1, 'admin', ' ', '2023-02-01 16:56:09', NULL, NULL, '0'),
(63, 20, 'false', '否', 'common_status', '否', 1, 'admin', ' ', '2023-02-09 11:02:39', NULL, NULL, '0'),
(64, 20, 'true', '是', 'common_status', '是', 2, 'admin', ' ', '2023-02-09 11:02:52', NULL, NULL, '0'),
(86, 27, 'mysql', 'mysql', 'ds_type', 'mysql', 0, 'admin', ' ', '2023-03-12 09:58:11', NULL, NULL, '0'),
(1001, 101, '1', '营业', 'takeaway_merchant_business_status', '商家正在营业', 1, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1002, 101, '0', '休息', 'takeaway_merchant_business_status', '商家暂停营业', 2, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1003, 102, '0', '待审', 'takeaway_merchant_audit_status', '待审核', 1, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1004, 102, '1', '通过', 'takeaway_merchant_audit_status', '审核通过', 2, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1005, 102, '2', '驳回', 'takeaway_merchant_audit_status', '审核驳回', 3, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1006, 103, '1', '上架', 'takeaway_dish_sale_status', '菜品可售', 1, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1007, 103, '0', '下架', 'takeaway_dish_sale_status', '菜品不可售', 2, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1008, 104, '0', '待支付', 'takeaway_order_status', '订单待支付', 1, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1009, 104, '1', '已支付', 'takeaway_order_status', '订单已支付', 2, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1010, 104, '2', '商家已接单', 'takeaway_order_status', '商家已接单', 3, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1011, 104, '3', '配送中', 'takeaway_order_status', '骑手配送中', 4, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1012, 104, '4', '已完成', 'takeaway_order_status', '订单完成', 5, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1013, 104, '5', '已取消', 'takeaway_order_status', '订单取消', 6, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1014, 105, '0', '模拟支付', 'takeaway_pay_channel', '模拟支付', 1, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1015, 106, '0', '离线', 'takeaway_delivery_online_status', '骑手离线', 1, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1016, 106, '1', '在线', 'takeaway_delivery_online_status', '骑手在线', 2, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1017, 107, '0', '离职', 'takeaway_delivery_employment_status', '骑手离职', 1, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0'),
(1018, 107, '1', '在职', 'takeaway_delivery_employment_status', '骑手在职', 2, 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', NULL, '0');

-- --------------------------------------------------------

--
-- 表的结构 `sys_file`
--

DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL COMMENT '编号',
  `file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件名',
  `bucket_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件存储桶名称',
  `original` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '原始文件名',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件类型',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `create_time` datetime DEFAULT NULL COMMENT '上传时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件管理表';

-- --------------------------------------------------------

--
-- 表的结构 `sys_log`
--

DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` bigint NOT NULL COMMENT '编号',
  `log_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '日志类型',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日志标题',
  `service_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '服务ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remote_addr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '远程地址',
  `user_agent` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户代理',
  `request_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '请求URI',
  `method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '请求方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '请求参数',
  `time` bigint DEFAULT NULL COMMENT '执行时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志',
  `exception` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '异常信息'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='日志表';

-- --------------------------------------------------------

--
-- 表的结构 `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '菜单名称',
  `en_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '英文名称',
  `permission` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识',
  `path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由路径',
  `parent_id` bigint DEFAULT NULL COMMENT '父菜单ID',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '菜单图标',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '是否可见，0隐藏，1显示',
  `sort_order` int DEFAULT '1' COMMENT '排序值，越小越靠前',
  `keep_alive` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '是否缓存，0否，1是',
  `embedded` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否内嵌，0否，1是',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '菜单类型，0目录，2菜单，1按钮',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志，0未删除，1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';

--
-- 转存表中的数据 `sys_menu`
--

INSERT INTO `sys_menu` (`menu_id`, `name`, `en_name`, `permission`, `path`, `parent_id`, `icon`, `visible`, `sort_order`, `keep_alive`, `embedded`, `menu_type`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) VALUES
(1000, '权限管理', 'authorization', NULL, '/admin', -1, 'iconfont icon-icon-', '1', 0, '0', '0', '0', '', '2018-09-28 08:29:53', 'admin', '2023-03-12 22:32:52', '0'),
(1100, '用户管理', 'user', NULL, '/admin/user/index', 1000, 'ele-User', '1', 1, '0', '0', '0', '', '2017-11-02 22:24:37', 'admin', '2023-07-05 10:28:22', '0'),
(1101, '用户新增', NULL, 'sys_user_add', NULL, 1100, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 09:52:09', ' ', '2021-05-25 03:12:55', '0'),
(1102, '用户修改', NULL, 'sys_user_edit', NULL, 1100, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 09:52:48', ' ', '2021-05-25 03:12:55', '0'),
(1103, '用户删除', NULL, 'sys_user_del', NULL, 1100, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 09:54:01', ' ', '2021-05-25 03:12:55', '0'),
(1104, '导入导出', NULL, 'sys_user_export', NULL, 1100, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 09:54:01', ' ', '2021-05-25 03:12:55', '0'),
(1200, '菜单管理', 'menu', NULL, '/admin/menu/index', 1000, 'iconfont icon-caidan', '1', 2, '0', '0', '0', '', '2017-11-08 09:57:27', 'admin', '2023-07-05 10:28:17', '0'),
(1201, '菜单新增', NULL, 'sys_menu_add', NULL, 1200, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 10:15:53', ' ', '2021-05-25 03:12:55', '0'),
(1202, '菜单修改', NULL, 'sys_menu_edit', NULL, 1200, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 10:16:23', ' ', '2021-05-25 03:12:55', '0'),
(1203, '菜单删除', NULL, 'sys_menu_del', NULL, 1200, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 10:16:43', ' ', '2021-05-25 03:12:55', '0'),
(1300, '角色管理', 'role', NULL, '/admin/role/index', 1000, 'iconfont icon-gerenzhongxin', '1', 3, '0', NULL, '0', '', '2017-11-08 10:13:37', 'admin', '2023-07-05 10:28:13', '0'),
(1301, '角色新增', NULL, 'sys_role_add', NULL, 1300, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 10:14:18', ' ', '2021-05-25 03:12:55', '0'),
(1302, '角色修改', NULL, 'sys_role_edit', NULL, 1300, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 10:14:41', ' ', '2021-05-25 03:12:55', '0'),
(1303, '角色删除', NULL, 'sys_role_del', NULL, 1300, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 10:14:59', ' ', '2021-05-25 03:12:55', '0'),
(1304, '分配权限', NULL, 'sys_role_perm', NULL, 1300, NULL, '1', 1, '0', NULL, '1', ' ', '2018-04-20 07:22:55', ' ', '2021-05-25 03:12:55', '0'),
(1305, '角色导入导出', NULL, 'sys_role_export', NULL, 1300, NULL, '1', 4, '0', NULL, '1', ' ', '2022-03-26 15:54:34', ' ', NULL, '0'),
(2000, '系统管理', 'system', NULL, '/system', -1, 'iconfont icon-quanjushezhi_o', '1', 1, '0', NULL, '0', '', '2017-11-07 20:56:00', 'admin', '2023-07-05 10:27:58', '0'),
(2001, '日志管理', 'log', NULL, '/admin/logs', 2000, 'ele-Cloudy', '1', 0, '0', '0', '0', 'admin', '2023-03-02 12:26:42', 'admin', '2026-03-11 20:12:18', '1'),
(2100, '操作日志', 'operation', NULL, '/admin/log/index', 2000, 'iconfont icon-jinridaiban', '1', 2, '0', '0', '0', '', '2017-11-20 14:06:22', 'admin', '2026-03-11 20:11:48', '0'),
(2101, '日志删除', NULL, 'sys_log_del', NULL, 2100, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-20 20:37:37', ' ', '2021-05-25 03:12:55', '0'),
(2102, '导入导出', NULL, 'sys_log_export', NULL, 2100, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 09:54:01', ' ', '2021-05-25 03:12:55', '0'),
(2200, '字典管理', 'dict', NULL, '/admin/dict/index', 2000, 'iconfont icon-zhongduancanshuchaxun', '1', 6, '0', NULL, '0', '', '2017-11-29 11:30:52', 'admin', '2026-03-11 20:51:07', '0'),
(2201, '字典删除', NULL, 'sys_dict_del', NULL, 2200, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-29 11:30:11', 'admin', '2026-03-11 20:51:01', '0'),
(2202, '字典新增', NULL, 'sys_dict_add', NULL, 2200, NULL, '1', 1, '0', NULL, '1', ' ', '2018-05-11 22:34:55', 'admin', '2026-03-11 20:51:02', '0'),
(2203, '字典修改', NULL, 'sys_dict_edit', NULL, 2200, NULL, '1', 1, '0', NULL, '1', ' ', '2018-05-11 22:36:03', 'admin', '2026-03-11 20:50:58', '0'),
(2210, '参数管理', 'parameter', NULL, '/admin/param/index', 2000, 'iconfont icon-wenducanshu-05', '1', 7, '0', NULL, '0', '', '2019-04-29 22:16:50', 'admin', '2026-03-14 16:22:44', '0'),
(2211, '参数新增', NULL, 'sys_syspublicparam_add', NULL, 2210, NULL, '1', 1, '0', NULL, '1', ' ', '2019-04-29 22:17:36', 'admin', '2026-03-11 20:51:15', '0'),
(2212, '参数删除', NULL, 'sys_syspublicparam_del', NULL, 2210, NULL, '1', 1, '0', NULL, '1', ' ', '2019-04-29 22:17:55', 'admin', '2026-03-11 20:51:13', '0'),
(2213, '参数编辑', NULL, 'sys_syspublicparam_edit', NULL, 2210, NULL, '1', 1, '0', NULL, '1', ' ', '2019-04-29 22:18:14', 'admin', '2026-03-11 20:51:12', '0'),
(2400, '终端管理', 'client', NULL, '/admin/client/index', 2000, 'iconfont icon-gongju', '1', 9, '1', NULL, '0', '', '2018-01-20 13:17:19', 'admin', '2026-03-11 20:51:27', '1'),
(2401, '客户端新增', NULL, 'sys_client_add', NULL, 2400, '1', '1', 1, '0', NULL, '1', ' ', '2018-05-15 21:35:18', 'admin', '2026-03-11 20:51:25', '1'),
(2402, '客户端修改', NULL, 'sys_client_edit', NULL, 2400, NULL, '1', 1, '0', NULL, '1', ' ', '2018-05-15 21:37:06', 'admin', '2026-03-11 20:51:22', '1'),
(2403, '客户端删除', NULL, 'sys_client_del', NULL, 2400, NULL, '1', 1, '0', NULL, '1', ' ', '2018-05-15 21:39:16', 'admin', '2026-03-11 20:51:23', '1'),
(2600, '令牌管理', 'token', NULL, '/admin/token/index', 2000, 'ele-Key', '1', 11, '0', NULL, '0', '', '2018-09-04 05:58:41', 'admin', '2023-02-16 15:28:28', '0'),
(2601, '令牌删除', NULL, 'sys_token_del', NULL, 2600, NULL, '1', 1, '0', NULL, '1', ' ', '2018-09-04 05:59:50', ' ', '2020-03-24 08:57:24', '0'),
(2906, '文件管理', 'file', NULL, '/admin/file/index', 2000, 'ele-Files', '1', 6, '0', NULL, '0', '', '2019-06-25 12:44:46', 'admin', '2023-02-16 15:24:42', '0'),
(2907, '删除文件', NULL, 'sys_file_del', NULL, 2906, NULL, '1', 1, '0', NULL, '1', ' ', '2019-06-25 13:41:41', ' ', '2020-03-24 08:58:42', '0'),
(2029765248645763074, '商家中心', 'merchant_center', NULL, '/merchant', -1, 'ele-Briefcase', '1', 10, '0', NULL, '0', 'admin', '2026-03-06 11:45:21', 'admin', '2026-03-06 11:46:00', '0'),
(2029765757146402817, '商家信息', '', NULL, '/merchant/info/index', 2029765248645763074, 'ele-Coin', '1', 0, '0', NULL, '0', 'admin', '2026-03-06 11:47:22', 'admin', '2026-03-07 21:12:16', '0'),
(2029766347125592065, '接单管理', 'merchant_order_manage', NULL, '/merchant/order/index', 2029765248645763074, 'iconfont icon-bolangnengshiyanchang', '1', 1, '0', NULL, '0', 'admin', '2026-03-06 11:49:43', 'admin', '2026-03-07 21:12:27', '0'),
(2029766829457969154, '菜品管理', NULL, NULL, '/merchant/dish/index', 2029765248645763074, 'ele-Bowl', '1', 2, '0', NULL, '0', 'admin', '2026-03-06 11:51:38', 'admin', '2026-03-06 19:56:33', '0'),
(2029767283445239809, '顾客中心', 'customer_center', NULL, '/customer', -1, 'ele-Avatar', '1', 11, '0', '0', '0', 'admin', '2026-03-06 11:53:26', 'admin', '2026-03-07 21:11:20', '0'),
(2029767684160655361, '顾客信息', NULL, NULL, '/customer/info/index', 2029767283445239809, 'iconfont icon-zhongduancanshuchaxun', '1', 0, '0', '0', '0', 'admin', '2026-03-06 11:55:02', 'admin', '2026-03-07 21:13:22', '0'),
(2029768204761862146, '订单管理', NULL, NULL, '/customer/order/index', 2029767283445239809, 'iconfont icon-bolangneng', '1', 2, '0', '0', '0', 'admin', '2026-03-06 11:57:06', 'admin', '2026-03-06 19:38:00', '0'),
(2029768804014657538, '菜品选取', NULL, NULL, '/customer/select/index', 2029767283445239809, 'iconfont icon-jiliandongxuanzeqi', '1', 1, '0', '0', '0', 'admin', '2026-03-06 11:59:29', 'admin', '2026-03-06 19:37:29', '0'),
(2029769996618862594, '骑手中心', NULL, NULL, '/delivery', -1, 'ele-Van', '1', 12, '0', '0', '0', 'admin', '2026-03-06 12:04:13', 'admin', '2026-03-06 13:19:56', '0'),
(2029770150633705474, '骑手信息', NULL, NULL, '/delivery/info/index', 2029769996618862594, 'ele-Bicycle', '1', 0, '0', '0', '0', 'admin', '2026-03-06 12:04:50', 'admin', '2026-03-06 19:56:00', '0'),
(2029770661516709889, '配送接单', NULL, NULL, '/delivery/order/index', 2029769996618862594, 'ele-Checked', '1', 1, '0', '0', '0', 'admin', '2026-03-06 12:06:52', 'admin', '2026-03-08 13:19:03', '0'),
(2029882213729284097, '地址管理', NULL, NULL, '/address/index', -1, 'ele-AddLocation', '1', 8, '0', '0', '0', 'admin', '2026-03-06 19:30:08', 'admin', '2026-03-06 19:55:46', '0'),
(2030513825756209153, '配送管理', NULL, NULL, '/delivery/send/index', 2029769996618862594, 'ele-ChromeFilled', '1', 2, '0', '0', '0', 'admin', '2026-03-08 13:19:56', 'admin', '2026-03-08 13:21:13', '0'),
(2031100000000001001, '地址新增', NULL, 'wm_address_add', NULL, 2029882213729284097, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001002, '地图选点新增', NULL, 'wm_address_add_map', NULL, 2029882213729284097, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001003, '地址编辑', NULL, 'wm_address_edit', NULL, 2029882213729284097, NULL, '1', 3, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001004, '地址删除', NULL, 'wm_address_del', NULL, 2029882213729284097, NULL, '1', 4, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001005, '商家信息新增', NULL, 'wm_merchant_info_add', NULL, 2029765757146402817, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001006, '商家信息编辑', NULL, 'wm_merchant_info_edit', NULL, 2029765757146402817, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001007, '商家菜品新增', NULL, 'wm_merchant_dish_add', NULL, 2029766829457969154, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001008, '商家菜品编辑', NULL, 'wm_merchant_dish_edit', NULL, 2029766829457969154, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001009, '商家菜品删除', NULL, 'wm_merchant_dish_del', NULL, 2029766829457969154, NULL, '1', 3, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001010, '商家菜品上架', NULL, 'wm_merchant_dish_sale_on', NULL, 2029766829457969154, NULL, '1', 4, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001011, '商家菜品下架', NULL, 'wm_merchant_dish_sale_off', NULL, 2029766829457969154, NULL, '1', 5, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001012, '商家订单接单', NULL, 'wm_merchant_order_accept', NULL, 2029766347125592065, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001013, '顾客信息新增', NULL, 'wm_customer_info_add', NULL, 2029767684160655361, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001014, '顾客信息编辑', NULL, 'wm_customer_info_edit', NULL, 2029767684160655361, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001015, '顾客下单', NULL, 'wm_customer_order_create', NULL, 2029768804014657538, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001016, '顾客订单支付', NULL, 'wm_customer_order_pay', NULL, 2029768204761862146, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001017, '顾客订单取消', NULL, 'wm_customer_order_cancel', NULL, 2029768204761862146, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001018, '骑手信息新增', NULL, 'wm_delivery_info_add', NULL, 2029770150633705474, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001019, '骑手信息编辑', NULL, 'wm_delivery_info_edit', NULL, 2029770150633705474, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001020, '骑手订单接单', NULL, 'wm_delivery_order_accept', NULL, 2029770661516709889, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2031100000000001021, '骑手配送完成', NULL, 'wm_delivery_order_finish', NULL, 2030513825756209153, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-11 21:00:00', 'admin', NULL, '0'),
(2032730947093729281, '智能点餐', NULL, NULL, '/customer/ai-order/index', 2029767283445239809, 'ele-Sunrise', '1', 0, '0', '0', '0', 'admin', '2026-03-14 16:09:59', NULL, NULL, '0');

-- --------------------------------------------------------

--
-- 表的结构 `sys_oauth_client_details`
--

DROP TABLE IF EXISTS `sys_oauth_client_details`;
CREATE TABLE `sys_oauth_client_details` (
  `id` bigint NOT NULL COMMENT 'ID',
  `client_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '客户端ID',
  `resource_ids` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资源ID集合',
  `client_secret` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户端秘钥',
  `scope` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '授权范围',
  `authorized_grant_types` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '授权类型',
  `web_server_redirect_uri` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '回调地址',
  `authorities` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限集合',
  `access_token_validity` int DEFAULT NULL COMMENT '访问令牌有效期（秒）',
  `refresh_token_validity` int DEFAULT NULL COMMENT '刷新令牌有效期（秒）',
  `additional_information` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '附加信息',
  `autoapprove` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '自动授权',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='终端信息表';

--
-- 转存表中的数据 `sys_oauth_client_details`
--

INSERT INTO `sys_oauth_client_details` (`id`, `client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`, `del_flag`, `create_by`, `update_by`, `create_time`, `update_time`) VALUES
(1, 'app', NULL, 'app', 'server', 'password,refresh_token,authorization_code,client_credentials,mobile', 'http://localhost:4040/sso1/login,http://localhost:4041/sso1/login,http://localhost:8080/renren-admin/sys/oauth2-sso,http://localhost:8090/sys/oauth2-sso', NULL, 43200, 2592001, '{\"enc_flag\":\"1\",\"captcha_flag\":\"1\",\"online_quantity\":\"1\"}', 'true', '0', '', 'admin', NULL, '2023-02-09 13:54:54'),
(2, 'daemon', NULL, 'daemon', 'server', 'password,refresh_token', NULL, NULL, 43200, 2592001, '{\"enc_flag\":\"1\",\"captcha_flag\":\"1\"}', 'true', '0', ' ', ' ', NULL, NULL),
(3, 'gen', NULL, 'gen', 'server', 'password,refresh_token', NULL, NULL, 43200, 2592001, '{\"enc_flag\":\"1\",\"captcha_flag\":\"1\"}', 'true', '0', ' ', ' ', NULL, NULL),
(4, 'mp', NULL, 'mp', 'server', 'password,refresh_token', NULL, NULL, 43200, 2592001, '{\"enc_flag\":\"1\",\"captcha_flag\":\"1\"}', 'true', '0', ' ', ' ', NULL, NULL),
(5, 'pig', NULL, 'pig', 'server', 'password,refresh_token,authorization_code,client_credentials,mobile', 'http://localhost:4040/sso1/login,http://localhost:4041/sso1/login,http://localhost:8080/renren-admin/sys/oauth2-sso,http://localhost:8090/sys/oauth2-sso', NULL, 43200, 2592001, '{\"enc_flag\":\"1\",\"captcha_flag\":\"1\",\"online_quantity\":\"1\"}', 'false', '0', '', 'admin', NULL, '2023-03-08 11:32:41'),
(6, 'test', NULL, 'test', 'server', 'password,refresh_token', NULL, NULL, 43200, 2592001, '{ \"enc_flag\":\"1\",\"captcha_flag\":\"0\"}', 'true', '0', ' ', ' ', NULL, NULL),
(7, 'social', NULL, 'social', 'server', 'password,refresh_token,mobile', NULL, NULL, 43200, 2592001, '{ \"enc_flag\":\"0\",\"captcha_flag\":\"0\"}', 'true', '0', ' ', ' ', NULL, NULL);

-- --------------------------------------------------------

--
-- 表的结构 `sys_public_param`
--

DROP TABLE IF EXISTS `sys_public_param`;
CREATE TABLE `sys_public_param` (
  `public_id` bigint NOT NULL COMMENT '编号',
  `public_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
  `public_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '键',
  `public_value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '值',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态，0禁用，1启用',
  `validate_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '校验码',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `public_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '类型，0未知，1系统，2业务',
  `system_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '系统标识，0非系统，1系统',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记，0未删除，1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公共参数配置表';

--
-- 转存表中的数据 `sys_public_param`
--

INSERT INTO `sys_public_param` (`public_id`, `public_name`, `public_key`, `public_value`, `status`, `validate_code`, `create_by`, `update_by`, `create_time`, `update_time`, `public_type`, `system_flag`, `del_flag`) VALUES
(9, '注册用户默认角色', 'USER_DEFAULT_ROLE', 'GENERAL_USER', '0', NULL, ' ', ' ', '2022-03-31 16:52:24', NULL, '2', '1', '0'),
(1001, '外卖订单自动取消时长(毫秒)', 'TAKEAWAY_ORDER_AUTO_CANCEL_MS', '600000', '0', '^\\d+$', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '2', '0', '0'),
(1002, '外卖骑手抢单锁时长(秒)', 'TAKEAWAY_ORDER_DELIVERY_LOCK_SECONDS', '10', '0', '^\\d+$', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '2', '0', '0'),
(1003, '外卖菜品异步落库DB锁等待(秒)', 'TAKEAWAY_DISH_DB_LOCK_WAIT_TIMEOUT_SECONDS', '3', '0', '^\\d+$', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '2', '0', '0'),
(1004, '外卖商家自动审核最小延时(秒)', 'TAKEAWAY_MERCHANT_AUTO_AUDIT_DELAY_MIN_SECONDS', '3', '0', '^\\d+$', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '2', '0', '0'),
(1005, '外卖商家自动审核最大延时(秒)', 'TAKEAWAY_MERCHANT_AUTO_AUDIT_DELAY_MAX_SECONDS', '10', '0', '^\\d+$', 'admin', 'admin', '2026-03-14 18:00:00', '2026-03-14 18:00:00', '2', '0', '0');

-- --------------------------------------------------------

--
-- 表的结构 `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `role_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '角色名称',
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '角色编码',
  `role_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '角色描述',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记，0未删除，1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统角色表';

--
-- 转存表中的数据 `sys_role`
--

INSERT INTO `sys_role` (`role_id`, `role_name`, `role_code`, `role_desc`, `create_by`, `update_by`, `create_time`, `update_time`, `del_flag`) VALUES
(1, '管理员', 'ROLE_ADMIN', '管理员', '', 'admin', '2017-10-29 15:45:51', '2023-07-07 14:55:07', '0'),
(2, '普通用户', 'GENERAL_USER', '普通用户', '', 'admin', '2022-03-31 17:03:15', '2023-04-03 02:28:51', '0'),
(3000000000000000101, '商家', 'ROLE_MERCHANT', '外卖平台商家角色', 'admin', 'admin', '2026-03-05 03:49:34', '2026-03-05 03:49:34', '0'),
(3000000000000000102, '客户', 'ROLE_CUSTOMER', '外卖平台客户角色', 'admin', 'admin', '2026-03-05 03:49:34', '2026-03-05 03:49:34', '0'),
(3000000000000000103, '配送员', 'ROLE_DELIVERY', '外卖平台配送员角色', 'admin', 'admin', '2026-03-05 03:49:34', '2026-03-05 03:49:34', '0');

-- --------------------------------------------------------

--
-- 表的结构 `sys_role_menu`
--

DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单表';

--
-- 转存表中的数据 `sys_role_menu`
--

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1000),
(1, 1100),
(1, 1101),
(1, 1102),
(1, 1103),
(1, 1104),
(1, 1200),
(1, 1201),
(1, 1202),
(1, 1203),
(1, 1300),
(1, 1301),
(1, 1302),
(1, 1303),
(1, 1304),
(1, 1305),
(1, 2000),
(1, 2100),
(1, 2101),
(1, 2102),
(1, 2200),
(1, 2201),
(1, 2202),
(1, 2203),
(1, 2210),
(1, 2211),
(1, 2212),
(1, 2213),
(1, 2600),
(1, 2601),
(1, 2906),
(1, 2907),
(2, 2029882213729284097),
(2, 2031100000000001001),
(2, 2031100000000001002),
(2, 2031100000000001003),
(2, 2031100000000001004),
(3000000000000000101, 2029765248645763074),
(3000000000000000101, 2029765757146402817),
(3000000000000000101, 2029766347125592065),
(3000000000000000101, 2029766829457969154),
(3000000000000000101, 2029882213729284097),
(3000000000000000101, 2031100000000001001),
(3000000000000000101, 2031100000000001002),
(3000000000000000101, 2031100000000001003),
(3000000000000000101, 2031100000000001004),
(3000000000000000101, 2031100000000001005),
(3000000000000000101, 2031100000000001006),
(3000000000000000101, 2031100000000001007),
(3000000000000000101, 2031100000000001008),
(3000000000000000101, 2031100000000001009),
(3000000000000000101, 2031100000000001010),
(3000000000000000101, 2031100000000001011),
(3000000000000000101, 2031100000000001012),
(3000000000000000102, 2029767283445239809),
(3000000000000000102, 2029767684160655361),
(3000000000000000102, 2029768204761862146),
(3000000000000000102, 2029768804014657538),
(3000000000000000102, 2029882213729284097),
(3000000000000000102, 2031100000000001001),
(3000000000000000102, 2031100000000001002),
(3000000000000000102, 2031100000000001003),
(3000000000000000102, 2031100000000001004),
(3000000000000000102, 2031100000000001013),
(3000000000000000102, 2031100000000001014),
(3000000000000000102, 2031100000000001015),
(3000000000000000102, 2031100000000001016),
(3000000000000000102, 2031100000000001017),
(3000000000000000102, 2032730947093729281),
(3000000000000000103, 2029769996618862594),
(3000000000000000103, 2029770150633705474),
(3000000000000000103, 2029770661516709889),
(3000000000000000103, 2029882213729284097),
(3000000000000000103, 2030513825756209153),
(3000000000000000103, 2031100000000001001),
(3000000000000000103, 2031100000000001002),
(3000000000000000103, 2031100000000001003),
(3000000000000000103, 2031100000000001004),
(3000000000000000103, 2031100000000001018),
(3000000000000000103, 2031100000000001019),
(3000000000000000103, 2031100000000001020),
(3000000000000000103, 2031100000000001021);

-- --------------------------------------------------------

--
-- 表的结构 `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '密码',
  `salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '盐值',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '电话号码',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '昵称',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '姓名',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱地址',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `lock_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '锁定标记，0未锁定，9已锁定',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记，0未删除，1已删除',
  `wx_openid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信登录openId',
  `mini_openid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '小程序openId',
  `qq_openid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'QQ openId',
  `gitee_login` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '码云标识',
  `osc_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '开源中国标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

--
-- 转存表中的数据 `sys_user`
--

INSERT INTO `sys_user` (`user_id`, `username`, `password`, `salt`, `phone`, `avatar`, `nickname`, `name`, `email`, `create_by`, `update_by`, `create_time`, `update_time`, `lock_flag`, `del_flag`, `wx_openid`, `mini_openid`, `qq_openid`, `gitee_login`, `osc_id`) VALUES
(1, 'admin', '$2a$10$c/Ae0pRjJtMZg3BnvVpO.eIK6WYWVbKTzqgdy3afR7w.vd.xi3Mgy', '', '17034642999', '/api/admin/sys-file/local/a3c603a1edac40c3a048d93bc57fdd71.jpg', '管理员', '管理员', 'pig4cloud1@qq.com', ' ', 'admin', '2018-04-20 07:15:18', '2026-03-15 17:33:31', '0', '0', NULL, 'oBxPy5E-v82xWGsfzZVzkD3wEX64', NULL, 'log4j', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色表';

--
-- 转存表中的数据 `sys_user_role`
--

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2033114242394513410, 3000000000000000101),
(2033114536197120002, 3000000000000000101),
(2033114675489955841, 3000000000000000101),
(2033114817374871554, 3000000000000000101),
(2033114952838307841, 3000000000000000101),
(2033115092332470274, 3000000000000000101),
(2033115310725685250, 3000000000000000102),
(2033115459845775362, 3000000000000000103);

-- --------------------------------------------------------

--
-- 表的结构 `wm_address`
--

DROP TABLE IF EXISTS `wm_address`;
CREATE TABLE `wm_address` (
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外卖平台-地址表';

-- --------------------------------------------------------

--
-- 表的结构 `wm_customer_user_ext`
--

DROP TABLE IF EXISTS `wm_customer_user_ext`;
CREATE TABLE `wm_customer_user_ext` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID(sys_user.user_id)',
  `real_name` varchar(64) DEFAULT NULL COMMENT '真实姓名',
  `default_address_id` bigint DEFAULT NULL COMMENT '默认收货地址ID(wm_address.id)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外卖平台-客户用户扩展表';

-- --------------------------------------------------------

--
-- 表的结构 `wm_delivery_order`
--

DROP TABLE IF EXISTS `wm_delivery_order`;
CREATE TABLE `wm_delivery_order` (
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外卖平台-配送单表';

-- --------------------------------------------------------

--
-- 表的结构 `wm_delivery_user_ext`
--

DROP TABLE IF EXISTS `wm_delivery_user_ext`;
CREATE TABLE `wm_delivery_user_ext` (
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外卖平台-配送员用户扩展表';

-- --------------------------------------------------------

--
-- 表的结构 `wm_dish`
--

DROP TABLE IF EXISTS `wm_dish`;
CREATE TABLE `wm_dish` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `merchant_user_id` bigint NOT NULL COMMENT '商家用户ID(sys_user.user_id)',
  `dish_image` varchar(512) DEFAULT NULL COMMENT '菜品图片URL',
  `dish_name` varchar(128) NOT NULL COMMENT '菜品名称',
  `dish_desc` varchar(255) DEFAULT NULL COMMENT '菜品描述',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `stock` int DEFAULT '0' COMMENT '库存',
  `sale_status` char(1) DEFAULT '1' COMMENT '上架状态 0下架 1上架',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外卖平台-菜品表';

-- --------------------------------------------------------

--
-- 表的结构 `wm_dish_knowledge_doc`
--

DROP TABLE IF EXISTS `wm_dish_knowledge_doc`;
CREATE TABLE `wm_dish_knowledge_doc` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID(wm_dish.id)',
  `category` varchar(32) DEFAULT NULL COMMENT '主类别',
  `spicy` tinyint(1) DEFAULT NULL COMMENT '是否辣',
  `spicy_level` int DEFAULT NULL COMMENT '辣度等级',
  `light_taste` tinyint(1) DEFAULT NULL COMMENT '是否清淡',
  `oily` tinyint(1) DEFAULT NULL COMMENT '是否油腻',
  `soup_based` tinyint(1) DEFAULT NULL COMMENT '是否汤类',
  `vegetarian` tinyint(1) DEFAULT NULL COMMENT '是否素食',
  `calories` int DEFAULT NULL COMMENT '热量kcal',
  `protein` int DEFAULT NULL COMMENT '蛋白质g',
  `fat` int DEFAULT NULL COMMENT '脂肪g',
  `carbohydrate` int DEFAULT NULL COMMENT '碳水g',
  `meal_time` json DEFAULT NULL COMMENT '适用餐段JSON数组',
  `portion_size` varchar(16) DEFAULT NULL COMMENT '分量',
  `tags` json DEFAULT NULL COMMENT '标签JSON数组',
  `suitable_scenes` json DEFAULT NULL COMMENT '推荐场景JSON数组',
  `avoid_scenes` json DEFAULT NULL COMMENT '避免场景JSON数组',
  `suitable_people` json DEFAULT NULL COMMENT '适用人群JSON数组',
  `embedding_text` text COMMENT 'embedding输入文本',
  `flavor_description` varchar(255) DEFAULT NULL COMMENT '风味描述',
  `llm_summary` text COMMENT '知识摘要',
  `recommendation_reason` text COMMENT '推荐理由',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外卖平台-菜品知识文档';

-- --------------------------------------------------------

--
-- 表的结构 `wm_merchant_user_ext`
--

DROP TABLE IF EXISTS `wm_merchant_user_ext`;
CREATE TABLE `wm_merchant_user_ext` (
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外卖平台-商家用户扩展表';

-- --------------------------------------------------------

--
-- 表的结构 `wm_order`
--

DROP TABLE IF EXISTS `wm_order`;
CREATE TABLE `wm_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `customer_user_id` bigint NOT NULL COMMENT '客户用户ID(sys_user.user_id)',
  `merchant_user_id` bigint NOT NULL COMMENT '商家用户ID(sys_user.user_id)',
  `delivery_user_id` bigint DEFAULT NULL COMMENT '配送员用户ID(sys_user.user_id)',
  `delivery_address_id` bigint NOT NULL COMMENT '收货地址ID(wm_address.id)',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `order_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单状态 0待支付 1已支付（未接单） 2已接单（未配送） 3配送中 4已完成 5已取消',
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外卖平台-订单主表';

-- --------------------------------------------------------

--
-- 表的结构 `wm_order_item`
--

DROP TABLE IF EXISTS `wm_order_item`;
CREATE TABLE `wm_order_item` (
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外卖平台-订单明细表';

-- --------------------------------------------------------

--
-- 表的结构 `wm_order_pay`
--

DROP TABLE IF EXISTS `wm_order_pay`;
CREATE TABLE `wm_order_pay` (
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记 0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外卖平台-支付记录表';

--
-- 转储表的索引
--

--
-- 表的索引 `sys_dict`
--
ALTER TABLE `sys_dict`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_sys_dict_type` (`dict_type`),
  ADD KEY `idx_sys_dict_del_flag` (`del_flag`);

--
-- 表的索引 `sys_dict_item`
--
ALTER TABLE `sys_dict_item`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_sys_dict_item_dict_id` (`dict_id`),
  ADD KEY `idx_sys_dict_item_dict_type` (`dict_type`),
  ADD KEY `idx_sys_dict_item_del_flag` (`del_flag`);

--
-- 表的索引 `sys_file`
--
ALTER TABLE `sys_file`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_sys_file_create_by` (`create_by`),
  ADD KEY `idx_sys_file_del_flag` (`del_flag`);

--
-- 表的索引 `sys_log`
--
ALTER TABLE `sys_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_sys_log_create_by` (`create_by`),
  ADD KEY `idx_sys_log_create_time` (`create_time`),
  ADD KEY `idx_sys_log_del_flag` (`del_flag`);

--
-- 表的索引 `sys_menu`
--
ALTER TABLE `sys_menu`
  ADD PRIMARY KEY (`menu_id`),
  ADD KEY `idx_sys_menu_parent_id` (`parent_id`),
  ADD KEY `idx_sys_menu_del_flag` (`del_flag`);

--
-- 表的索引 `sys_oauth_client_details`
--
ALTER TABLE `sys_oauth_client_details`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_sys_oauth_client_id` (`client_id`),
  ADD KEY `idx_sys_oauth_del_flag` (`del_flag`);

--
-- 表的索引 `sys_public_param`
--
ALTER TABLE `sys_public_param`
  ADD PRIMARY KEY (`public_id`),
  ADD UNIQUE KEY `uk_sys_public_param_key` (`public_key`),
  ADD KEY `idx_sys_public_param_del_flag` (`del_flag`);

--
-- 表的索引 `sys_role`
--
ALTER TABLE `sys_role`
  ADD PRIMARY KEY (`role_id`),
  ADD UNIQUE KEY `uk_sys_role_code` (`role_code`),
  ADD KEY `idx_sys_role_del_flag` (`del_flag`);

--
-- 表的索引 `sys_role_menu`
--
ALTER TABLE `sys_role_menu`
  ADD PRIMARY KEY (`role_id`,`menu_id`),
  ADD KEY `idx_sys_role_menu_role_id` (`role_id`);

--
-- 表的索引 `sys_user`
--
ALTER TABLE `sys_user`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `uk_sys_user_username` (`username`),
  ADD UNIQUE KEY `uk_sys_user_phone` (`phone`),
  ADD KEY `idx_sys_user_del_flag` (`del_flag`);

--
-- 表的索引 `sys_user_role`
--
ALTER TABLE `sys_user_role`
  ADD PRIMARY KEY (`user_id`,`role_id`),
  ADD KEY `idx_sys_user_role_user_id` (`user_id`),
  ADD KEY `idx_sys_user_role_role_id` (`role_id`);

--
-- 表的索引 `wm_address`
--
ALTER TABLE `wm_address`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_wm_address_user_id` (`user_id`),
  ADD KEY `idx_wm_address_del_flag` (`del_flag`),
  ADD KEY `idx_wm_address_create_time` (`create_time`);

--
-- 表的索引 `wm_customer_user_ext`
--
ALTER TABLE `wm_customer_user_ext`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_wm_customer_user_ext_user_id` (`user_id`),
  ADD KEY `idx_wm_customer_user_ext_del_flag` (`del_flag`);

--
-- 表的索引 `wm_delivery_order`
--
ALTER TABLE `wm_delivery_order`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_wm_delivery_order_no` (`order_no`),
  ADD KEY `idx_wm_delivery_order_order_id` (`order_id`),
  ADD KEY `idx_wm_delivery_order_delivery_user_id` (`delivery_user_id`),
  ADD KEY `idx_wm_delivery_order_del_flag` (`del_flag`);

--
-- 表的索引 `wm_delivery_user_ext`
--
ALTER TABLE `wm_delivery_user_ext`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_wm_delivery_user_ext_user_id` (`user_id`),
  ADD KEY `idx_wm_delivery_user_ext_del_flag` (`del_flag`);

--
-- 表的索引 `wm_dish`
--
ALTER TABLE `wm_dish`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_wm_dish_merchant_user_id` (`merchant_user_id`),
  ADD KEY `idx_wm_dish_sale_status` (`sale_status`),
  ADD KEY `idx_wm_dish_del_flag` (`del_flag`);

--
-- 表的索引 `wm_dish_knowledge_doc`
--
ALTER TABLE `wm_dish_knowledge_doc`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_wm_dish_knowledge_doc_dish_id` (`dish_id`),
  ADD KEY `idx_wm_dish_knowledge_doc_category` (`category`),
  ADD KEY `idx_wm_dish_knowledge_doc_spicy` (`spicy`),
  ADD KEY `idx_wm_dish_knowledge_doc_calories` (`calories`);

--
-- 表的索引 `wm_merchant_user_ext`
--
ALTER TABLE `wm_merchant_user_ext`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_wm_merchant_user_ext_user_id` (`user_id`),
  ADD KEY `idx_wm_merchant_user_ext_del_flag` (`del_flag`);

--
-- 表的索引 `wm_order`
--
ALTER TABLE `wm_order`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_wm_order_no` (`order_no`),
  ADD KEY `idx_wm_order_customer_user_id` (`customer_user_id`),
  ADD KEY `idx_wm_order_merchant_user_id` (`merchant_user_id`),
  ADD KEY `idx_wm_order_delivery_user_id` (`delivery_user_id`),
  ADD KEY `idx_wm_order_status` (`order_status`),
  ADD KEY `idx_wm_order_create_time` (`create_time`),
  ADD KEY `idx_wm_order_del_flag` (`del_flag`);

--
-- 表的索引 `wm_order_item`
--
ALTER TABLE `wm_order_item`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_wm_order_item_order_id` (`order_id`),
  ADD KEY `idx_wm_order_item_dish_id` (`dish_id`);

--
-- 表的索引 `wm_order_pay`
--
ALTER TABLE `wm_order_pay`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_wm_order_pay_order_id` (`order_id`),
  ADD KEY `idx_wm_order_pay_pay_no` (`pay_no`),
  ADD KEY `idx_wm_order_pay_status` (`pay_status`),
  ADD KEY `idx_wm_order_pay_pay_time` (`pay_time`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;