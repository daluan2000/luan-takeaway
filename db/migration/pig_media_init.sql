USE `pig`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `media_file`;
CREATE TABLE `media_file` (
  `id` bigint NOT NULL COMMENT '主键',
  `owner_id` bigint NOT NULL COMMENT '所属用户ID',
  `bucket_name` varchar(128) NOT NULL COMMENT '存储桶',
  `object_key` varchar(512) NOT NULL COMMENT '对象键',
  `origin_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `content_type` varchar(128) DEFAULT NULL COMMENT '内容类型',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `md5` varchar(64) DEFAULT NULL COMMENT '文件md5',
  `width` int DEFAULT NULL COMMENT '宽',
  `height` int DEFAULT NULL COMMENT '高',
  `status` char(1) DEFAULT '0' COMMENT '状态',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_media_file_owner_time` (`owner_id`, `create_time`) USING BTREE,
  KEY `idx_media_file_object_key` (`object_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片元数据';

DROP TABLE IF EXISTS `media_album`;
CREATE TABLE `media_album` (
  `id` bigint NOT NULL COMMENT '主键',
  `owner_id` bigint NOT NULL COMMENT '拥有者ID',
  `name` varchar(128) NOT NULL COMMENT '相册名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `cover_file_id` bigint DEFAULT NULL COMMENT '封面图片ID',
  `visible_scope` varchar(32) DEFAULT 'private' COMMENT '可见范围',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_media_album_owner_time` (`owner_id`, `create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册';

DROP TABLE IF EXISTS `media_album_item`;
CREATE TABLE `media_album_item` (
  `id` bigint NOT NULL COMMENT '主键',
  `album_id` bigint NOT NULL COMMENT '相册ID',
  `file_id` bigint NOT NULL COMMENT '图片ID',
  `sort_no` int DEFAULT '0' COMMENT '排序',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_media_album_item` (`album_id`, `file_id`),
  KEY `idx_media_album_item_sort` (`album_id`, `sort_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册图片关系';

DROP TABLE IF EXISTS `media_album_member`;
CREATE TABLE `media_album_member` (
  `id` bigint NOT NULL COMMENT '主键',
  `album_id` bigint NOT NULL COMMENT '相册ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(16) NOT NULL COMMENT '角色 owner/editor/viewer',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_media_album_member` (`album_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册协作者';

DROP TABLE IF EXISTS `media_share_link`;
CREATE TABLE `media_share_link` (
  `id` bigint NOT NULL COMMENT '主键',
  `share_token` varchar(64) NOT NULL COMMENT '分享token',
  `owner_id` bigint NOT NULL COMMENT '拥有者ID',
  `album_id` bigint NOT NULL COMMENT '相册ID',
  `code` varchar(64) DEFAULT NULL COMMENT '提取码',
  `expire_at` datetime DEFAULT NULL COMMENT '过期时间',
  `max_view_count` int DEFAULT '1000' COMMENT '最大访问次数',
  `current_view_count` int DEFAULT '0' COMMENT '当前访问次数',
  `status` char(1) DEFAULT '0' COMMENT '状态 0正常 1关闭',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_media_share_token` (`share_token`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册分享链接';

DROP TABLE IF EXISTS `media_share_access_log`;
CREATE TABLE `media_share_access_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `share_id` bigint NOT NULL COMMENT '分享ID',
  `visitor_ip` varchar(64) DEFAULT NULL COMMENT '访问IP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '访问UA',
  `access_time` datetime DEFAULT NULL COMMENT '访问时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_media_share_access_time` (`share_id`, `access_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享访问日志';

SET FOREIGN_KEY_CHECKS = 1;
