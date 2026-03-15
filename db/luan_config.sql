-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- 主机： luan-mysql:3306
-- 生成日期： 2026-03-11 09:33:22
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
-- 数据库： `luan_config`
--
CREATE DATABASE IF NOT EXISTS `luan_config` DEFAULT CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci;
USE `luan_config`;

-- --------------------------------------------------------

--
-- 表的结构 `config_info`
--

DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info` (
  `id` bigint NOT NULL COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'group_id',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'app_name',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'configuration description',
  `c_use` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'configuration usage',
  `effect` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '配置生效的描述',
  `type` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT '配置的类型',
  `c_schema` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin COMMENT '配置的模式',
  `encrypted_data_key` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT '密钥'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='config_info';

--
-- 转存表中的数据 `config_info`
--

INSERT INTO `config_info` (`id`, `data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`, `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `c_use`, `effect`, `type`, `c_schema`, `encrypted_data_key`) VALUES
(1, 'application-dev.yml', 'DEFAULT_GROUP', '# 配置文件加密根密码\njasypt:\n  encryptor:\n    password: pig\n    algorithm: PBEWithMD5AndDES\n    iv-generator-classname: org.jasypt.iv.NoIvGenerator\n    \n# Spring 相关\nspring:\n  cache:\n    type: redis\n  data:\n    redis:\n      host: ${REDIS_HOST:127.0.0.1}\n      password: ${REDIS_PASSWORD:}\n      port: ${REDIS_PORT:6379}\n      database: ${REDIS_DATABASE:0}\n  cloud:\n    sentinel:\n      eager: true\n      transport:\n        dashboard: luan-sentinel:5003\n    openfeign:\n      sentinel:\n        enabled: true\n      okhttp:\n        enabled: true\n      httpclient:\n        enabled: false\n      compression:\n        request:\n          enabled: true\n        response:\n          enabled: true\n\n# 暴露监控端点\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: \"*\"  \n  endpoint:\n    health:\n      show-details: ALWAYS\n\n# mybaits-plus配置\nmybatis-plus:\n  mapper-locations: classpath:/mapper/*Mapper.xml\n  global-config:\n    banner: false\n    db-config:\n      id-type: auto\n      table-underline: true\n      logic-delete-value: 1\n      logic-not-delete-value: 0\n  type-handlers-package: com.luan.takeaway.common.mybatis.handler\n  configuration:\n    map-underscore-to-camel-case: true\n    shrink-whitespaces-in-sql: true\n\n# 短信插件配置：https://www.yuque.com/vxixfq/pig/zw8udk\nsms:\n  is-print: false # 是否打印日志\n  config-type: yaml # 配置类型，yaml', 'f847289cb72a2856d11ae647825805dc', '2025-05-16 12:48:39', '2025-10-29 09:01:23', 'nacos', '10.25.25.1', '', 'public', '', NULL, NULL, 'yaml', NULL, ''),
(2, 'luan-auth-dev.yml', 'DEFAULT_GROUP', '# 数据源\nspring:\n  freemarker:\n    allow-request-override: false\n    allow-session-override: false\n    cache: true\n    charset: UTF-8\n    check-template-location: true\n    content-type: text/html\n    enabled: true\n    request-context-attribute: request\n    expose-request-attributes: false\n    expose-session-attributes: false\n    expose-spring-macro-helpers: true\n    prefer-file-system-access: true\n    suffix: .ftl\n    template-loader-path: classpath:/templates/\n\n\nsecurity:\n  encode-key: \'thanks,pig4cloud\'\n  ignore-clients:\n    - test\n    - client\n    - open\n    - app', 'b6aa19e677274151bc6f045e9a046a4e', '2025-01-30 16:50:04', '2025-01-30 16:50:04', 'nacos', '127.0.0.1', '', 'public', NULL, NULL, NULL, 'yaml', NULL, ''),
(4, 'luan-gateway-dev.yml', 'DEFAULT_GROUP', 'spring:\n  cloud:\n    gateway:\n      server:\n        webflux:\n          routes:\n            # 认证中心\n            - id: luan-auth\n              uri: lb://luan-auth\n              predicates:\n                - Path=/auth/**\n            #UPMS 模块\n            - id: luan-upms-biz\n              uri: lb://luan-upms-biz\n              predicates:\n                - Path=/admin/**\n              filters:\n                # 限流配置\n                - name: RequestRateLimiter\n                  args:\n                    key-resolver: \'#{@remoteAddrKeyResolver}\'\n                    redis-rate-limiter.replenishRate: 100\n                    redis-rate-limiter.burstCapacity: 200\n            # 外卖用户服务（合并后的统一入口）\n            - id: luan-takeaway-user-biz\n              uri: lb://luan-takeaway-user-biz\n              predicates:\n                - Path=/takeaway/user/**\n            # 外卖菜品服务\n            - id: luan-takeaway-dish-biz\n              uri: lb://luan-takeaway-dish-biz\n              predicates:\n                - Path=/takeaway/dish/**\n            # 外卖订单服务\n            - id: luan-takeaway-order-biz\n              uri: lb://luan-takeaway-order-biz\n              predicates:\n                - Path=/takeaway/order/**\n            # 外卖支付服务\n            - id: luan-takeaway-pay-biz\n              uri: lb://luan-takeaway-pay-biz\n              predicates:\n                - Path=/takeaway/pay/**\n            # AI 点餐助手服务\n            - id: luan-takeaway-ai-biz\n              uri: lb://luan-takeaway-ai-biz\n              predicates:\n                - Path=/takeaway/ai/**\n              filters:\n                - StripPrefix=1\n            # 固定路由转发配置 无修改\n            - id: openapi\n              uri: lb://luan-gateway\n              predicates:\n                - Path=/v3/api-docs/**\n              filters:\n                - RewritePath=/v3/api-docs/(?<path>.*), /$\\{path}/$\\{path}/v3/api-docs', 'a230ccddeabf24375fba01478e5085c5', '2025-01-30 16:50:04', '2025-05-30 08:36:27', 'nacos_namespace_migrate', '0:0:0:0:0:0:0:1', '', 'public', '', NULL, NULL, 'yaml', NULL, ''),
(6, 'luan-upms-biz-dev.yml', 'DEFAULT_GROUP', '# 数据源\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    username: ${MYSQL_USERNAME:root}\n    password: ${MYSQL_PASSWORD:root}\n    url: jdbc:mysql://${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}/${MYSQL_DB:luan}?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowMultiQueries=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true&allowPublicKeyRetrieval=true\n\n# 文件上传相关 支持阿里云、华为云、腾讯、minio\nfile:\n  bucketName: s3demo \n  local:\n    enable: true\n    base-path: /home/luan/data/takeaway/files', '551fa02b1b48a12e171bac427fb5429f', '2025-10-29 11:39:40', '2025-10-29 11:39:40', 'nacos', '10.25.25.2', '', 'public', '', NULL, NULL, 'yaml', NULL, ''),
(7, 'luan-takeaway-user-biz-dev.yml', 'DEFAULT_GROUP', '# 数据源\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    username: ${MYSQL_USERNAME:root}\n    password: ${MYSQL_PASSWORD:root}\n    url: jdbc:mysql://${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}/${MYSQL_DB:luan}?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowMultiQueries=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true&allowPublicKeyRetrieval=true\n', 'c3dd5be472ecbc031e24a808403b164e', '2026-03-11 09:33:22', '2026-03-11 09:33:22', 'nacos', '127.0.0.1', '', 'public', '', NULL, NULL, 'yaml', NULL, ''),
(8, 'luan-takeaway-dish-biz-dev.yml', 'DEFAULT_GROUP', '# 数据源\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    username: ${MYSQL_USERNAME:root}\n    password: ${MYSQL_PASSWORD:root}\n    url: jdbc:mysql://${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}/${MYSQL_DB:luan}?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowMultiQueries=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true&allowPublicKeyRetrieval=true\n', 'c3dd5be472ecbc031e24a808403b164e', '2026-03-11 09:33:22', '2026-03-11 09:33:22', 'nacos', '127.0.0.1', '', 'public', '', NULL, NULL, 'yaml', NULL, ''),
(9, 'luan-takeaway-order-biz-dev.yml', 'DEFAULT_GROUP', '# 数据源\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    username: ${MYSQL_USERNAME:root}\n    password: ${MYSQL_PASSWORD:root}\n    url: jdbc:mysql://${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}/${MYSQL_DB:luan}?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowMultiQueries=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true&allowPublicKeyRetrieval=true\n', 'c3dd5be472ecbc031e24a808403b164e', '2026-03-11 09:33:22', '2026-03-11 09:33:22', 'nacos', '127.0.0.1', '', 'public', '', NULL, NULL, 'yaml', NULL, ''),
(10, 'luan-takeaway-pay-biz-dev.yml', 'DEFAULT_GROUP', '# 数据源\nspring:\n  datasource:\n    type: com.zaxxer.hikari.HikariDataSource\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    username: ${MYSQL_USERNAME:root}\n    password: ${MYSQL_PASSWORD:root}\n    url: jdbc:mysql://${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}/${MYSQL_DB:luan}?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowMultiQueries=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true&allowPublicKeyRetrieval=true\n', 'c3dd5be472ecbc031e24a808403b164e', '2026-03-11 09:33:22', '2026-03-11 09:33:22', 'nacos', '127.0.0.1', '', 'public', '', NULL, NULL, 'yaml', NULL, ''),
(11, 'luan-takeaway-ai-biz-dev.yml', 'DEFAULT_GROUP', '# AI 点餐助手配置\nai:\n  assistant:\n    max-recommendation: 5\n    llm:\n      enabled: ${AI_LLM_ENABLED:false}\n      source: ${AI_LLM_SOURCE:local}\n      base-url: ${AI_LLM_BASE_URL:http://127.0.0.1:11434/v1}\n      api-key: ${AI_LLM_API_KEY:}\n      model: ${AI_LLM_MODEL:qwen2.5:7b}\n      local-base-url: ${AI_LLM_LOCAL_BASE_URL:http://127.0.0.1:8000/v1}\n      local-api-key: ${AI_LLM_LOCAL_API_KEY:EMPTY}\n      local-model: ${AI_LLM_LOCAL_MODEL:qwen3.5-4b}\n      remote-base-url: ${AI_LLM_REMOTE_BASE_URL:}\n      remote-api-key: ${AI_LLM_REMOTE_API_KEY:}\n      remote-model: ${AI_LLM_REMOTE_MODEL:}\n      timeout-ms: ${AI_LLM_TIMEOUT_MS:30000}\n', NULL, '2026-03-15 16:50:00', '2026-03-15 16:50:00', 'nacos', '127.0.0.1', '', 'public', '', NULL, NULL, 'yaml', NULL, '');

