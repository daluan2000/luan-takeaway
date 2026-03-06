-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- 主机： pig-mysql:3306
-- 生成日期： 2026-03-06 03:26:24
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
-- 数据库： `pig`
--
CREATE DATABASE IF NOT EXISTS `pig` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `pig`;

-- --------------------------------------------------------

--
-- 表的结构 `gen_datasource_conf`
--

DROP TABLE IF EXISTS `gen_datasource_conf`;
CREATE TABLE `gen_datasource_conf` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '别名',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'jdbcurl',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '密码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记',
  `ds_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数据库类型',
  `conf_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配置类型',
  `ds_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数据库名称',
  `instance` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '实例',
  `port` int DEFAULT NULL COMMENT '端口',
  `host` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主机'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据源表';

-- --------------------------------------------------------

--
-- 表的结构 `gen_field_type`
--

DROP TABLE IF EXISTS `gen_field_type`;
CREATE TABLE `gen_field_type` (
  `id` bigint NOT NULL COMMENT '主键',
  `column_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字段类型',
  `attr_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '属性类型',
  `package_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '属性包名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字段类型管理';

--
-- 转存表中的数据 `gen_field_type`
--

INSERT INTO `gen_field_type` (`id`, `column_type`, `attr_type`, `package_name`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`) VALUES
(1, 'datetime', 'LocalDateTime', 'java.time.LocalDateTime', '2023-02-06 08:45:10', NULL, NULL, NULL, '0'),
(2, 'date', 'LocalDate', 'java.time.LocalDate', '2023-02-06 08:45:10', NULL, NULL, NULL, '0'),
(3, 'tinyint', 'Integer', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(4, 'smallint', 'Integer', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(5, 'mediumint', 'Integer', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(6, 'int', 'Integer', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(7, 'integer', 'Integer', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(8, 'bigint', 'Long', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(9, 'float', 'Float', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(10, 'double', 'Double', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(11, 'decimal', 'BigDecimal', 'java.math.BigDecimal', '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(12, 'bit', 'Boolean', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(13, 'char', 'String', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(14, 'varchar', 'String', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(15, 'tinytext', 'String', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(16, 'text', 'String', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(17, 'mediumtext', 'String', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(18, 'longtext', 'String', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(19, 'timestamp', 'LocalDateTime', 'java.time.LocalDateTime', '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(20, 'NUMBER', 'Integer', NULL, '2023-02-06 08:45:11', NULL, NULL, NULL, '0'),
(21, 'BINARY_INTEGER', 'Integer', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(22, 'BINARY_FLOAT', 'Float', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(23, 'BINARY_DOUBLE', 'Double', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(24, 'VARCHAR2', 'String', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(25, 'NVARCHAR', 'String', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(26, 'NVARCHAR2', 'String', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(27, 'CLOB', 'String', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(28, 'int8', 'Long', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(29, 'int4', 'Integer', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(30, 'int2', 'Integer', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(31, 'numeric', 'BigDecimal', 'java.math.BigDecimal', '2023-02-06 08:45:12', NULL, NULL, NULL, '0'),
(32, 'json', 'String', NULL, '2023-02-06 08:45:12', NULL, NULL, NULL, '0');

-- --------------------------------------------------------

--
-- 表的结构 `gen_group`
--

DROP TABLE IF EXISTS `gen_group`;
CREATE TABLE `gen_group` (
  `id` bigint NOT NULL,
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分组名称',
  `group_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分组描述',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `create_time` datetime DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '修改人',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='模板分组';

-- --------------------------------------------------------

--
-- 表的结构 `gen_table`
--

DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table` (
  `id` bigint NOT NULL,
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表名',
  `class_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '类名',
  `db_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数据库类型',
  `table_comment` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '说明',
  `author` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '作者',
  `email` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `package_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目包名',
  `version` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目版本号',
  `i18n` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '是否生成带有i18n 0 不带有 1带有',
  `style` bigint DEFAULT NULL COMMENT '代码风格',
  `child_table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '子表名称',
  `main_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主表关联键',
  `child_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '子表关联键',
  `generator_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '生成方式  0：zip压缩包   1：自定义目录',
  `backend_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '后端生成路径',
  `frontend_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '前端生成路径',
  `module_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模块名',
  `function_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '功能名',
  `form_layout` tinyint DEFAULT NULL COMMENT '表单布局  1：一列   2：两列',
  `ds_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数据源ID',
  `baseclass_id` bigint DEFAULT NULL COMMENT '基类ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成表';

-- --------------------------------------------------------

--
-- 表的结构 `gen_table_column`
--

DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column` (
  `id` bigint NOT NULL,
  `ds_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '数据源名称',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表名称',
  `field_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字段名称',
  `field_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字段类型',
  `field_comment` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字段说明',
  `attr_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '属性名',
  `attr_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '属性类型',
  `package_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '属性包名',
  `sort` int DEFAULT NULL COMMENT '排序',
  `auto_fill` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '自动填充  DEFAULT、INSERT、UPDATE、INSERT_UPDATE',
  `primary_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '主键 0：否  1：是',
  `base_field` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '基类字段 0：否  1：是',
  `form_item` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '表单项 0：否  1：是',
  `form_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '表单必填 0：否  1：是',
  `form_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表单类型',
  `form_validator` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表单效验',
  `grid_item` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '列表项 0：否  1：是',
  `grid_sort` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '列表排序 0：否  1：是',
  `query_item` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '查询项 0：否  1：是',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '查询方式',
  `query_form_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '查询表单类型',
  `field_dict` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '字典类型'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成表字段';

-- --------------------------------------------------------

--
-- 表的结构 `gen_template`
--

DROP TABLE IF EXISTS `gen_template`;
CREATE TABLE `gen_template` (
  `id` bigint NOT NULL COMMENT '主键',
  `template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板名称',
  `generator_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板路径',
  `template_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板描述',
  `template_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模板代码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '删除标记',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='模板';

-- --------------------------------------------------------

--
-- 表的结构 `gen_template_group`
--

DROP TABLE IF EXISTS `gen_template_group`;
CREATE TABLE `gen_template_group` (
  `group_id` bigint NOT NULL COMMENT '分组id',
  `template_id` bigint NOT NULL COMMENT '模板id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='模板分组关联表';

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_BLOB_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `BLOB_DATA` blob
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_CALENDARS`
--

DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
CREATE TABLE `QRTZ_CALENDARS` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `CALENDAR_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `CALENDAR` blob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_CRON_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `CRON_EXPRESSION` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TIME_ZONE_ID` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_FIRED_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `ENTRY_ID` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `INSTANCE_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `FIRED_TIME` bigint NOT NULL,
  `SCHED_TIME` bigint NOT NULL,
  `PRIORITY` int NOT NULL,
  `STATE` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `JOB_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_JOB_DETAILS`
--

DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
CREATE TABLE `QRTZ_JOB_DETAILS` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `DESCRIPTION` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `IS_DURABLE` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `IS_NONCONCURRENT` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `IS_UPDATE_DATA` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_DATA` blob
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_LOCKS`
--

DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `LOCK_NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `QRTZ_LOCKS`
--

INSERT INTO `QRTZ_LOCKS` (`SCHED_NAME`, `LOCK_NAME`) VALUES
('clusteredScheduler', 'STATE_ACCESS'),
('clusteredScheduler', 'TRIGGER_ACCESS');

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_PAUSED_TRIGGER_GRPS`
--

DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_SCHEDULER_STATE`
--

DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `INSTANCE_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `LAST_CHECKIN_TIME` bigint NOT NULL,
  `CHECKIN_INTERVAL` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `QRTZ_SCHEDULER_STATE`
--

INSERT INTO `QRTZ_SCHEDULER_STATE` (`SCHED_NAME`, `INSTANCE_NAME`, `LAST_CHECKIN_TIME`, `CHECKIN_INTERVAL`) VALUES
('clusteredScheduler', 'luan-MS-7D991772767448906', 1772767571767, 30000);

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_SIMPLE_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `REPEAT_COUNT` bigint NOT NULL,
  `REPEAT_INTERVAL` bigint NOT NULL,
  `TIMES_TRIGGERED` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_SIMPROP_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `STR_PROP_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `STR_PROP_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `STR_PROP_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `INT_PROP_1` int DEFAULT NULL,
  `INT_PROP_2` int DEFAULT NULL,
  `LONG_PROP_1` bigint DEFAULT NULL,
  `LONG_PROP_2` bigint DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `QRTZ_TRIGGERS`
--

DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
CREATE TABLE `QRTZ_TRIGGERS` (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `JOB_GROUP` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `DESCRIPTION` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint DEFAULT NULL,
  `PREV_FIRE_TIME` bigint DEFAULT NULL,
  `PRIORITY` int DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `TRIGGER_TYPE` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `START_TIME` bigint NOT NULL,
  `END_TIME` bigint DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `MISFIRE_INSTR` smallint DEFAULT NULL,
  `JOB_DATA` blob
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `sys_dept`
--

DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '部门名称',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志',
  `parent_id` bigint DEFAULT NULL COMMENT '父级部门ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门管理';

--
-- 转存表中的数据 `sys_dept`
--

INSERT INTO `sys_dept` (`dept_id`, `name`, `sort_order`, `create_by`, `update_by`, `create_time`, `update_time`, `del_flag`, `parent_id`) VALUES
(1, '总裁办', 1, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:07:49', '0', 0),
(2, '技术部', 2, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 1),
(3, '市场部', 3, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 1),
(4, '销售部', 4, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 1),
(5, '财务部', 5, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 1),
(6, '人事行政部', 6, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:53:36', '1', 1),
(7, '研发部', 7, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 2),
(8, 'UI设计部', 11, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 7),
(9, '产品部', 12, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 2),
(10, '渠道部', 13, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 3),
(11, '推广部', 14, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 3),
(12, '客服部', 15, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 4),
(13, '财务会计部', 16, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 13:04:47', '0', 5),
(14, '审计风控部', 17, 'admin', 'admin', '2023-04-03 13:04:47', '2023-04-03 14:06:57', '0', 5);

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
(2, 'social_type', '社交登录', ' ', ' ', '2019-03-19 11:09:44', '2019-03-19 11:09:44', '微信、QQ', '1', '0'),
(3, 'job_type', '定时任务类型', ' ', ' ', '2019-03-19 11:22:21', '2019-03-19 11:22:21', 'quartz', '1', '0'),
(4, 'job_status', '定时任务状态', ' ', ' ', '2019-03-19 11:24:57', '2019-03-19 11:24:57', '发布状态、运行状态', '1', '0'),
(5, 'job_execute_status', '定时任务执行状态', ' ', ' ', '2019-03-19 11:26:15', '2019-03-19 11:26:15', '正常、异常', '1', '0'),
(6, 'misfire_policy', '定时任务错失执行策略', ' ', ' ', '2019-03-19 11:27:19', '2019-03-19 11:27:19', '周期', '1', '0'),
(7, 'gender', '性别', ' ', ' ', '2019-03-27 13:44:06', '2019-03-27 13:44:06', '微信用户性别', '1', '0'),
(8, 'subscribe', '订阅状态', ' ', ' ', '2019-03-27 13:48:33', '2019-03-27 13:48:33', '公众号订阅状态', '1', '0'),
(9, 'response_type', '回复', ' ', ' ', '2019-03-28 21:29:21', '2019-03-28 21:29:21', '微信消息是否已回复', '1', '0'),
(10, 'param_type', '参数配置', ' ', ' ', '2019-04-29 18:20:47', '2019-04-29 18:20:47', '检索、原文、报表、安全、文档、消息、其他', '1', '0'),
(11, 'status_type', '租户状态', ' ', ' ', '2019-05-15 16:31:08', '2019-05-15 16:31:08', '租户状态', '1', '0'),
(12, 'dict_type', '字典类型', ' ', ' ', '2019-05-16 14:16:20', '2019-05-16 14:20:16', '系统类不能修改', '1', '0'),
(13, 'channel_type', '支付类型', ' ', ' ', '2019-05-16 14:16:20', '2019-05-16 14:20:16', '系统类不能修改', '1', '0'),
(14, 'grant_types', '授权类型', ' ', ' ', '2019-08-13 07:34:10', '2019-08-13 07:34:10', NULL, '1', '0'),
(15, 'style_type', '前端风格', ' ', ' ', '2020-02-07 03:49:28', '2020-02-07 03:50:40', '0-Avue 1-element', '1', '0'),
(16, 'captcha_flag_types', '验证码开关', ' ', ' ', '2020-11-18 06:53:25', '2020-11-18 06:53:25', '是否校验验证码', '1', '0'),
(17, 'enc_flag_types', '前端密码加密', ' ', ' ', '2020-11-18 06:54:44', '2020-11-18 06:54:44', '前端密码是否加密传输', '1', '0'),
(18, 'lock_flag', '用户状态', 'admin', ' ', '2023-02-01 16:55:31', NULL, NULL, '1', '0'),
(19, 'ds_config_type', '数据连接类型', 'admin', ' ', '2023-02-06 18:36:59', NULL, NULL, '1', '0'),
(20, 'common_status', '通用状态', 'admin', ' ', '2023-02-09 11:02:08', NULL, NULL, '1', '0'),
(21, 'app_social_type', 'app社交登录', 'admin', ' ', '2023-02-10 11:11:06', NULL, 'app社交登录', '1', '0'),
(22, 'yes_no_type', '是否', 'admin', ' ', '2023-02-20 23:25:04', NULL, NULL, '1', '0'),
(23, 'repType', '微信消息类型', 'admin', ' ', '2023-02-24 15:08:25', NULL, NULL, '0', '0'),
(24, 'leave_status', '请假状态', 'admin', ' ', '2023-03-02 22:50:15', NULL, NULL, '0', '0'),
(25, 'schedule_type', '日程类型', 'admin', ' ', '2023-03-06 14:49:18', NULL, NULL, '0', '0'),
(26, 'schedule_status', '日程状态', 'admin', ' ', '2023-03-06 14:52:57', NULL, NULL, '0', '0'),
(27, 'ds_type', '代码生成器支持的数据库类型', 'admin', ' ', '2023-03-12 09:57:59', NULL, NULL, '1', '0');

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
(3, 2, 'WX', '微信', 'social_type', '微信登录', 0, ' ', ' ', '2019-03-19 11:10:02', '2019-03-25 12:49:36', '', '0'),
(4, 2, 'QQ', 'QQ', 'social_type', 'QQ登录', 1, ' ', ' ', '2019-03-19 11:10:14', '2019-03-25 12:49:36', '', '0'),
(5, 3, '1', 'java类', 'job_type', 'java类', 1, ' ', ' ', '2019-03-19 11:22:37', '2019-03-25 12:49:36', '', '0'),
(6, 3, '2', 'spring bean', 'job_type', 'spring bean容器实例', 2, ' ', ' ', '2019-03-19 11:23:05', '2019-03-25 12:49:36', '', '0'),
(7, 3, '9', '其他', 'job_type', '其他类型', 9, ' ', ' ', '2019-03-19 11:23:31', '2019-03-25 12:49:36', '', '0'),
(8, 3, '3', 'Rest 调用', 'job_type', 'Rest 调用', 3, ' ', ' ', '2019-03-19 11:23:57', '2019-03-25 12:49:36', '', '0'),
(9, 3, '4', 'jar', 'job_type', 'jar类型', 4, ' ', ' ', '2019-03-19 11:24:20', '2019-03-25 12:49:36', '', '0'),
(10, 4, '1', '未发布', 'job_status', '未发布', 1, ' ', ' ', '2019-03-19 11:25:18', '2019-03-25 12:49:36', '', '0'),
(11, 4, '2', '运行中', 'job_status', '运行中', 2, ' ', ' ', '2019-03-19 11:25:31', '2019-03-25 12:49:36', '', '0'),
(12, 4, '3', '暂停', 'job_status', '暂停', 3, ' ', ' ', '2019-03-19 11:25:42', '2019-03-25 12:49:36', '', '0'),
(13, 5, '0', '正常', 'job_execute_status', '正常', 0, ' ', ' ', '2019-03-19 11:26:27', '2019-03-25 12:49:36', '', '0'),
(14, 5, '1', '异常', 'job_execute_status', '异常', 1, ' ', ' ', '2019-03-19 11:26:41', '2019-03-25 12:49:36', '', '0'),
(15, 6, '1', '错失周期立即执行', 'misfire_policy', '错失周期立即执行', 1, ' ', ' ', '2019-03-19 11:27:45', '2019-03-25 12:49:36', '', '0'),
(16, 6, '2', '错失周期执行一次', 'misfire_policy', '错失周期执行一次', 2, ' ', ' ', '2019-03-19 11:27:57', '2019-03-25 12:49:36', '', '0'),
(17, 6, '3', '下周期执行', 'misfire_policy', '下周期执行', 3, ' ', ' ', '2019-03-19 11:28:08', '2019-03-25 12:49:36', '', '0'),
(18, 7, '1', '男', 'gender', '微信-男', 0, ' ', ' ', '2019-03-27 13:45:13', '2019-03-27 13:45:13', '微信-男', '0'),
(19, 7, '2', '女', 'gender', '女-微信', 1, ' ', ' ', '2019-03-27 13:45:34', '2019-03-27 13:45:34', '女-微信', '0'),
(20, 7, '0', '未知', 'gender', 'x性别未知', 3, ' ', ' ', '2019-03-27 13:45:57', '2019-03-27 13:45:57', 'x性别未知', '0'),
(21, 8, '0', '未关注', 'subscribe', '公众号-未关注', 0, ' ', ' ', '2019-03-27 13:49:07', '2019-03-27 13:49:07', '公众号-未关注', '0'),
(22, 8, '1', '已关注', 'subscribe', '公众号-已关注', 1, ' ', ' ', '2019-03-27 13:49:26', '2019-03-27 13:49:26', '公众号-已关注', '0'),
(23, 9, '0', '未回复', 'response_type', '微信消息-未回复', 0, ' ', ' ', '2019-03-28 21:29:47', '2019-03-28 21:29:47', '微信消息-未回复', '0'),
(24, 9, '1', '已回复', 'response_type', '微信消息-已回复', 1, ' ', ' ', '2019-03-28 21:30:08', '2019-03-28 21:30:08', '微信消息-已回复', '0'),
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
(37, 2, 'GITEE', '码云', 'social_type', '码云', 2, ' ', ' ', '2019-06-28 09:59:12', '2019-06-28 09:59:12', '码云', '0'),
(38, 2, 'OSC', '开源中国', 'social_type', '开源中国登录', 2, ' ', ' ', '2019-06-28 10:04:32', '2019-06-28 10:04:32', '', '0'),
(39, 14, 'password', '密码模式', 'grant_types', '支持oauth密码模式', 0, ' ', ' ', '2019-08-13 07:35:28', '2019-08-13 07:35:28', NULL, '0'),
(40, 14, 'authorization_code', '授权码模式', 'grant_types', 'oauth2 授权码模式', 1, ' ', ' ', '2019-08-13 07:36:07', '2019-08-13 07:36:07', NULL, '0'),
(41, 14, 'client_credentials', '客户端模式', 'grant_types', 'oauth2 客户端模式', 2, ' ', ' ', '2019-08-13 07:36:30', '2019-08-13 07:36:30', NULL, '0'),
(42, 14, 'refresh_token', '刷新模式', 'grant_types', 'oauth2 刷新token', 3, ' ', ' ', '2019-08-13 07:36:54', '2019-08-13 07:36:54', NULL, '0'),
(43, 14, 'implicit', '简化模式', 'grant_types', 'oauth2 简化模式', 4, ' ', ' ', '2019-08-13 07:39:32', '2019-08-13 07:39:32', NULL, '0'),
(44, 15, '0', 'Avue', 'style_type', 'Avue风格', 0, ' ', ' ', '2020-02-07 03:52:52', '2020-02-07 03:52:52', '', '0'),
(45, 15, '1', 'element', 'style_type', 'element-ui', 1, ' ', ' ', '2020-02-07 03:53:12', '2020-02-07 03:53:12', '', '0'),
(46, 16, '0', '关', 'captcha_flag_types', '不校验验证码', 0, ' ', ' ', '2020-11-18 06:53:58', '2020-11-18 06:53:58', '不校验验证码 -0', '0'),
(47, 16, '1', '开', 'captcha_flag_types', '校验验证码', 1, ' ', ' ', '2020-11-18 06:54:15', '2020-11-18 06:54:15', '不校验验证码-1', '0'),
(48, 17, '0', '否', 'enc_flag_types', '不加密', 0, ' ', ' ', '2020-11-18 06:55:31', '2020-11-18 06:55:31', '不加密-0', '0'),
(49, 17, '1', '是', 'enc_flag_types', '加密', 1, ' ', ' ', '2020-11-18 06:55:51', '2020-11-18 06:55:51', '加密-1', '0'),
(50, 13, 'MERGE_PAY', '聚合支付', 'channel_type', '聚合支付', 1, ' ', ' ', '2019-05-30 19:08:08', '2019-06-18 13:51:53', '聚合支付', '0'),
(51, 2, 'CAS', 'CAS登录', 'social_type', 'CAS 单点登录系统', 3, ' ', ' ', '2022-02-18 13:56:25', '2022-02-18 13:56:28', NULL, '0'),
(52, 2, 'DINGTALK', '钉钉', 'social_type', '钉钉', 3, ' ', ' ', '2022-02-18 13:56:25', '2022-02-18 13:56:28', NULL, '0'),
(53, 2, 'WEIXIN_CP', '企业微信', 'social_type', '企业微信', 3, ' ', ' ', '2022-02-18 13:56:25', '2022-02-18 13:56:28', NULL, '0'),
(54, 15, '2', 'APP', 'style_type', 'uview风格', 1, ' ', ' ', '2020-02-07 03:53:12', '2020-02-07 03:53:12', '', '0'),
(55, 13, 'ALIPAY_WAP', '支付宝支付', 'channel_type', '支付宝支付', 1, ' ', ' ', '2019-05-30 19:08:08', '2019-06-18 13:51:53', '聚合支付', '0'),
(56, 13, 'WEIXIN_MP', '微信支付', 'channel_type', '微信支付', 1, ' ', ' ', '2019-05-30 19:08:08', '2019-06-18 13:51:53', '聚合支付', '0'),
(57, 14, 'mobile', 'mobile', 'grant_types', '移动端登录', 5, 'admin', ' ', '2023-01-29 17:21:42', NULL, NULL, '0'),
(58, 18, '0', '有效', 'lock_flag', '有效', 0, 'admin', ' ', '2023-02-01 16:56:00', NULL, NULL, '0'),
(59, 18, '9', '禁用', 'lock_flag', '禁用', 1, 'admin', ' ', '2023-02-01 16:56:09', NULL, NULL, '0'),
(60, 15, '4', 'vue3', 'style_type', 'element-plus', 4, 'admin', ' ', '2023-02-06 13:52:43', NULL, NULL, '0'),
(61, 19, '0', '主机', 'ds_config_type', '主机', 0, 'admin', ' ', '2023-02-06 18:37:23', NULL, NULL, '0'),
(62, 19, '1', 'JDBC', 'ds_config_type', 'jdbc', 2, 'admin', ' ', '2023-02-06 18:37:34', NULL, NULL, '0'),
(63, 20, 'false', '否', 'common_status', '否', 1, 'admin', ' ', '2023-02-09 11:02:39', NULL, NULL, '0'),
(64, 20, 'true', '是', 'common_status', '是', 2, 'admin', ' ', '2023-02-09 11:02:52', NULL, NULL, '0'),
(65, 21, 'MINI', '小程序', 'app_social_type', '小程序登录', 0, 'admin', ' ', '2023-02-10 11:11:41', NULL, NULL, '0'),
(66, 22, '0', '否', 'yes_no_type', '0', 0, 'admin', ' ', '2023-02-20 23:35:23', NULL, '0', '0'),
(67, 22, '1', '是', 'yes_no_type', '1', 0, 'admin', ' ', '2023-02-20 23:35:37', NULL, '1', '0'),
(69, 23, 'text', '文本', 'repType', '文本', 0, 'admin', ' ', '2023-02-24 15:08:45', NULL, NULL, '0'),
(70, 23, 'image', '图片', 'repType', '图片', 0, 'admin', ' ', '2023-02-24 15:08:56', NULL, NULL, '0'),
(71, 23, 'voice', '语音', 'repType', '语音', 0, 'admin', ' ', '2023-02-24 15:09:08', NULL, NULL, '0'),
(72, 23, 'video', '视频', 'repType', '视频', 0, 'admin', ' ', '2023-02-24 15:09:18', NULL, NULL, '0'),
(73, 23, 'shortvideo', '小视频', 'repType', '小视频', 0, 'admin', ' ', '2023-02-24 15:09:29', NULL, NULL, '0'),
(74, 23, 'location', '地理位置', 'repType', '地理位置', 0, 'admin', ' ', '2023-02-24 15:09:41', NULL, NULL, '0'),
(75, 23, 'link', '链接消息', 'repType', '链接消息', 0, 'admin', ' ', '2023-02-24 15:09:49', NULL, NULL, '0'),
(76, 23, 'event', '事件推送', 'repType', '事件推送', 0, 'admin', ' ', '2023-02-24 15:09:57', NULL, NULL, '0'),
(77, 24, '0', '未提交', 'leave_status', '未提交', 0, 'admin', ' ', '2023-03-02 22:50:45', NULL, '未提交', '0'),
(78, 24, '1', '审批中', 'leave_status', '审批中', 0, 'admin', ' ', '2023-03-02 22:50:57', NULL, '审批中', '0'),
(79, 24, '2', '完成', 'leave_status', '完成', 0, 'admin', ' ', '2023-03-02 22:51:06', NULL, '完成', '0'),
(80, 24, '9', '驳回', 'leave_status', '驳回', 0, 'admin', ' ', '2023-03-02 22:51:20', NULL, NULL, '0'),
(81, 25, 'record', '日程记录', 'schedule_type', '日程记录', 0, 'admin', ' ', '2023-03-06 14:50:01', NULL, NULL, '0'),
(82, 25, 'plan', '计划', 'schedule_type', '计划类型', 0, 'admin', ' ', '2023-03-06 14:50:29', NULL, NULL, '0'),
(83, 26, '0', '计划中', 'schedule_status', '日程状态', 0, 'admin', ' ', '2023-03-06 14:53:18', NULL, NULL, '0'),
(84, 26, '1', '已开始', 'schedule_status', '已开始', 0, 'admin', ' ', '2023-03-06 14:53:33', NULL, NULL, '0'),
(85, 26, '3', '已结束', 'schedule_status', '已结束', 0, 'admin', ' ', '2023-03-06 14:53:41', NULL, NULL, '0'),
(86, 27, 'mysql', 'mysql', 'ds_type', 'mysql', 0, 'admin', ' ', '2023-03-12 09:58:11', NULL, NULL, '0');

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
-- 表的结构 `sys_job`
--

DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL COMMENT '任务id',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `job_order` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '组内执行顺利，值越大执行优先级越高，最大值9，最小值1',
  `job_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1' COMMENT '1、java类;2、spring bean名称;3、rest调用;4、jar调用;9其他',
  `execute_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'job_type=3时，rest调用地址，仅支持rest get协议,需要增加String返回值，0成功，1失败;job_type=4时，jar路径;其它值为空',
  `class_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'job_type=1时，类完整路径;job_type=2时，spring bean名称;其它值为空',
  `method_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务方法',
  `method_params_value` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '参数值',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '3' COMMENT '错失执行策略（1错失周期立即执行 2错失周期执行一次 3下周期执行）',
  `job_tenant_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '1、多租户任务;2、非多租户任务',
  `job_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（1、未发布;2、运行中;3、暂停;4、删除;）',
  `job_execute_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1异常）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '初次执行时间',
  `previous_time` timestamp NULL DEFAULT NULL COMMENT '上次执行时间',
  `next_time` timestamp NULL DEFAULT NULL COMMENT '下次执行时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注信息'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务调度表';

-- --------------------------------------------------------

--
-- 表的结构 `sys_job_log`
--

DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint NOT NULL COMMENT '任务日志ID',
  `job_id` bigint NOT NULL COMMENT '任务id',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务组名',
  `job_order` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组内执行顺利，值越大执行优先级越高，最大值9，最小值1',
  `job_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1' COMMENT '1、java类;2、spring bean名称;3、rest调用;4、jar调用;9其他',
  `execute_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'job_type=3时，rest调用地址，仅支持post协议;job_type=4时，jar路径;其它值为空',
  `class_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'job_type=1时，类完整路径;job_type=2时，spring bean名称;其它值为空',
  `method_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务方法',
  `method_params_value` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '参数值',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'cron执行表达式',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日志信息',
  `job_log_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `execute_time` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '执行时间',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务执行日志表';

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
(1400, '部门管理', 'dept', NULL, '/admin/dept/index', 1000, 'iconfont icon-zidingyibuju', '1', 4, '0', NULL, '0', '', '2018-01-20 13:17:19', 'admin', '2023-07-05 10:28:07', '0'),
(1401, '部门新增', NULL, 'sys_dept_add', NULL, 1400, NULL, '1', 1, '0', NULL, '1', ' ', '2018-01-20 14:56:16', ' ', '2021-05-25 03:12:55', '0'),
(1402, '部门修改', NULL, 'sys_dept_edit', NULL, 1400, NULL, '1', 1, '0', NULL, '1', ' ', '2018-01-20 14:56:59', ' ', '2021-05-25 03:12:55', '0'),
(1403, '部门删除', NULL, 'sys_dept_del', NULL, 1400, NULL, '1', 1, '0', NULL, '1', ' ', '2018-01-20 14:57:28', ' ', '2021-05-25 03:12:55', '0'),
(1600, '岗位管理', 'post', NULL, '/admin/post/index', 1000, 'iconfont icon--chaifenhang', '1', 5, '1', '0', '0', '', '2022-03-26 13:04:14', 'admin', '2023-07-05 10:28:03', '0'),
(1601, '岗位信息查看', NULL, 'sys_post_view', NULL, 1600, NULL, '1', 0, '0', NULL, '1', ' ', '2022-03-26 13:05:34', ' ', NULL, '0'),
(1602, '岗位信息新增', NULL, 'sys_post_add', NULL, 1600, NULL, '1', 1, '0', NULL, '1', ' ', '2022-03-26 13:06:00', ' ', NULL, '0'),
(1603, '岗位信息修改', NULL, 'sys_post_edit', NULL, 1600, NULL, '1', 2, '0', NULL, '1', ' ', '2022-03-26 13:06:31', ' ', '2022-03-26 13:06:38', '0'),
(1604, '岗位信息删除', NULL, 'sys_post_del', NULL, 1600, NULL, '1', 3, '0', NULL, '1', ' ', '2022-03-26 13:06:31', ' ', NULL, '0'),
(1605, '岗位导入导出', NULL, 'sys_post_export', NULL, 1600, NULL, '1', 4, '0', NULL, '1', ' ', '2022-03-26 13:06:31', ' ', '2022-03-26 06:32:02', '0'),
(2000, '系统管理', 'system', NULL, '/system', -1, 'iconfont icon-quanjushezhi_o', '1', 1, '0', NULL, '0', '', '2017-11-07 20:56:00', 'admin', '2023-07-05 10:27:58', '0'),
(2001, '日志管理', 'log', NULL, '/admin/logs', 2000, 'ele-Cloudy', '1', 0, '0', '0', '0', 'admin', '2023-03-02 12:26:42', 'admin', '2023-07-05 10:27:53', '0'),
(2100, '操作日志', 'operation', NULL, '/admin/log/index', 2001, 'iconfont icon-jinridaiban', '1', 2, '0', '0', '0', '', '2017-11-20 14:06:22', 'admin', '2023-07-05 10:27:49', '0'),
(2101, '日志删除', NULL, 'sys_log_del', NULL, 2100, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-20 20:37:37', ' ', '2021-05-25 03:12:55', '0'),
(2102, '导入导出', NULL, 'sys_log_export', NULL, 2100, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-08 09:54:01', ' ', '2021-05-25 03:12:55', '0'),
(2200, '字典管理', 'dict', NULL, '/admin/dict/index', 2000, 'iconfont icon-zhongduancanshuchaxun', '1', 6, '0', NULL, '0', '', '2017-11-29 11:30:52', 'admin', '2023-07-05 10:27:37', '0'),
(2201, '字典删除', NULL, 'sys_dict_del', NULL, 2200, NULL, '1', 1, '0', NULL, '1', ' ', '2017-11-29 11:30:11', ' ', '2021-05-25 03:12:55', '0'),
(2202, '字典新增', NULL, 'sys_dict_add', NULL, 2200, NULL, '1', 1, '0', NULL, '1', ' ', '2018-05-11 22:34:55', ' ', '2021-05-25 03:12:55', '0'),
(2203, '字典修改', NULL, 'sys_dict_edit', NULL, 2200, NULL, '1', 1, '0', NULL, '1', ' ', '2018-05-11 22:36:03', ' ', '2021-05-25 03:12:55', '0'),
(2210, '参数管理', 'parameter', NULL, '/admin/param/index', 2000, 'iconfont icon-wenducanshu-05', '1', 7, '1', NULL, '0', '', '2019-04-29 22:16:50', 'admin', '2023-02-16 15:24:51', '0'),
(2211, '参数新增', NULL, 'sys_syspublicparam_add', NULL, 2210, NULL, '1', 1, '0', NULL, '1', ' ', '2019-04-29 22:17:36', ' ', '2020-03-24 08:57:11', '0'),
(2212, '参数删除', NULL, 'sys_syspublicparam_del', NULL, 2210, NULL, '1', 1, '0', NULL, '1', ' ', '2019-04-29 22:17:55', ' ', '2020-03-24 08:57:12', '0'),
(2213, '参数编辑', NULL, 'sys_syspublicparam_edit', NULL, 2210, NULL, '1', 1, '0', NULL, '1', ' ', '2019-04-29 22:18:14', ' ', '2020-03-24 08:57:13', '0'),
(2300, '代码生成', 'code', NULL, '/gen/table/index', 9000, 'iconfont icon-zhongduancanshu', '1', 1, '0', '0', '0', '', '2018-01-20 13:17:19', 'admin', '2023-02-20 13:54:35', '0'),
(2400, '终端管理', 'client', NULL, '/admin/client/index', 2000, 'iconfont icon-gongju', '1', 9, '1', NULL, '0', '', '2018-01-20 13:17:19', 'admin', '2023-02-16 15:25:28', '0'),
(2401, '客户端新增', NULL, 'sys_client_add', NULL, 2400, '1', '1', 1, '0', NULL, '1', ' ', '2018-05-15 21:35:18', ' ', '2021-05-25 03:12:55', '0'),
(2402, '客户端修改', NULL, 'sys_client_edit', NULL, 2400, NULL, '1', 1, '0', NULL, '1', ' ', '2018-05-15 21:37:06', ' ', '2021-05-25 03:12:55', '0'),
(2403, '客户端删除', NULL, 'sys_client_del', NULL, 2400, NULL, '1', 1, '0', NULL, '1', ' ', '2018-05-15 21:39:16', ' ', '2021-05-25 03:12:55', '0'),
(2600, '令牌管理', 'token', NULL, '/admin/token/index', 2000, 'ele-Key', '1', 11, '0', NULL, '0', '', '2018-09-04 05:58:41', 'admin', '2023-02-16 15:28:28', '0'),
(2601, '令牌删除', NULL, 'sys_token_del', NULL, 2600, NULL, '1', 1, '0', NULL, '1', ' ', '2018-09-04 05:59:50', ' ', '2020-03-24 08:57:24', '0'),
(2800, 'Quartz管理', 'quartz', NULL, '/daemon/job-manage/index', 2000, 'ele-AlarmClock', '1', 8, '0', NULL, '0', '', '2018-01-20 13:17:19', 'admin', '2023-02-16 15:25:06', '0'),
(2810, '任务新增', NULL, 'job_sys_job_add', NULL, 2800, '1', '1', 0, '0', NULL, '1', ' ', '2018-05-15 21:35:18', ' ', '2020-03-24 08:57:26', '0'),
(2820, '任务修改', NULL, 'job_sys_job_edit', NULL, 2800, '1', '1', 0, '0', NULL, '1', ' ', '2018-05-15 21:35:18', ' ', '2020-03-24 08:57:27', '0'),
(2830, '任务删除', NULL, 'job_sys_job_del', NULL, 2800, '1', '1', 0, '0', NULL, '1', ' ', '2018-05-15 21:35:18', ' ', '2020-03-24 08:57:28', '0'),
(2840, '任务暂停', NULL, 'job_sys_job_shutdown_job', NULL, 2800, '1', '1', 0, '0', NULL, '1', ' ', '2018-05-15 21:35:18', ' ', '2020-03-24 08:57:28', '0'),
(2850, '任务开始', NULL, 'job_sys_job_start_job', NULL, 2800, '1', '1', 0, '0', NULL, '1', ' ', '2018-05-15 21:35:18', ' ', '2020-03-24 08:57:29', '0'),
(2860, '任务刷新', NULL, 'job_sys_job_refresh_job', NULL, 2800, '1', '1', 0, '0', NULL, '1', ' ', '2018-05-15 21:35:18', ' ', '2020-03-24 08:57:30', '0'),
(2870, '执行任务', NULL, 'job_sys_job_run_job', NULL, 2800, '1', '1', 0, '0', NULL, '1', ' ', '2019-08-08 15:35:18', ' ', '2020-03-24 08:57:31', '0'),
(2871, '导出', NULL, 'job_sys_job_export', NULL, 2800, NULL, '1', 0, '0', '0', '1', 'admin', '2023-03-06 15:26:13', ' ', NULL, '0'),
(2906, '文件管理', 'file', NULL, '/admin/file/index', 2000, 'ele-Files', '1', 6, '0', NULL, '0', '', '2019-06-25 12:44:46', 'admin', '2023-02-16 15:24:42', '0'),
(2907, '删除文件', NULL, 'sys_file_del', NULL, 2906, NULL, '1', 1, '0', NULL, '1', ' ', '2019-06-25 13:41:41', ' ', '2020-03-24 08:58:42', '0'),
(4000, '系统监控', 'monitor', NULL, '/daemon', -1, 'iconfont icon-shuju', '1', 3, '0', '0', '0', 'admin', '2023-02-06 20:20:47', 'admin', '2023-02-23 20:01:07', '0'),
(4001, '文档扩展', 'doc', NULL, 'http://pig-gateway:9999/swagger-ui.html', 4000, 'iconfont icon-biaodan', '1', 2, '0', '1', '0', '', '2018-06-26 10:50:32', 'admin', '2023-02-23 20:01:29', '0'),
(4002, '缓存监控', 'cache', NULL, '/ext/cache', 4000, 'iconfont icon-shuju', '1', 1, '0', '0', '0', 'admin', '2023-05-29 15:12:59', 'admin', '2023-06-06 11:58:41', '0'),
(9000, '开发平台', 'develop', NULL, '/gen', -1, 'iconfont icon-shuxingtu', '1', 9, '0', '0', '0', '', '2019-08-12 09:35:16', 'admin', '2023-07-05 10:25:27', '0'),
(9005, '数据源管理', 'datasource', NULL, '/gen/datasource/index', 9000, 'ele-Coin', '1', 0, '0', NULL, '0', '', '2019-08-12 09:42:11', 'admin', '2023-07-05 10:26:56', '0'),
(9006, '表单设计', 'Form Design', NULL, '/gen/design/index', 9000, 'iconfont icon-AIshiyanshi', '0', 2, '0', '0', '0', '', '2019-08-16 10:08:56', 'admin', '2023-02-23 14:06:50', '0'),
(9007, '生成页面', 'generation', NULL, '/gen/gener/index', 9000, 'iconfont icon-tongzhi4', '0', 0, '0', '0', '0', 'admin', '2023-02-20 09:58:23', 'admin', '2023-07-05 10:27:06', '0'),
(9050, '元数据管理', 'metadata', NULL, '/gen/metadata', 9000, 'iconfont icon--chaifenhang', '1', 9, '0', '0', '0', '', '2018-07-27 01:13:21', 'admin', '2023-07-05 10:27:13', '0'),
(9051, '模板管理', 'template', NULL, '/gen/template/index', 9050, 'iconfont icon--chaifenhang', '1', 5, '0', '0', '0', 'admin', '2023-02-21 11:22:54', 'admin', '2023-07-05 10:27:18', '0'),
(9052, '查询', NULL, 'codegen_template_view', NULL, 9051, NULL, '0', 0, '0', '0', '1', 'admin', '2023-02-21 12:33:03', 'admin', '2023-02-21 13:50:54', '0'),
(9053, '增加', NULL, 'codegen_template_add', NULL, 9051, NULL, '1', 0, '0', '0', '1', 'admin', '2023-02-21 13:34:10', 'admin', '2023-02-21 13:39:49', '0'),
(9054, '新增', NULL, 'codegen_template_add', NULL, 9051, NULL, '0', 1, '0', '0', '1', 'admin', '2023-02-21 13:51:32', ' ', NULL, '0'),
(9055, '导出', NULL, 'codegen_template_export', NULL, 9051, NULL, '0', 2, '0', '0', '1', 'admin', '2023-02-21 13:51:58', ' ', NULL, '0'),
(9056, '删除', NULL, 'codegen_template_del', NULL, 9051, NULL, '0', 3, '0', '0', '1', 'admin', '2023-02-21 13:52:16', ' ', NULL, '0'),
(9057, '编辑', NULL, 'codegen_template_edit', NULL, 9051, NULL, '0', 4, '0', '0', '1', 'admin', '2023-02-21 13:52:58', ' ', NULL, '0'),
(9059, '模板分组', 'group', NULL, '/gen/group/index', 9050, 'iconfont icon-shuxingtu', '1', 6, '0', '0', '0', 'admin', '2023-02-21 15:06:50', 'admin', '2023-07-05 10:27:22', '0'),
(9060, '查询', NULL, 'codegen_group_view', NULL, 9059, NULL, '0', 0, '0', '0', '1', 'admin', '2023-02-21 15:08:07', ' ', NULL, '0'),
(9061, '新增', NULL, 'codegen_group_add', NULL, 9059, NULL, '0', 0, '0', '0', '1', 'admin', '2023-02-21 15:08:28', ' ', NULL, '0'),
(9062, '修改', NULL, 'codegen_group_edit', NULL, 9059, NULL, '0', 0, '0', '0', '1', 'admin', '2023-02-21 15:08:43', ' ', NULL, '0'),
(9063, '删除', NULL, 'codegen_group_del', NULL, 9059, NULL, '0', 0, '0', '0', '1', 'admin', '2023-02-21 15:09:02', ' ', NULL, '0'),
(9064, '导出', NULL, 'codegen_group_export', NULL, 9059, NULL, '0', 0, '0', '0', '1', 'admin', '2023-02-21 15:09:22', ' ', NULL, '0'),
(9065, '字段管理', 'field', NULL, '/gen/field-type/index', 9050, 'iconfont icon-fuwenben', '1', 0, '0', '0', '0', 'admin', '2023-02-23 20:05:09', 'admin', '2023-07-05 10:27:31', '0'),
(3000000000000010000, '外卖平台', 'takeaway', NULL, '/takeaway', -1, 'iconfont icon-gongju', '1', 10, '0', '0', '0', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010100, '商家中心', 'merchant', NULL, '/takeaway/merchant/index', 3000000000000010000, 'ele-Shop', '1', 1, '0', '0', '0', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010101, '商家查询', NULL, 'wm_merchant_view', NULL, 3000000000000010100, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010102, '商家入驻', NULL, 'wm_merchant_apply', NULL, 3000000000000010100, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010103, '商家审核', NULL, 'wm_merchant_audit', NULL, 3000000000000010100, NULL, '1', 3, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010104, '商家编辑', NULL, 'wm_merchant_edit', NULL, 3000000000000010100, NULL, '1', 4, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010105, '营业状态', NULL, 'wm_merchant_business', NULL, 3000000000000010100, NULL, '1', 5, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010106, '商家接单', NULL, 'wm_merchant_accept', NULL, 3000000000000010100, NULL, '1', 6, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010200, '菜品管理', 'dish', NULL, '/takeaway/dish/index', 3000000000000010000, 'ele-Food', '1', 2, '0', '0', '0', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010201, '菜品查询', NULL, 'wm_dish_view', NULL, 3000000000000010200, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010202, '菜品新增', NULL, 'wm_dish_add', NULL, 3000000000000010200, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010203, '菜品编辑', NULL, 'wm_dish_edit', NULL, 3000000000000010200, NULL, '1', 3, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010204, '菜品删除', NULL, 'wm_dish_del', NULL, 3000000000000010200, NULL, '1', 4, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010205, '菜品上架', NULL, 'wm_dish_sale_on', NULL, 3000000000000010200, NULL, '1', 5, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010206, '菜品下架', NULL, 'wm_dish_sale_off', NULL, 3000000000000010200, NULL, '1', 6, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010300, '订单中心', 'order', NULL, '/takeaway/order/index', 3000000000000010000, 'ele-List', '1', 3, '0', '0', '0', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010301, '订单查询', NULL, 'wm_order_view', NULL, 3000000000000010300, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010302, '订单创建', NULL, 'wm_order_create', NULL, 3000000000000010300, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010303, '订单详情', NULL, 'wm_order_detail', NULL, 3000000000000010300, NULL, '1', 3, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010304, '订单取消', NULL, 'wm_order_cancel', NULL, 3000000000000010300, NULL, '1', 4, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010400, '配送中心', 'delivery', NULL, '/takeaway/delivery/index', 3000000000000010000, 'ele-Bicycle', '1', 4, '0', '0', '0', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010401, '骑手信息', NULL, 'wm_delivery_rider', NULL, 3000000000000010400, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010402, '配送查询', NULL, 'wm_delivery_view', NULL, 3000000000000010400, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010403, '骑手接单', NULL, 'wm_delivery_accept', NULL, 3000000000000010400, NULL, '1', 3, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010404, '配送完成', NULL, 'wm_delivery_complete', NULL, 3000000000000010400, NULL, '1', 4, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010500, '支付管理', 'pay', NULL, '/takeaway/pay/index', 3000000000000010000, 'ele-Wallet', '1', 5, '0', '0', '0', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010501, '支付查询', NULL, 'wm_pay_view', NULL, 3000000000000010500, NULL, '1', 1, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0'),
(3000000000000010502, '模拟支付', NULL, 'wm_pay_mock', NULL, 3000000000000010500, NULL, '1', 2, '0', NULL, '1', 'admin', '2026-03-05 03:49:34', 'admin', '2026-03-05 03:49:34', '0');

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
-- 表的结构 `sys_post`
--

DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '岗位排序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '岗位描述',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '是否删除  -1：已删除  0：正常',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='岗位信息表';

--
-- 转存表中的数据 `sys_post`
--

INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `remark`, `del_flag`, `create_time`, `create_by`, `update_time`, `update_by`) VALUES
(1, 'CTO', 'CTO', 0, 'CTOOO', '0', '2022-03-26 13:48:17', '', '2023-03-08 16:03:35', 'admin');

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
(1, '租户默认来源', 'TENANT_DEFAULT_ID', '1', '0', '', ' ', ' ', '2020-05-12 04:03:46', '2020-06-20 08:56:30', '2', '0', '1'),
(2, '租户默认部门名称', 'TENANT_DEFAULT_DEPTNAME', '租户默认部门', '0', '', ' ', ' ', '2020-05-12 03:36:32', NULL, '2', '1', '0'),
(3, '租户默认账户', 'TENANT_DEFAULT_USERNAME', 'admin', '0', '', ' ', ' ', '2020-05-12 04:05:04', NULL, '2', '1', '0'),
(4, '租户默认密码', 'TENANT_DEFAULT_PASSWORD', '123456', '0', '', ' ', ' ', '2020-05-12 04:05:24', NULL, '2', '1', '0'),
(5, '租户默认角色编码', 'TENANT_DEFAULT_ROLECODE', 'ROLE_ADMIN', '0', '', ' ', ' ', '2020-05-12 04:05:57', NULL, '2', '1', '0'),
(6, '租户默认角色名称', 'TENANT_DEFAULT_ROLENAME', '租户默认角色', '0', '', ' ', ' ', '2020-05-12 04:06:19', NULL, '2', '1', '0'),
(7, '表前缀', 'GEN_TABLE_PREFIX', 'tb_', '0', '', ' ', ' ', '2020-05-12 04:23:04', NULL, '9', '1', '0'),
(8, '接口文档不显示的字段', 'GEN_HIDDEN_COLUMNS', 'tenant_id', '0', '', ' ', ' ', '2020-05-12 04:25:19', NULL, '9', '1', '0'),
(9, '注册用户默认角色', 'USER_DEFAULT_ROLE', 'GENERAL_USER', '0', NULL, ' ', ' ', '2022-03-31 16:52:24', NULL, '2', '1', '0');

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
(1, 1400),
(1, 1401),
(1, 1402),
(1, 1403),
(1, 1600),
(1, 1601),
(1, 1602),
(1, 1603),
(1, 1604),
(1, 1605),
(1, 2000),
(1, 2001),
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
(1, 2300),
(1, 2400),
(1, 2401),
(1, 2402),
(1, 2403),
(1, 2600),
(1, 2601),
(1, 2800),
(1, 2810),
(1, 2820),
(1, 2830),
(1, 2840),
(1, 2850),
(1, 2860),
(1, 2870),
(1, 2871),
(1, 2906),
(1, 2907),
(1, 4000),
(1, 4001),
(1, 4002),
(1, 9000),
(1, 9005),
(1, 9006),
(1, 9007),
(1, 9050),
(1, 9051),
(1, 9052),
(1, 9053),
(1, 9054),
(1, 9055),
(1, 9056),
(1, 9057),
(1, 9059),
(1, 9060),
(1, 9061),
(1, 9062),
(1, 9063),
(1, 9064),
(1, 9065),
(1, 3000000000000010000),
(1, 3000000000000010100),
(1, 3000000000000010101),
(1, 3000000000000010102),
(1, 3000000000000010103),
(1, 3000000000000010104),
(1, 3000000000000010105),
(1, 3000000000000010106),
(1, 3000000000000010200),
(1, 3000000000000010201),
(1, 3000000000000010202),
(1, 3000000000000010203),
(1, 3000000000000010204),
(1, 3000000000000010205),
(1, 3000000000000010206),
(1, 3000000000000010300),
(1, 3000000000000010301),
(1, 3000000000000010302),
(1, 3000000000000010303),
(1, 3000000000000010304),
(1, 3000000000000010400),
(1, 3000000000000010401),
(1, 3000000000000010402),
(1, 3000000000000010403),
(1, 3000000000000010404),
(1, 3000000000000010500),
(1, 3000000000000010501),
(1, 3000000000000010502),
(2, 4000),
(2, 4001),
(2, 4002),
(3000000000000000101, 3000000000000010000),
(3000000000000000101, 3000000000000010100),
(3000000000000000101, 3000000000000010101),
(3000000000000000101, 3000000000000010102),
(3000000000000000101, 3000000000000010104),
(3000000000000000101, 3000000000000010105),
(3000000000000000101, 3000000000000010106),
(3000000000000000101, 3000000000000010200),
(3000000000000000101, 3000000000000010201),
(3000000000000000101, 3000000000000010202),
(3000000000000000101, 3000000000000010203),
(3000000000000000101, 3000000000000010204),
(3000000000000000101, 3000000000000010205),
(3000000000000000101, 3000000000000010206),
(3000000000000000101, 3000000000000010300),
(3000000000000000101, 3000000000000010301),
(3000000000000000101, 3000000000000010303),
(3000000000000000102, 3000000000000010000),
(3000000000000000102, 3000000000000010300),
(3000000000000000102, 3000000000000010301),
(3000000000000000102, 3000000000000010302),
(3000000000000000102, 3000000000000010303),
(3000000000000000102, 3000000000000010304),
(3000000000000000102, 3000000000000010500),
(3000000000000000102, 3000000000000010501),
(3000000000000000102, 3000000000000010502),
(3000000000000000103, 3000000000000010000),
(3000000000000000103, 3000000000000010300),
(3000000000000000103, 3000000000000010301),
(3000000000000000103, 3000000000000010303),
(3000000000000000103, 3000000000000010400),
(3000000000000000103, 3000000000000010401),
(3000000000000000103, 3000000000000010402),
(3000000000000000103, 3000000000000010403),
(3000000000000000103, 3000000000000010404);

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
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
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

INSERT INTO `sys_user` (`user_id`, `username`, `password`, `salt`, `phone`, `avatar`, `nickname`, `name`, `email`, `dept_id`, `create_by`, `update_by`, `create_time`, `update_time`, `lock_flag`, `del_flag`, `wx_openid`, `mini_openid`, `qq_openid`, `gitee_login`, `osc_id`) VALUES
(1, 'admin', '$2a$10$c/Ae0pRjJtMZg3BnvVpO.eIK6WYWVbKTzqgdy3afR7w.vd.xi3Mgy', '', '17034642999', '/admin/sys-file/s3demo/7ff4ca6b7bf446f3a5a13ac016dc21af.png', '管理员', '管理员', 'pig4cloud1@qq.com', 4, ' ', 'admin', '2018-04-20 07:15:18', '2026-03-06 11:24:47', '0', '0', NULL, 'oBxPy5E-v82xWGsfzZVzkD3wEX64', NULL, 'log4j', NULL),
(2000000000000000001, 'test_admin', '$2a$10$c/Ae0pRjJtMZg3BnvVpO.eIK6WYWVbKTzqgdy3afR7w.vd.xi3Mgy', '', '18800000001', '/admin/sys-file/s3demo/7ff4ca6b7bf446f3a5a13ac016dc21af.png', '测试管理员', '测试管理员', 'test_admin@example.com', 4, 'admin', 'admin', '2026-03-03 00:00:00', '2026-03-03 00:00:00', '0', '0', NULL, NULL, NULL, NULL, NULL),
(2000000000000000002, 'test_user', '$2a$10$c/Ae0pRjJtMZg3BnvVpO.eIK6WYWVbKTzqgdy3afR7w.vd.xi3Mgy', '', '18800000002', '/admin/sys-file/s3demo/7ff4ca6b7bf446f3a5a13ac016dc21af.png', '测试普通用户', '测试普通用户', 'test_user1@example.com', 4, 'admin', 'admin', '2026-03-03 00:00:00', '2026-03-06 11:06:24', '9', '0', NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- 表的结构 `sys_user_post`
--

DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户与岗位关联表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `sys_user_post`
--

INSERT INTO `sys_user_post` (`user_id`, `post_id`) VALUES
(1, 1),
(2000000000000000001, 1),
(2000000000000000002, 1);

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
(1, 2),
(2000000000000000001, 1),
(2000000000000000002, 1),
(2000000000000000002, 2);

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
-- 表的索引 `gen_datasource_conf`
--
ALTER TABLE `gen_datasource_conf`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- 表的索引 `gen_field_type`
--
ALTER TABLE `gen_field_type`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `column_type` (`column_type`);

--
-- 表的索引 `gen_group`
--
ALTER TABLE `gen_group`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `gen_table`
--
ALTER TABLE `gen_table`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `table_name` (`table_name`,`ds_name`) USING BTREE;

--
-- 表的索引 `gen_table_column`
--
ALTER TABLE `gen_table_column`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `gen_template`
--
ALTER TABLE `gen_template`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `gen_template_group`
--
ALTER TABLE `gen_template_group`
  ADD PRIMARY KEY (`group_id`,`template_id`);

--
-- 表的索引 `QRTZ_BLOB_TRIGGERS`
--
ALTER TABLE `QRTZ_BLOB_TRIGGERS`
  ADD PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`);

--
-- 表的索引 `QRTZ_CALENDARS`
--
ALTER TABLE `QRTZ_CALENDARS`
  ADD PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`);

--
-- 表的索引 `QRTZ_CRON_TRIGGERS`
--
ALTER TABLE `QRTZ_CRON_TRIGGERS`
  ADD PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`);

--
-- 表的索引 `QRTZ_FIRED_TRIGGERS`
--
ALTER TABLE `QRTZ_FIRED_TRIGGERS`
  ADD PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`);

--
-- 表的索引 `QRTZ_JOB_DETAILS`
--
ALTER TABLE `QRTZ_JOB_DETAILS`
  ADD PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`);

--
-- 表的索引 `QRTZ_LOCKS`
--
ALTER TABLE `QRTZ_LOCKS`
  ADD PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`);

--
-- 表的索引 `QRTZ_PAUSED_TRIGGER_GRPS`
--
ALTER TABLE `QRTZ_PAUSED_TRIGGER_GRPS`
  ADD PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`);

