USE `pig`;

SET NAMES utf8mb4;

INSERT INTO `sys_menu` (`menu_id`, `name`, `en_name`, `permission`, `path`, `parent_id`, `icon`, `visible`, `sort_order`, `keep_alive`, `embedded`, `menu_type`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`)
VALUES
(1700, '图床管理', 'media', NULL, '/media', 1000, 'ele-Picture', '1', 6, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
(1701, '图片上传', NULL, 'media_file_upload', NULL, 1700, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(1702, '图片删除', NULL, 'media_file_del', NULL, 1700, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(1703, '相册新增', NULL, 'media_album_add', NULL, 1700, NULL, '1', 3, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(1704, '相册编辑', NULL, 'media_album_edit', NULL, 1700, NULL, '1', 4, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(1705, '相册删除', NULL, 'media_album_del', NULL, 1700, NULL, '1', 5, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(1706, '分享新增', NULL, 'media_share_add', NULL, 1700, NULL, '1', 6, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(1707, '分享关闭', NULL, 'media_share_del', NULL, 1700, NULL, '1', 7, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`path` = VALUES(`path`),
`parent_id` = VALUES(`parent_id`),
`update_time` = NOW(),
`del_flag` = '0';

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES
(1, 1700),
(1, 1701),
(1, 1702),
(1, 1703),
(1, 1704),
(1, 1705),
(1, 1706),
(1, 1707),
(2, 1700),
(2, 1701),
(2, 1702),
(2, 1703),
(2, 1704),
(2, 1705),
(2, 1706),
(2, 1707),
ON DUPLICATE KEY UPDATE
`menu_id` = VALUES(`menu_id`);
