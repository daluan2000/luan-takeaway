package com.luan.takeaway.takeaway.user.controller;

import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.log.annotation.SysLog;
import com.luan.takeaway.common.security.annotation.HasPermission;
import com.luan.takeaway.takeaway.common.api.TakeawayApiConstants;
import com.luan.takeaway.takeaway.common.dto.BatchUserExtRequest;
import com.luan.takeaway.takeaway.common.dto.BatchUserExtResult;
import com.luan.takeaway.takeaway.user.service.WmUserExtBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户扩展信息批量导入控制器
 *
 * <p>功能说明：提供用户扩展信息（客户、商家、骑手）的批量导入接口。
 *
 * <p>接口说明：
 * - 接口地址：POST /takeaway/user/ext/batch/import
 * - 权限要求：sys_user_ext_batch_import
 *
 * @author system
 */
@RestController
@AllArgsConstructor
@Tag(name = "用户扩展信息管理")
public class WmUserExtController {

	private final WmUserExtBatchService wmUserExtBatchService;

	/**
	 * 批量导入客户、商家、骑手扩展信息
	 *
	 * <p>功能说明：管理员批量导入用户扩展信息，支持客户、商家、骑手三种类型。
	 * 返回每个导入项的结果（成功/失败），管理员可根据返回的失败原因处理异常数据。
	 *
	 * <p>权限控制：需拥有 sys_user_ext_batch_import 权限
	 *
	 * @param request 批量导入请求，包含扩展信息列表
	 * @return 批量导入结果，包含总数、成功数、失败数及每条明细
	 * @see BatchUserExtRequest
	 * @see BatchUserExtResult
	 */
	@PostMapping(TakeawayApiConstants.USER_PREFIX + "/ext/batch/import")
	@SysLog("批量导入用户扩展信息")
	@HasPermission("sys_user_ext_batch_import")
	@Operation(summary = "批量导入用户扩展信息", description = "批量导入客户、商家、骑手的扩展信息")
	public R<BatchUserExtResult> batchImport(@RequestBody BatchUserExtRequest request) {
		return R.ok(wmUserExtBatchService.batchImport(request));
	}

}