--
-- 表的索引 `QRTZ_SCHEDULER_STATE`
--
ALTER TABLE `QRTZ_SCHEDULER_STATE`
  ADD PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`);

--
-- 表的索引 `QRTZ_SIMPLE_TRIGGERS`
--
ALTER TABLE `QRTZ_SIMPLE_TRIGGERS`
  ADD PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`);

--
-- 表的索引 `QRTZ_SIMPROP_TRIGGERS`
--
ALTER TABLE `QRTZ_SIMPROP_TRIGGERS`
  ADD PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`);

--
-- 表的索引 `QRTZ_TRIGGERS`
--
ALTER TABLE `QRTZ_TRIGGERS`
  ADD PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  ADD KEY `SCHED_NAME` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`);

--
-- 表的索引 `sys_dept`
--
ALTER TABLE `sys_dept`
  ADD PRIMARY KEY (`dept_id`) USING BTREE;

--
-- 表的索引 `sys_dict`
--
ALTER TABLE `sys_dict`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `sys_dict_del_flag` (`del_flag`) USING BTREE;

--
-- 表的索引 `sys_dict_item`
--
ALTER TABLE `sys_dict_item`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `sys_dict_value` (`item_value`) USING BTREE,
  ADD KEY `sys_dict_label` (`label`) USING BTREE,
  ADD KEY `sys_dict_item_del_flag` (`del_flag`) USING BTREE;

