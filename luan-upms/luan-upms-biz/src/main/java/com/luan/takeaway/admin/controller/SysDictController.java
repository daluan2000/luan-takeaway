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

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.admin.api.entity.SysDict;
import com.luan.takeaway.admin.api.entity.SysDictItem;
import com.luan.takeaway.admin.service.SysDictItemService;
import com.luan.takeaway.admin.service.SysDictService;
import com.luan.takeaway.common.core.constant.CacheConstants;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.log.annotation.SysLog;
import com.luan.takeaway.common.security.annotation.Inner;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
在这个项目里，字典就是“前后端共享约定”的一层配置：

1 后端维护字典源
字典存于 sys_dict / sys_dict_item，通过 /admin/dict/type/{type} 提供给前端和其他服务。

2 前端按字典值渲染和提交
比如 luan-ui/src/views/admin/client/form.vue:73 的 grant_types，来自 useDict('grant_types')，用户选中后提交的是字典 value（如 password、authorization_code）。

3 后端按同一套值做业务判断
后端拿到这些值后按约定进行逻辑分支，不依赖前端文案（label），只依赖稳定的 item_value。

4 文案与逻辑解耦
label 可改（显示文案），value 尽量稳定（业务语义）；这样改展示不影响逻辑。

一句话：字典是“数据驱动的业务枚举协议”，把前后端硬编码常量统一成数据库可配置的契约。
 */


/**
 * 在外卖业务模块，暂时维持硬编码，暂时不用这个字典逻辑
 * 前端暂时删除这个路由菜单，后端保留字典接口
 */


