package com.luan.takeaway.takeaway.user.service;

import com.luan.takeaway.takeaway.common.dto.BatchUserExtRequest;
import com.luan.takeaway.takeaway.common.dto.BatchUserExtResult;

/**
 * 用户扩展信息批量导入服务接口
 *
 * <p>功能说明：提供客户、商家、骑手三类用户扩展信息的批量导入能力。
 *
 * <p>支持的扩展类型：
 * - customer：客户扩展信息
 * - merchant：商家扩展信息（含店铺资料）
 * - delivery：骑手扩展信息（含配送状态）
 *
 * @author system
 * @see WmUserExtBatchServiceImpl 服务实现
 * @see BatchUserExtRequest 批量导入请求
 * @see BatchUserExtResult 批量导入结果
 */
public interface WmUserExtBatchService {

	/**
	 * 批量导入客户、商家、骑手扩展信息
	 *
	 * <p>功能说明：遍历请求中的扩展信息列表，根据 userType 分发到对应处理方法。
	 * 返回每个导入项的结果（成功/失败），管理员可根据结果处理失败项。
	 *
	 * @param request 批量导入请求，包含用户扩展信息列表
	 * @return 批量导入结果，包含总数、成功数、失败数及每条明细
	 * @see BatchUserExtRequest
	 * @see BatchUserExtResult
	 */
	BatchUserExtResult batchImport(BatchUserExtRequest request);

}