--
-- 表的索引 `sys_file`
--
ALTER TABLE `sys_file`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- 表的索引 `sys_job`
--
ALTER TABLE `sys_job`
  ADD PRIMARY KEY (`job_id`) USING BTREE,
  ADD UNIQUE KEY `job_name_group_idx` (`job_name`,`job_group`) USING BTREE;

--
-- 表的索引 `sys_job_log`
--
ALTER TABLE `sys_job_log`
  ADD PRIMARY KEY (`job_log_id`) USING BTREE;

--
-- 表的索引 `sys_log`
--
ALTER TABLE `sys_log`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `sys_log_request_uri` (`request_uri`) USING BTREE,
  ADD KEY `sys_log_type` (`log_type`) USING BTREE,
  ADD KEY `sys_log_create_date` (`create_time`) USING BTREE;

--
-- 表的索引 `sys_menu`
--
ALTER TABLE `sys_menu`
  ADD PRIMARY KEY (`menu_id`) USING BTREE;

--
-- 表的索引 `sys_oauth_client_details`
--
ALTER TABLE `sys_oauth_client_details`
  ADD PRIMARY KEY (`id`) USING BTREE;

--
-- 表的索引 `sys_post`
--
ALTER TABLE `sys_post`
  ADD PRIMARY KEY (`post_id`) USING BTREE;

