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

package com.luan.takeaway.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luan.takeaway.admin.api.dto.RegisterUserDTO;
import com.luan.takeaway.admin.api.dto.UserDTO;
import com.luan.takeaway.admin.api.dto.UserInfo;
import com.luan.takeaway.admin.api.dto.BatchRegisterUserRequest;
import com.luan.takeaway.admin.api.dto.BatchRegisterUserDTO;
import com.luan.takeaway.admin.api.dto.BatchRegisterResult;
import com.luan.takeaway.admin.api.dto.BatchRegisterResultItem;
import com.luan.takeaway.admin.api.entity.SysMenu;
import com.luan.takeaway.admin.api.entity.SysRole;
import com.luan.takeaway.admin.api.entity.SysUser;
import com.luan.takeaway.admin.api.entity.SysUserRole;
import com.luan.takeaway.admin.api.util.ParamResolver;
import com.luan.takeaway.admin.api.vo.UserExcelVO;
import com.luan.takeaway.admin.api.vo.UserVO;
import com.luan.takeaway.admin.mapper.SysUserMapper;
import com.luan.takeaway.admin.mapper.SysUserRoleMapper;
import com.luan.takeaway.admin.service.*;
import com.luan.takeaway.common.core.constant.CacheConstants;
import com.luan.takeaway.common.core.constant.CommonConstants;
import com.luan.takeaway.common.core.exception.ErrorCodes;
import com.luan.takeaway.common.core.util.MsgUtils;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.security.service.PigUser;
import com.luan.takeaway.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 系统用户服务实现类
 *
 * @author lengleng
 * @date 2025/05/30
 */
