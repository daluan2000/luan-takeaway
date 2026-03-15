/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */
package com.luan.takeaway.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luan.takeaway.admin.api.entity.SysDict;
import com.luan.takeaway.common.core.util.R;

// todo 字典管理的作用是什么，业务数据库是依赖字典值的，如果删除或修改字典值，那么所有业务数据库将会出现差错
// 如果增加字典值，那么前后端一定需要增加相应的字典逻辑，那直接改代码得了
// 说白了，字典值的增加删除修改，就不该是运行阶段进行的，而是开发阶段的任务
// 目前字典管理的唯一作用好像是将字典值传给前端，防止前端字典值的硬编码，但直接将java常量类里的字典值传给前端即可，没必要用数据库

/**
 * 字典表服务接口 提供字典数据的增删改查及缓存同步功能
 *
 * @author lengleng
 * @date 2025/05/30
 */
public interface SysDictService extends IService<SysDict> {

	/**
	 * 根据ID列表删除字典
	 * @param ids 要删除的字典ID数组
	 * @return 操作结果
	 */
	R<?> removeDictByIds(Long[] ids);

	/**
	 * 更新字典
	 * @param sysDict 要更新的字典对象
	 * @return 操作结果
	 */
	R<SysDict> updateDict(SysDict sysDict);

	/**
	 * 同步字典缓存（清空缓存）
	 * @return 操作结果
	 */
	R<?> syncDictCache();

}