--
-- 表的索引 `sys_public_param`
--
ALTER TABLE `sys_public_param`
  ADD PRIMARY KEY (`public_id`) USING BTREE;

--
-- 表的索引 `sys_role`
--
ALTER TABLE `sys_role`
  ADD PRIMARY KEY (`role_id`) USING BTREE,
  ADD KEY `role_idx1_role_code` (`role_code`) USING BTREE;

--
-- 表的索引 `sys_role_menu`
--
ALTER TABLE `sys_role_menu`
  ADD PRIMARY KEY (`role_id`,`menu_id`) USING BTREE;

--
-- 表的索引 `sys_user`
--
ALTER TABLE `sys_user`
  ADD PRIMARY KEY (`user_id`) USING BTREE,
  ADD KEY `user_wx_openid` (`wx_openid`) USING BTREE,
  ADD KEY `user_qq_openid` (`qq_openid`) USING BTREE,
  ADD KEY `user_idx1_username` (`username`) USING BTREE;

--
-- 表的索引 `sys_user_post`
--
ALTER TABLE `sys_user_post`
  ADD PRIMARY KEY (`user_id`,`post_id`) USING BTREE;

--
-- 表的索引 `sys_user_role`
--
ALTER TABLE `sys_user_role`
  ADD PRIMARY KEY (`user_id`,`role_id`) USING BTREE;