-- --------------------------------------------------------

--
-- 表的结构 `config_info_aggr`
--

DROP TABLE IF EXISTS `config_info_aggr`;
CREATE TABLE `config_info_aggr` (
  `id` bigint NOT NULL COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'datum_id',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'content',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'app_name',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '' COMMENT '租户字段'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='增加租户字段';

-- --------------------------------------------------------

--
-- 表的结构 `config_info_beta`
--

DROP TABLE IF EXISTS `config_info_beta`;
CREATE TABLE `config_info_beta` (
  `id` bigint NOT NULL COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'content',
  `beta_ips` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'betaIps',
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'source ip',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT '密钥'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='config_info_beta';

-- --------------------------------------------------------

--
-- 表的结构 `config_info_gray`
--

DROP TABLE IF EXISTS `config_info_gray`;
CREATE TABLE `config_info_gray` (
  `id` bigint UNSIGNED NOT NULL COMMENT 'id',
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) NOT NULL COMMENT 'group_id',
  `content` longtext NOT NULL COMMENT 'content',
  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
  `src_user` text COMMENT 'src_user',
  `src_ip` varchar(100) DEFAULT NULL COMMENT 'src_ip',
  `gmt_create` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'gmt_create',
  `gmt_modified` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'gmt_modified',
  `app_name` varchar(128) DEFAULT NULL COMMENT 'app_name',
  `tenant_id` varchar(128) DEFAULT '' COMMENT 'tenant_id',
  `gray_name` varchar(128) NOT NULL COMMENT 'gray_name',
  `gray_rule` text NOT NULL COMMENT 'gray_rule',
  `encrypted_data_key` varchar(256) NOT NULL DEFAULT '' COMMENT 'encrypted_data_key'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='config_info_gray';

-- --------------------------------------------------------

--
-- 表的结构 `config_info_tag`
--

DROP TABLE IF EXISTS `config_info_tag`;
CREATE TABLE `config_info_tag` (
  `id` bigint NOT NULL COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '' COMMENT 'tenant_id',
  `tag_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'tag_id',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'source ip'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='config_info_tag';

-- --------------------------------------------------------

--
-- 表的结构 `config_tags_relation`
--

DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation` (
  `id` bigint NOT NULL COMMENT 'id',
  `tag_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint NOT NULL COMMENT 'nid, 自增长标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='config_tag_relation';

-- --------------------------------------------------------

--
-- 表的结构 `group_capacity`
--

DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity` (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数，，0表示使用默认值',
  `max_aggr_size` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='集群、各Group容量信息表';

-- --------------------------------------------------------

--
-- 表的结构 `his_config_info`
--

DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info` (
  `id` bigint UNSIGNED NOT NULL COMMENT 'id',
  `nid` bigint UNSIGNED NOT NULL COMMENT 'nid, 自增标识',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'source ip',
  `op_type` char(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'operation type',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT '密钥',
  `publish_type` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT 'formal' COMMENT 'publish type gray or formal',
  `gray_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'gray name',
  `ext_info` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin COMMENT 'ext info'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='多租户改造';

-- --------------------------------------------------------

--
-- 表的结构 `permissions`
--

DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `role` varchar(50) NOT NULL COMMENT 'role',
  `resource` varchar(128) NOT NULL COMMENT 'resource',
  `action` varchar(8) NOT NULL COMMENT 'action'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- --------------------------------------------------------

--
-- 表的结构 `roles`
--

DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `username` varchar(50) NOT NULL COMMENT 'username',
  `role` varchar(50) NOT NULL COMMENT 'role'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- 转存表中的数据 `roles`
--

INSERT INTO `roles` (`username`, `role`) VALUES
('nacos', 'ROLE_ADMIN');

-- --------------------------------------------------------

--
-- 表的结构 `tenant_capacity`
--

DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity` (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键ID',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '配额，0表示使用默认值',
  `usage` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '使用量',
  `max_size` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '聚合子配置最大个数',
  `max_aggr_size` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='租户容量信息表';

--
-- 转存表中的数据 `tenant_capacity`
--

INSERT INTO `tenant_capacity` (`id`, `tenant_id`, `quota`, `usage`, `max_size`, `max_aggr_count`, `max_aggr_size`, `max_history_count`, `gmt_create`, `gmt_modified`) VALUES
(1, 'public', 0, 0, 0, 0, 0, 0, '2026-03-05 03:49:34', '2026-03-05 03:49:34');

-- --------------------------------------------------------

--
-- 表的结构 `tenant_info`
--

DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info` (
  `id` bigint NOT NULL COMMENT 'id',
  `kp` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint NOT NULL COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin COMMENT='tenant_info';

--
-- 转存表中的数据 `tenant_info`
--

INSERT INTO `tenant_info` (`id`, `kp`, `tenant_id`, `tenant_name`, `tenant_desc`, `create_source`, `gmt_create`, `gmt_modified`) VALUES
(1, '1', 'public', 'public', 'public namespace', 'nacos-init', 1772682574120, 1772682574120);

-- --------------------------------------------------------

--
-- 表的结构 `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `username` varchar(50) NOT NULL COMMENT 'username',
  `password` varchar(500) NOT NULL COMMENT 'password',
  `enabled` tinyint(1) NOT NULL COMMENT 'enabled'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- 转存表中的数据 `users`
--

INSERT INTO `users` (`username`, `password`, `enabled`) VALUES
('nacos', '$2a$10$W6PKgRTzXUp6R/NY853Kn.nRaIcX3whIMTZ/WWkNqo2MTOeSBjKJq', 1);

--
-- 转储表的索引
--

--
-- 表的索引 `config_info`
--
ALTER TABLE `config_info`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`);

--
-- 表的索引 `config_info_aggr`
--
ALTER TABLE `config_info_aggr`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`);

--
-- 表的索引 `config_info_beta`
--
ALTER TABLE `config_info_beta`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_configinfobeta_datagrouptenant` (`data_id`,`group_id`,`tenant_id`);

--
-- 表的索引 `config_info_gray`
--
ALTER TABLE `config_info_gray`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_configinfogray_datagrouptenantgray` (`data_id`,`group_id`,`tenant_id`,`gray_name`),
  ADD KEY `idx_dataid_gmt_modified` (`data_id`,`gmt_modified`),
  ADD KEY `idx_gmt_modified` (`gmt_modified`);

--
-- 表的索引 `config_info_tag`
--
ALTER TABLE `config_info_tag`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_configinfotag_datagrouptenanttag` (`data_id`,`group_id`,`tenant_id`,`tag_id`);

--
-- 表的索引 `config_tags_relation`
--
ALTER TABLE `config_tags_relation`
  ADD PRIMARY KEY (`nid`),
  ADD UNIQUE KEY `uk_configtagrelation_configidtag` (`id`,`tag_name`,`tag_type`),
  ADD KEY `idx_tenant_id` (`tenant_id`);

--
-- 表的索引 `group_capacity`
--
ALTER TABLE `group_capacity`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_group_id` (`group_id`);

--
-- 表的索引 `his_config_info`
--
ALTER TABLE `his_config_info`
  ADD PRIMARY KEY (`nid`),
  ADD KEY `idx_gmt_create` (`gmt_create`),
  ADD KEY `idx_gmt_modified` (`gmt_modified`),
  ADD KEY `idx_did` (`data_id`);

--
-- 表的索引 `permissions`
--
ALTER TABLE `permissions`
  ADD UNIQUE KEY `uk_role_permission` (`role`,`resource`,`action`) USING BTREE;

--
-- 表的索引 `roles`
--
ALTER TABLE `roles`
  ADD UNIQUE KEY `idx_user_role` (`username`,`role`) USING BTREE;

--
-- 表的索引 `tenant_capacity`
--
ALTER TABLE `tenant_capacity`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_tenant_id` (`tenant_id`);

--
-- 表的索引 `tenant_info`
--
ALTER TABLE `tenant_info`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_tenant_info_kptenantid` (`kp`,`tenant_id`),
  ADD KEY `idx_tenant_id` (`tenant_id`);

--
-- 表的索引 `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`username`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `config_info`
--
ALTER TABLE `config_info`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id', AUTO_INCREMENT=16;

--
-- 使用表AUTO_INCREMENT `config_info_aggr`
--
ALTER TABLE `config_info_aggr`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';

--
-- 使用表AUTO_INCREMENT `config_info_beta`
--
ALTER TABLE `config_info_beta`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';

--
-- 使用表AUTO_INCREMENT `config_info_gray`
--
ALTER TABLE `config_info_gray`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id';

--
-- 使用表AUTO_INCREMENT `config_info_tag`
--
ALTER TABLE `config_info_tag`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';

--
-- 使用表AUTO_INCREMENT `config_tags_relation`
--
ALTER TABLE `config_tags_relation`
  MODIFY `nid` bigint NOT NULL AUTO_INCREMENT COMMENT 'nid, 自增长标识';

--
-- 使用表AUTO_INCREMENT `group_capacity`
--
ALTER TABLE `group_capacity`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID';

--
-- 使用表AUTO_INCREMENT `his_config_info`
--
ALTER TABLE `his_config_info`
  MODIFY `nid` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'nid, 自增标识', AUTO_INCREMENT=8;

--
-- 使用表AUTO_INCREMENT `tenant_capacity`
--
ALTER TABLE `tenant_capacity`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID', AUTO_INCREMENT=2;

--
-- 使用表AUTO_INCREMENT `tenant_info`
--
ALTER TABLE `tenant_info`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id', AUTO_INCREMENT=2;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;