/**
 * 字典表前端控制器
 *
 * @author lengleng
 * @date 2025/05/30
 * @since 2019-03-19
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dict")
@Tag(description = "dict", name = "字典管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class SysDictController {

	private final SysDictService sysDictService;

	private final SysDictItemService sysDictItemService;

	/**
	 * 通过ID查询字典信息
	 * @param id 字典ID
	 * @return 包含字典信息的响应对象
	 */
	@GetMapping("/details/{id}")
	@Operation(summary = "通过ID查询字典信息", description = "通过ID查询字典信息")
	public R<SysDict> getById(@PathVariable Long id) {
		return R.ok(sysDictService.getById(id));
	}

	/**
	 * 查询字典详细信息
	 * @param query 字典查询条件对象
	 * @return 包含字典信息的响应结果
	 */
	@GetMapping("/details")
	@Operation(summary = "查询字典详细信息", description = "查询字典详细信息")
	public R<SysDict> getDetails(@ParameterObject SysDict query) {
		return R.ok(sysDictService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 分页查询字典信息
	 * @param page 分页对象
	 * @param sysDict 字典查询条件
	 * @return 包含分页结果的响应对象
	 */
	@GetMapping("/page")
	@Operation(summary = "分页查询字典信息", description = "分页查询字典信息")
	public R<IPage<SysDict>> getDictPage(@ParameterObject Page<SysDict> page, @ParameterObject SysDict sysDict) {
		return R.ok(sysDictService.page(page,
				Wrappers.<SysDict>lambdaQuery()
					.eq(StrUtil.isNotBlank(sysDict.getSystemFlag()), SysDict::getSystemFlag, sysDict.getSystemFlag())
					.like(StrUtil.isNotBlank(sysDict.getDictType()), SysDict::getDictType, sysDict.getDictType())));
	}

	/**
	 * 保存字典信息
	 * @param sysDict 字典信息对象
	 * @return 操作结果，包含保存的字典信息
	 */
	@SysLog("添加字典")
	@PostMapping
	@Operation(summary = "保存字典信息", description = "保存字典信息")
	@PreAuthorize("@pms.hasPermission('sys_dict_add')")
	public R<SysDict> saveDict(@Valid @RequestBody SysDict sysDict) {
		sysDictService.save(sysDict);
		return R.ok(sysDict);
	}

	/**
	 * 删除字典并清除字典缓存
	 * @param ids 字典ID数组
	 * @return 操作结果
	 */
	@SysLog("删除字典")
	@DeleteMapping
	@PreAuthorize("@pms.hasPermission('sys_dict_del')")
	@Operation(summary = "删除字典并清除字典缓存", description = "删除字典并清除字典缓存")
	@CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
	public R<?> removeById(@RequestBody Long[] ids) {
		return R.ok(sysDictService.removeDictByIds(ids));
	}

	/**
	 * 修改字典信息
	 * @param sysDict 字典信息
	 * @return 操作结果 success/false
	 */
	@PutMapping
	@SysLog("修改字典")
	@PreAuthorize("@pms.hasPermission('sys_dict_edit')")
	@Operation(summary = "修改字典信息", description = "修改字典信息")
	public R<SysDict> updateDict(@Valid @RequestBody SysDict sysDict) {
		return sysDictService.updateDict(sysDict);
	}

	/**
	 * 分页查询字典列表
	 * @param name 字典类型名称或描述
	 * @return 包含字典列表的响应结果
	 */
	@GetMapping("/list")
	@Operation(summary = "分页查询字典列表", description = "分页查询字典列表")
	public R<List<SysDict>> listDicts(String name) {
		return R.ok(sysDictService.list(Wrappers.<SysDict>lambdaQuery()
			.like(StrUtil.isNotBlank(name), SysDict::getDictType, name)
			.or()
			.like(StrUtil.isNotBlank(name), SysDict::getDescription, name)));
	}

	/**
	 * 分页查询字典项
	 * @param page 分页对象
	 * @param sysDictItem 字典项查询条件
	 * @return 分页查询结果
	 */
	@GetMapping("/item/page")
	@Operation(summary = "分页查询字典项", description = "分页查询字典项")
	public R<IPage<SysDictItem>> getDictItemPage(@ParameterObject Page<SysDictItem> page,
			@ParameterObject SysDictItem sysDictItem) {
		return R.ok(sysDictItemService.page(page, Wrappers.query(sysDictItem)));
	}

	/**
	 * 通过id查询字典项详情
	 * @param id 字典项id
	 * @return 包含字典项详情的响应结果
	 */
	@GetMapping("/item/details/{id}")
	@Operation(summary = "通过id查询字典项详情", description = "通过id查询字典项详情")
	public R<SysDictItem> getDictItemById(@PathVariable("id") Long id) {
		return R.ok(sysDictItemService.getById(id));
	}

	/**
	 * 获取字典项详情
	 * @param query 字典项查询条件
	 * @return 包含字典项详情的响应结果
	 */
	@GetMapping("/item/details")
	@Operation(summary = "获取字典项详情", description = "获取字典项详情")
	public R<SysDictItem> getDictItemDetails(SysDictItem query) {
		return R.ok(sysDictItemService.getOne(Wrappers.query(query), false));
	}

	/**
	 * 新增字典项
	 * @param sysDictItem 字典项对象
	 * @return 操作结果
	 */
	@SysLog("新增字典项")
	@PostMapping("/item")
	@Operation(summary = "新增字典项", description = "新增字典项")
	@CacheEvict(value = CacheConstants.DICT_DETAILS, allEntries = true)
	public R<Boolean> saveDictItem(@RequestBody SysDictItem sysDictItem) {
		return R.ok(sysDictItemService.save(sysDictItem));
	}

	/**
	 * 修改字典项
	 * @param sysDictItem 要修改的字典项对象
	 * @return 操作结果
	 */
	@SysLog("修改字典项")
	@PutMapping("/item")
	@Operation(summary = "修改字典项", description = "修改字典项")
	public R<Boolean> updateDictItem(@RequestBody SysDictItem sysDictItem) {
		return sysDictItemService.updateDictItem(sysDictItem);
	}

	/**
	 * 通过id删除字典项
	 * @param id 字典项id
	 * @return 操作结果
	 */
	@SysLog("通过id删除字典项")
	@DeleteMapping("/item/{id}")
	@Operation(summary = "通过id删除字典项", description = "通过id删除字典项")
	public R<Boolean> removeDictItemById(@PathVariable Long id) {
		return sysDictItemService.removeDictItem(id);
	}

	/**
	 * 同步字典缓存
	 * @return 操作结果
	 */
	@SysLog("同步字典缓存")
	@PutMapping("/sync")
	@Operation(summary = "同步字典缓存", description = "同步字典缓存")
	public R<?> syncDict() {
		return sysDictService.syncDictCache();
	}

	/**
	 * 导出字典项数据
	 * @param sysDictItem 字典项查询条件
	 * @return 符合条件的字典项列表
	 */
	@ResponseExcel
	@GetMapping("/export")
	@Operation(summary = "导出字典项数据", description = "导出字典项数据")
	public List<SysDictItem> exportDictItems(SysDictItem sysDictItem) {
		return sysDictItemService.list(Wrappers.query(sysDictItem));
	}

	/**
	 * 通过字典类型查找字典
	 * @param type 类型
	 * @return 同类型字典
	 */
	@GetMapping("/type/{type}")
	@Operation(summary = "通过字典类型查找字典", description = "通过字典类型查找字典")
	@Cacheable(value = CacheConstants.DICT_DETAILS, key = "#type", unless = "#result.data.isEmpty()")
	public R<List<SysDictItem>> getDictByType(@PathVariable String type) {
		return R.ok(sysDictItemService.list(Wrappers.<SysDictItem>query().lambda().eq(SysDictItem::getDictType, type)));
	}

	/**
	 * 通过字典类型查找字典 (针对feign调用) TODO: 兼容性方案，代码重复
	 * @param type 类型
	 * @return 同类型字典
	 */
	@Inner
	@GetMapping("/remote/type/{type}")
	@Operation(summary = "通过字典类型查找字典(针对feign调用)", description = "通过字典类型查找字典(针对feign调用)", hidden = true)
	@Cacheable(value = CacheConstants.DICT_DETAILS, key = "#type", unless = "#result.data.isEmpty()")
	public R<List<SysDictItem>> getRemoteDictByType(@PathVariable String type) {
		return R.ok(sysDictItemService.list(Wrappers.<SysDictItem>query().lambda().eq(SysDictItem::getDictType, type)));
	}

}