--
-- 表的索引 `wm_address`
--
ALTER TABLE `wm_address`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_wm_address_user` (`user_id`) USING BTREE,
  ADD KEY `idx_wm_address_area` (`province`,`city`,`district`) USING BTREE;

--
-- 表的索引 `wm_customer_user_ext`
--
ALTER TABLE `wm_customer_user_ext`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_wm_customer_user` (`user_id`) USING BTREE,
  ADD KEY `idx_wm_customer_default_address` (`default_address_id`) USING BTREE;

--
-- 表的索引 `wm_delivery_order`
--
ALTER TABLE `wm_delivery_order`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_wm_delivery_order_id` (`order_id`) USING BTREE,
  ADD KEY `idx_wm_delivery_order_delivery_user` (`delivery_user_id`) USING BTREE,
  ADD KEY `idx_wm_delivery_order_status` (`delivery_status`) USING BTREE;

--
-- 表的索引 `wm_delivery_user_ext`
--
ALTER TABLE `wm_delivery_user_ext`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_wm_delivery_user` (`user_id`) USING BTREE,
  ADD KEY `idx_wm_delivery_status` (`online_status`,`employment_status`) USING BTREE;

--
-- 表的索引 `wm_dish`
--
ALTER TABLE `wm_dish`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_wm_dish_merchant` (`merchant_user_id`) USING BTREE,
  ADD KEY `idx_wm_dish_sale_status` (`sale_status`) USING BTREE;

--
-- 表的索引 `wm_merchant_user_ext`
--
ALTER TABLE `wm_merchant_user_ext`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_wm_merchant_user` (`user_id`) USING BTREE,
  ADD KEY `idx_wm_merchant_status` (`business_status`,`audit_status`) USING BTREE,
  ADD KEY `idx_wm_merchant_store_address` (`store_address_id`) USING BTREE;