@Slf4j
@Service
@AllArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

	private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

	private final SysMenuService sysMenuService;

	private final SysRoleService sysRoleService;

	private final SysUserRoleService sysUserRoleService;

	private final SysUserRoleMapper sysUserRoleMapper;

	private final CacheManager cacheManager;

	/**
	 * 保存用户信息
	 * @param userDto 用户数据传输对象
	 * @return 操作是否成功
	 * @throws Exception 事务回滚时抛出异常
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveUser(UserDTO userDto) {
		Objects.requireNonNull(userDto, "userDto must not be null");
		SysUser sysUser = new SysUser();
		BeanUtils.copyProperties(userDto, sysUser);
		sysUser.setDelFlag(CommonConstants.STATUS_NORMAL);
		sysUser.setCreateBy(userDto.getUsername());
		sysUser.setPassword(ENCODER.encode(userDto.getPassword()));
		baseMapper.insert(sysUser);

		// 如果角色为空，赋默认角色
		if (CollUtil.isEmpty(userDto.getRole())) {
			// 获取默认角色编码
			String defaultRole = ParamResolver.getStr("USER_DEFAULT_ROLE");
			// 默认角色
			SysRole sysRole = sysRoleService
				.getOne(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleCode, defaultRole));
			userDto.setRole(Collections.singletonList(sysRole.getRoleId()));
		}

		// 插入用户角色关系表
		userDto.getRole().forEach(roleId -> {
			SysUserRole userRole = new SysUserRole();
			userRole.setUserId(sysUser.getUserId());
			userRole.setRoleId(roleId);
			sysUserRoleMapper.insert(userRole);
		});
		return Boolean.TRUE;
	}

	/**
	 * 查询用户全部信息，包括角色和权限
	 * @param query 用户查询条件
	 * @return 包含用户角色和权限的用户信息对象
	 */
	@Override
	public R<UserInfo> getUserInfo(UserDTO query) {
		UserVO dbUser = baseMapper.getUser(query);

		if (dbUser == null) {
			return R.failed(MsgUtils.getMessage(ErrorCodes.SYS_USER_USERINFO_EMPTY, query.getUsername()));
		}

		UserInfo userInfo = new UserInfo();
		BeanUtils.copyProperties(dbUser, userInfo);
		// 设置权限列表（menu.permission）
		List<String> permissions = dbUser.getRoleList()
			.stream()
			.map(SysRole::getRoleId)
			.flatMap(roleId -> sysMenuService.findMenuByRoleId(roleId).stream())
			.filter(menu -> StrUtil.isNotEmpty(menu.getPermission()))
			.map(SysMenu::getPermission)
			.toList();
		userInfo.setPermissions(permissions);
		return R.ok(userInfo);
	}

	/**
	 * 分页查询用户信息（包含角色信息）
	 * @param page 分页对象
	 * @param userDTO 查询参数
	 * @return 包含用户和角色信息的分页结果
	 */
	@Override
	public IPage<?> getUsersWithRolePage(Page<?> page, UserDTO userDTO) {
		return baseMapper.getUsersPage(page, userDTO);
	}

	/**
	 * 通过ID查询用户信息
	 * @param id 用户ID
	 * @return 用户信息VO对象
	 */
	@Override
	public UserVO getUserById(Long id) {
		UserDTO query = new UserDTO();
		query.setUserId(id);
		return baseMapper.getUser(query);
	}

	/**
	 * 根据用户ID列表删除用户及相关缓存
	 * @param ids 用户ID数组
	 * @return 删除成功返回true
	 * @throws Exception 事务回滚时抛出异常
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeUserByIds(Long[] ids) {
		List<Long> idList = CollUtil.toList(ids);
		// 删除 spring cache
		Cache cache = cacheManager.getCache(CacheConstants.USER_DETAILS);
		if (cache != null) {
			baseMapper.selectByIds(idList).forEach(user -> {
				String username = user.getUsername();
				if (username != null) {
					cache.evictIfPresent(username);
				}
			});
		}

		sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getUserId, idList));
		this.removeBatchByIds(idList);
		return Boolean.TRUE;
	}

	/**
	 * 更新用户信息
	 * @param userDto 用户数据传输对象
	 * @return 操作结果，包含更新是否成功
	 */
	@Override
	@CacheEvict(value = CacheConstants.USER_DETAILS, key = "#userDto.username")
	public R<Boolean> updateUserInfo(UserDTO userDto) {
		SysUser sysUser = new SysUser();
		sysUser.setPhone(userDto.getPhone());
		sysUser.setUserId(SecurityUtils.getUser().getId());
		sysUser.setAvatar(userDto.getAvatar());
		sysUser.setNickname(userDto.getNickname());
		sysUser.setName(userDto.getName());
		sysUser.setEmail(userDto.getEmail());
		return R.ok(this.updateById(sysUser));
	}

	/**
	 * 更新用户角色信息 禁止新增或删除admin角色
	 * @param userDto 用户数据传输对象
	 * @return 操作结果，包含更新是否成功
	 */
	@Override
	@CacheEvict(value = CacheConstants.USER_DETAILS, key = "#userDto.username")
	public R<Boolean> updateUserRole(UserDTO userDto) {

		if (CollUtil.isEmpty(userDto.getRole())) {
			return R.failed("角色ID列表不能为空");
		}

		PigUser user = SecurityUtils.getUser();
		List<Long> currentRoleIds = sysUserRoleService.list(Wrappers.<SysUserRole>lambdaQuery()
			.eq(SysUserRole::getUserId, user.getId()))
			.stream()
			.map(SysUserRole::getRoleId)
			.toList();
		List<SysRole> currentRoles = CollUtil.isEmpty(currentRoleIds)
			? Collections.emptyList()
			: sysRoleService.listByIds(currentRoleIds);

		List<SysRole> roles = sysRoleService.listByIds(userDto.getRole());

		Set<Long> currentAdminRoleIds = currentRoles.stream()
			.filter(role -> StrUtil.containsIgnoreCase(role.getRoleCode(), "admin"))
			.map(SysRole::getRoleId)
			.collect(Collectors.toSet());
		Set<Long> targetAdminRoleIds = roles.stream()
			.filter(role -> StrUtil.containsIgnoreCase(role.getRoleCode(), "admin"))
			.map(SysRole::getRoleId)
			.collect(Collectors.toSet());

		if (!currentAdminRoleIds.equals(targetAdminRoleIds)) {
			return R.failed("禁止新增或删除admin角色");
		}

		UserDTO ud = new UserDTO();
		ud.setUserId(user.getId());
		ud.setRole(userDto.getRole());
		ud.setUsername(userDto.getUsername()); // 至少传递用户名用于缓存清理
		return R.ok(updateUser(ud));
	}


	/**
	 * 更新用户信息
	 * @param userDto 用户数据传输对象，包含需要更新的用户信息
	 * @return 更新成功返回true
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = CacheConstants.USER_DETAILS, key = "#userDto.username")
	public Boolean updateUser(UserDTO userDto) {
		Objects.requireNonNull(userDto, "userDto must not be null");
		// 更新用户表信息
		SysUser sysUser = new SysUser();
		BeanUtils.copyProperties(userDto, sysUser);
		sysUser.setUpdateTime(LocalDateTime.now());
		if (StrUtil.isNotBlank(userDto.getPassword())) {
			sysUser.setPassword(ENCODER.encode(userDto.getPassword()));
		}
		this.updateById(sysUser);

		// 更新用户角色表
		if (Objects.nonNull(userDto.getRole())) {
			// 删除用户角色关系
			sysUserRoleMapper
				.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userDto.getUserId()));
			userDto.getRole().forEach(roleId -> {
				SysUserRole userRole = new SysUserRole();
				userRole.setUserId(sysUser.getUserId());
				userRole.setRoleId(roleId);
				sysUserRoleMapper.insert(userRole);
			});
		}

		return Boolean.TRUE;
	}

	/**
	 * 查询用户列表并转换为Excel导出格式
	 * @param userDTO 用户查询条件
	 * @return 用户Excel视图对象列表
	 */
	@Override
	public List<UserExcelVO> listUsers(UserDTO userDTO) {
		// 根据数据权限查询全部的用户信息
		List<UserVO> voList = baseMapper.listUsers(userDTO);
		// 转换成execl 对象输出
		return voList.stream().map(userVO -> {
			Objects.requireNonNull(userVO, "userVO must not be null");
			UserExcelVO excelVO = new UserExcelVO();
			BeanUtils.copyProperties(userVO, excelVO);
			excelVO.setRoleNameList(
					userVO.getRoleList().stream().map(SysRole::getRoleName).collect(Collectors.joining(StrUtil.COMMA)));
			return excelVO;
		}).toList();
	}

	/**
	 * 注册用户并赋予角色
	 * - 若传 roleCode：后端自动解析为 roleId（推荐）
	 * - 若传 role（List<Long>）：直接使用
	 * - 两者都为空：使用后端默认角色 USER_DEFAULT_ROLE
	 * @param userDto 用户注册信息DTO
	 * @return 注册结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R<Boolean> registerUser(RegisterUserDTO userDto) {
		// 判断用户名是否存在
		SysUser sysUser = this.getOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, userDto.getUsername()));
		if (sysUser != null) {
			String message = MsgUtils.getMessage(ErrorCodes.SYS_USER_USERNAME_EXISTING, userDto.getUsername());
			return R.failed(message);
		}

		UserDTO user = new UserDTO();
		BeanUtils.copyProperties(userDto, user);

		// 优先使用 roleCode 解析 roleId
		if (StrUtil.isNotBlank(userDto.getRoleCode())) {
			SysRole role = sysRoleService
				.getOne(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleCode, userDto.getRoleCode()));
			if (role == null) {
				return R.failed("角色编码不存在: " + userDto.getRoleCode());
			}
			user.setRole(CollUtil.toList(role.getRoleId()));
		}

		return R.ok(saveUser(user));
	}

	/**
	 * 批量注册用户
	 *
	 * <p>功能说明：使用真正的批量插入优化性能，减少数据库交互次数。
	 * 返回每个用户的注册结果（成功/失败），管理员可根据结果处理失败项。
	 *
	 * <p>处理逻辑：
	 * 1. 一次性查询所有已存在的用户名
	 * 2. 一次性查询所有需要的角色
	 * 3. 多线程并行加密密码
	 * 4. 批量插入用户（使用 MyBatis-Plus 的 saveBatch）
	 * 5. 批量插入用户角色关系
	 *
	 * @param request 批量注册请求，包含用户列表
	 * @return 批量注册结果，包含总数、成功数、失败数和每条结果
	 * @see BatchRegisterUserRequest
	 * @see BatchRegisterResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchRegisterResult batchRegister(BatchRegisterUserRequest request) {
		List<BatchRegisterResultItem> resultItems = new ArrayList<>();

		List<BatchRegisterUserDTO> users = request.getUsers();
		if (users == null || users.isEmpty()) {
			return BatchRegisterResult.builder()
				.total(0)
				.successCount(0)
				.failCount(0)
				.results(resultItems)
				.build();
		}

		// 1. 收集所有需要校验的用户名
		List<String> usernames = users.stream()
			.map(BatchRegisterUserDTO::getUsername)
			.filter(StrUtil::isNotBlank)
			.toList();

		// 2. 一次性查询所有已存在的用户名
		List<String> existingUsernames = baseMapper.selectExistingUsernames(usernames);
		Set<String> existingSet = new HashSet<>(existingUsernames);

		// 3. 收集所有角色编码并一次性查询
		Set<String> roleCodes = users.stream()
			.map(BatchRegisterUserDTO::getRoleCode)
			.filter(StrUtil::isNotBlank)
			.collect(Collectors.toSet());

		Map<String, SysRole> roleMap = new HashMap<>();
		if (!roleCodes.isEmpty()) {
			List<SysRole> roles = sysRoleService.list(
				Wrappers.<SysRole>lambdaQuery().in(SysRole::getRoleCode, roleCodes));
			roles.forEach(role -> roleMap.put(role.getRoleCode(), role));
		}

		// 获取默认角色
		String defaultRoleCode = ParamResolver.getStr("USER_DEFAULT_ROLE");
		SysRole defaultRole = null;
		if (StrUtil.isNotBlank(defaultRoleCode)) {
			defaultRole = roleMap.computeIfAbsent(defaultRoleCode,
				code -> sysRoleService.getOne(
					Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleCode, code)));
		}

		// 4. 收集待处理的用户数据
		List<BatchRegisterUserDTO> usersToProcess = new ArrayList<>();
		for (BatchRegisterUserDTO userDto : users) {
			// 校验用户名
			if (StrUtil.isBlank(userDto.getUsername())) {
				resultItems.add(BatchRegisterResultItem.builder()
					.username(userDto.getUsername())
					.success(false)
					.errorMessage("用户名不能为空")
					.build());
				continue;
			}

			if (existingSet.contains(userDto.getUsername())) {
				resultItems.add(BatchRegisterResultItem.builder()
					.username(userDto.getUsername())
					.success(false)
					.errorMessage("用户名已存在")
					.build());
				continue;
			}

			// 解析角色
			List<Long> roleIds;
			if (StrUtil.isNotBlank(userDto.getRoleCode())) {
				SysRole role = roleMap.get(userDto.getRoleCode());
				if (role == null) {
					resultItems.add(BatchRegisterResultItem.builder()
						.username(userDto.getUsername())
						.success(false)
						.errorMessage("角色编码不存在: " + userDto.getRoleCode())
						.build());
					continue;
				}
				roleIds = CollUtil.toList(role.getRoleId());
			} else {
				if (defaultRole == null) {
					resultItems.add(BatchRegisterResultItem.builder()
						.username(userDto.getUsername())
						.success(false)
						.errorMessage("默认角色不存在")
						.build());
					continue;
				}
				roleIds = CollUtil.toList(defaultRole.getRoleId());
			}

			usersToProcess.add(userDto);
			resultItems.add(BatchRegisterResultItem.builder()
				.username(userDto.getUsername())
				.success(true)
				.roleIds(roleIds)
				.build());
		}

		// 5. 多线程并行加密密码
		int threadCount = Runtime.getRuntime().availableProcessors();
		List<String> encryptedPasswords = new ArrayList<>(Collections.nCopies(usersToProcess.size(), ""));
		try {
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			List<Future<?>> futures = new ArrayList<>();

			for (int i = 0; i < usersToProcess.size(); i++) {
				final int index = i;
				final String password = usersToProcess.get(i).getPassword();
				futures.add(executor.submit(() -> {
					encryptedPasswords.set(index, ENCODER.encode(password));
				}));
			}

			for (Future<?> future : futures) {
				future.get();
			}
			executor.shutdown();
		} catch (Exception e) {
			log.error("密码加密失败", e);
		}

		// 6. 构建用户实体并批量插入
		List<SysUser> usersToInsert = new ArrayList<>();
		Map<String, List<Long>> usernameToRoleIds = new LinkedHashMap<>();

		for (int i = 0; i < usersToProcess.size(); i++) {
			BatchRegisterUserDTO userDto = usersToProcess.get(i);
			SysUser sysUser = new SysUser();
			sysUser.setUsername(userDto.getUsername());
			sysUser.setPassword(encryptedPasswords.get(i));
			sysUser.setNickname(userDto.getNickname());
			sysUser.setName(userDto.getName());
			sysUser.setPhone(userDto.getPhone());
			sysUser.setEmail(userDto.getEmail());
			sysUser.setDelFlag(CommonConstants.STATUS_NORMAL);
			sysUser.setCreateBy(userDto.getUsername());

			usersToInsert.add(sysUser);

			// 获取角色ID
			List<Long> roleIds = resultItems.stream()
				.filter(r -> r.isSuccess() && userDto.getUsername().equals(r.getUsername()))
				.findFirst()
				.map(BatchRegisterResultItem::getRoleIds)
				.orElse(new ArrayList<>());
			usernameToRoleIds.put(userDto.getUsername(), roleIds);
		}

		// 7. 批量插入用户
		if (!usersToInsert.isEmpty()) {
			saveBatch(usersToInsert);

			// 8. 批量插入用户角色关系
			List<SysUserRole> userRolesToInsert = new ArrayList<>();
			for (SysUser user : usersToInsert) {
				List<Long> roleIds = usernameToRoleIds.get(user.getUsername());
				if (roleIds != null) {
					for (Long roleId : roleIds) {
						SysUserRole userRole = new SysUserRole();
						userRole.setUserId(user.getUserId());
						userRole.setRoleId(roleId);
						userRolesToInsert.add(userRole);
					}
				}
			}

			if (!userRolesToInsert.isEmpty()) {
				sysUserRoleMapper.insertBatch(userRolesToInsert);
			}
		}

		// 9. 统计结果
		int successCount = (int) resultItems.stream().filter(BatchRegisterResultItem::isSuccess).count();
		int failCount = (int) resultItems.stream().filter(item -> !item.isSuccess()).count();

		return BatchRegisterResult.builder()
			.total(users.size())
			.successCount(successCount)
			.failCount(failCount)
			.results(resultItems)
			.build();
	}


	/**
	 * 锁定用户
	 * @param username 用户名
	 * @return 操作结果，包含是否成功的信息
	 */
	@Override
	@CacheEvict(value = CacheConstants.USER_DETAILS, key = "#username")
	public R<Boolean> lockUser(String username) {
		SysUser sysUser = baseMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));

		if (Objects.nonNull(sysUser)) {
			sysUser.setLockFlag(CommonConstants.STATUS_LOCK);
			baseMapper.updateById(sysUser);
		}
		return R.ok();
	}

	/**
	 * 修改用户密码
	 * @param userDto 用户信息传输对象，包含用户名、原密码和新密码
	 * @return 操作结果，成功返回R.ok()，失败返回错误信息
	 * @CacheEvict 清除用户详情缓存
	 */
	@Override
	@CacheEvict(value = CacheConstants.USER_DETAILS, key = "#userDto.username")
	public R<?> changePassword(UserDTO userDto) {
		SysUser sysUser = baseMapper.selectById(SecurityUtils.getUser().getId());
		if (Objects.isNull(sysUser)) {
			return R.failed("用户不存在");
		}

		if (StrUtil.isEmpty(userDto.getPassword())) {
			return R.failed("原密码不能为空");
		}

		if (!ENCODER.matches(userDto.getPassword(), sysUser.getPassword())) {
			log.info("原密码错误，修改个人信息失败:{}", userDto.getUsername());
			return R.failed(MsgUtils.getMessage(ErrorCodes.SYS_USER_UPDATE_PASSWORDERROR));
		}

		if (StrUtil.isEmpty(userDto.getNewpassword1())) {
			return R.failed("新密码不能为空");
		}
		String password = ENCODER.encode(userDto.getNewpassword1());

		this.update(Wrappers.<SysUser>lambdaUpdate()
			.set(SysUser::getPassword, password)
			.eq(SysUser::getUserId, sysUser.getUserId()));
		return R.ok();
	}

	/**
	 * 校验用户密码是否正确
	 * @param password 待校验的密码
	 * @return 校验结果，成功返回R.ok()，失败返回R.failed()
	 */
	@Override
	public R<?> checkPassword(String password) {
		SysUser sysUser = baseMapper.selectById(SecurityUtils.getUser().getId());

		if (!ENCODER.matches(password, sysUser.getPassword())) {
			log.info("原密码错误");
			return R.failed("密码输入错误");
		}
		else {
			return R.ok();
		}
	}

}
