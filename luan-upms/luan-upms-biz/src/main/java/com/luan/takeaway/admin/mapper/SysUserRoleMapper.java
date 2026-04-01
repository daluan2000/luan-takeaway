/*
 *
 *      Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the pig4cloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lengleng (wangiegie@gmail.com)
 *
 */

package com.luan.takeaway.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luan.takeaway.admin.api.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

/**
 * <p>
 * 用户角色表 Mapper 接口
 * </p>
 *
 * @author lengleng
 * @since 2017-10-29
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

	/**
	 * 批量插入用户角色关系
	 * @param userRoles 用户角色关系列表
	 * @return 插入的行数
	 */
	@Insert("<script>" +
		"INSERT INTO sys_user_role (user_id, role_id) VALUES " +
		"<foreach collection='userRoles' item='item' separator=','>" +
		"(#{item.userId}, #{item.roleId})" +
		"</foreach>" +
		"</script>")
	int insertBatch(@Param("userRoles") List<SysUserRole> userRoles);

}