--
-- 表的索引 `wm_order`
--
ALTER TABLE `wm_order`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_wm_order_no` (`order_no`) USING BTREE,
  ADD KEY `idx_wm_order_customer` (`customer_user_id`) USING BTREE,
  ADD KEY `idx_wm_order_merchant` (`merchant_user_id`) USING BTREE,
  ADD KEY `idx_wm_order_delivery` (`delivery_user_id`) USING BTREE,
  ADD KEY `idx_wm_order_status` (`order_status`) USING BTREE,
  ADD KEY `idx_wm_order_delivery_address` (`delivery_address_id`) USING BTREE;

--
-- 表的索引 `wm_order_item`
--
ALTER TABLE `wm_order_item`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_wm_order_item_order` (`order_id`) USING BTREE,
  ADD KEY `idx_wm_order_item_dish` (`dish_id`) USING BTREE;

--
-- 表的索引 `wm_order_pay`
--
ALTER TABLE `wm_order_pay`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_wm_order_pay_order` (`order_id`) USING BTREE,
  ADD KEY `idx_wm_order_pay_status` (`pay_status`) USING BTREE;

--
-- 限制导出的表
--

--
-- 限制表 `QRTZ_BLOB_TRIGGERS`
--
ALTER TABLE `QRTZ_BLOB_TRIGGERS`
  ADD CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`);

