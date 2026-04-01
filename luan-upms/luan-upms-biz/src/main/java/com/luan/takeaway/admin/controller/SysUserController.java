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

package com.luan.takeaway.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.admin.api.dto.UserDTO;
import com.luan.takeaway.admin.api.dto.UserInfo;
import com.luan.takeaway.admin.api.dto.BatchRegisterUserRequest;
import com.luan.takeaway.admin.api.dto.BatchRegisterResult;
import com.luan.takeaway.admin.api.entity.SysUser;
import com.luan.takeaway.admin.api.vo.UserExcelVO;
import com.luan.takeaway.admin.api.vo.UserVO;
import com.luan.takeaway.admin.service.SysUserService;
import com.luan.takeaway.common.core.constant.CommonConstants;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.log.annotation.SysLog;
import com.luan.takeaway.common.security.annotation.HasPermission;
import com.luan.takeaway.common.security.annotation.Inner;
import com.luan.takeaway.common.security.util.SecurityUtils;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 *
 * @author lengleng
 * @date 2025/05/30
 */
@RestController
@AllArgsConstructor
@RequestMapping("/user")
@Tag(description = "user", name = "用户管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SysUserController {

	private final SysUserService userService;

	/**
	 * 查询用户信息
	 * @param userDTO 用户信息查询参数
	 * @return 包含用户信息的R对象
	 */
	@Inner
	@GetMapping(value = { "/info/query" })
	@Operation(summary = "查询用户信息", description = "查询用户信息")
	public R<UserInfo> info(UserDTO userDTO) {
		return userService.getUserInfo(userDTO);
	}

	/**
	 * 获取当前登录用户的全部信息
	 * @return 包含用户信息的响应结果
	 */
	@GetMapping(value = { "/info" })
	@Operation(summary = "获取当前登录用户的全部信息", description = "获取当前登录用户的全部信息")
	public R<UserInfo> info() {
		String username = SecurityUtils.getUser().getUsername();
		UserDTO userDTO = new UserDTO();
		userDTO.setUsername(username);
		// 获取用户信息，不返回数据库密码字段
		R<UserInfo> userInfoR = userService.getUserInfo(userDTO);
		if (userInfoR.getData() != null) {
			userInfoR.getData().setPassword(null);
		}
		return userInfoR;
	}

	/**
	 * 通过ID查询用户信息
	 * @param id 用户ID
	 * @return 包含用户信息的响应对象
	 */
	@GetMapping("/details/{id}")
	@Operation(summary = "通过ID查询用户信息", description = "通过ID查询用户信息")
	public R<UserVO> user(@PathVariable Long id) {
		return R.ok(userService.getUserById(id));
	}

	/**
	 * 查询用户详细信息
	 * @param query 用户查询条件对象
	 * @return 包含查询结果的响应对象，用户不存在时返回null
	 */
	@Inner(value = false)
	@GetMapping("/details")
	@Operation(summary = "查询用户详细信息", description = "查询用户详细信息")
	public R<?> getDetails(@ParameterObject SysUser query) {
		SysUser sysUser = userService.getOne(Wrappers.query(query), false);
		return R.ok(sysUser == null ? null : CommonConstants.SUCCESS);
	}

	/**
	 * 删除用户信息
	 * @param ids 用户ID数组
	 * @return 操作结果
	 */
	@SysLog("删除用户信息")
	@DeleteMapping
	@HasPermission("sys_user_del")
	@Operation(summary = "根据ID删除用户", description = "根据ID删除用户")
	public R<Boolean> userDel(@RequestBody Long[] ids) {
		return R.ok(userService.removeUserByIds(ids));
	}

	/**
	 * 添加用户
	 * @param userDto 用户信息DTO
	 * @return 操作结果，成功返回success，失败返回false
	 */
	@SysLog("添加用户")
	@PostMapping
	@HasPermission("sys_user_add")
	@Operation(summary = "添加用户", description = "添加用户")
	public R<Boolean> saveUser(@RequestBody UserDTO userDto) {
		return R.ok(userService.saveUser(userDto));
	}

	/**
	 * 更新用户信息
	 * @param userDto 用户信息DTO对象
	 * @return 包含操作结果的R对象
	 */
	@SysLog("更新用户信息")
	@PutMapping
	@HasPermission("sys_user_edit")
	@Operation(summary = "更新用户信息", description = "更新用户信息")
	public R<Boolean> updateUser(@Valid @RequestBody UserDTO userDto) {
		return R.ok(userService.updateUser(userDto));
	}

	@SysLog("更新角色")
	@PutMapping("/role")
	@Operation(summary = "更新角色", description = "更新角色，禁止新增或删除admin角色")
	public R<Boolean> updateUserRole(@Valid @RequestBody UserDTO userDto) {
		return userService.updateUserRole(userDto);
	}

	/**
	 * 分页查询用户
	 * @param page 参数集
	 * @param userDTO 查询参数列表
	 * @return 用户集合
	 */
	@GetMapping("/page")
	@Operation(summary = "分页查询用户", description = "分页查询用户")
	public R<IPage<?>> getUserPage(@ParameterObject Page<?> page, @ParameterObject UserDTO userDTO) {
		return R.ok(userService.getUsersWithRolePage(page, userDTO));
	}

	/**
	 * 修改个人信息
	 * @param userDto 用户信息传输对象
	 * @return 操作结果，成功返回success，失败返回false
	 */
	@SysLog("修改个人信息")
	@PutMapping("/edit")
	@Operation(summary = "修改个人信息", description = "修改个人信息")
	public R<Boolean> updateUserInfo(@Valid @RequestBody UserDTO userDto) {
		return userService.updateUserInfo(userDto);
	}

	/**
	 * 导出用户数据到Excel表格
	 * @param userDTO 用户查询条件
	 * @return 用户数据列表
	 */
	@ResponseExcel
	@GetMapping("/export")
	@HasPermission("sys_user_export")
	@Operation(summary = "导出用户数据到Excel表格", description = "导出用户数据到Excel表格")
	public List<UserExcelVO> exportUsers(UserDTO userDTO) {
		return userService.listUsers(userDTO);
	}

	/**
	 * 锁定指定用户
	 * @param username 用户名
	 * @return 操作结果
	 */
	@PutMapping("/lock/{username}")
	@Operation(summary = "锁定指定用户", description = "锁定指定用户")
	public R<Boolean> lockUser(@PathVariable String username) {
		return userService.lockUser(username);
	}

	/**
	 * 修改当前用户密码
	 * @param userDto 用户数据传输对象，包含新密码等信息
	 * @return 操作结果
	 */
	@PutMapping("/password")
	@Operation(summary = "修改当前用户密码", description = "修改当前用户密码")
	public R<?> password(@RequestBody UserDTO userDto) {
		String username = SecurityUtils.getUser().getUsername();
		userDto.setUsername(username);
		return userService.changePassword(userDto);
	}

	/**
	 * 检查密码是否符合要求
	 * @param password 待检查的密码
	 * @return 检查结果
	 */
	@PostMapping("/check")
	@Operation(summary = "检查密码是否符合要求", description = "检查密码是否符合要求")
	public R<?> check(String password) {
		return userService.checkPassword(password);
	}

	/**
	 * 批量注册用户
	 *
	 * <p>功能说明：管理员批量注册系统用户，支持一次性创建多个用户并分配角色。
	 * 返回每个用户的注册结果，管理员可根据返回的失败原因处理异常数据。
	 *
	 * <p>权限控制：需拥有 sys_user_batch_register 权限
	 * <p>接口地址：POST /user/batch/register
	 *
	 * @param request 批量注册请求，包含用户列表
	 * @return 批量注册结果，包含总数、成功数、失败数及每条明细
	 * @see BatchRegisterUserRequest
	 * @see BatchRegisterResult
	 */
	@SysLog("批量注册用户")
	@PostMapping("/batch/register")
	@HasPermission("sys_user_batch_register")
	@Operation(summary = "批量注册用户", description = "批量注册用户")
	public R<BatchRegisterResult> batchRegister(@RequestBody BatchRegisterUserRequest request) {
		return R.ok(userService.batchRegister(request));
	}

}