--
-- 限制表 `QRTZ_CRON_TRIGGERS`
--
ALTER TABLE `QRTZ_CRON_TRIGGERS`
  ADD CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`);

--
-- 限制表 `QRTZ_SIMPLE_TRIGGERS`
--
ALTER TABLE `QRTZ_SIMPLE_TRIGGERS`
  ADD CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`);

--
-- 限制表 `QRTZ_SIMPROP_TRIGGERS`
--
ALTER TABLE `QRTZ_SIMPROP_TRIGGERS`
  ADD CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`);

--
-- 限制表 `QRTZ_TRIGGERS`
--
ALTER TABLE `QRTZ_TRIGGERS`
  ADD CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`);

--
-- 限制表 `wm_customer_user_ext`
--
ALTER TABLE `wm_customer_user_ext`
  ADD CONSTRAINT `fk_wm_customer_default_address` FOREIGN KEY (`default_address_id`) REFERENCES `wm_address` (`id`);

--
-- 限制表 `wm_delivery_order`
--
ALTER TABLE `wm_delivery_order`
  ADD CONSTRAINT `fk_wm_delivery_order_order` FOREIGN KEY (`order_id`) REFERENCES `wm_order` (`id`);

--
-- 限制表 `wm_merchant_user_ext`
--
ALTER TABLE `wm_merchant_user_ext`
  ADD CONSTRAINT `fk_wm_merchant_store_address` FOREIGN KEY (`store_address_id`) REFERENCES `wm_address` (`id`);

--
-- 限制表 `wm_order`
--
ALTER TABLE `wm_order`
  ADD CONSTRAINT `fk_wm_order_delivery_address` FOREIGN KEY (`delivery_address_id`) REFERENCES `wm_address` (`id`);

--
-- 限制表 `wm_order_item`
--
ALTER TABLE `wm_order_item`
  ADD CONSTRAINT `fk_wm_order_item_dish` FOREIGN KEY (`dish_id`) REFERENCES `wm_dish` (`id`),
  ADD CONSTRAINT `fk_wm_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `wm_order` (`id`);

--
-- 限制表 `wm_order_pay`
--
ALTER TABLE `wm_order_pay`
  ADD CONSTRAINT `fk_wm_order_pay_order` FOREIGN KEY (`order_id`) REFERENCES `wm_order` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
