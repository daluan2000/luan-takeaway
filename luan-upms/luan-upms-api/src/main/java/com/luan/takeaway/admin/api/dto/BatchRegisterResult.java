package com.luan.takeaway.admin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量注册结果
 *
 * <p>功能说明：汇总批量注册操作的总体结果，包含成功/失败统计和逐条明细。
 *
 * @author system
 * @see BatchRegisterResultItem 单条注册结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量注册结果")
public class BatchRegisterResult {

	/**
	 * 总数
	 *
	 * <p>本次批量注册请求的用户总数
	 */
	@Schema(description = "总数")
	private int total;

	/**
	 * 成功数
	 *
	 * <p>成功创建的用户数量
	 */
	@Schema(description = "成功数")
	private int successCount;

	/**
	 * 失败数
	 *
	 * <p>创建失败的用户数量
	 */
	@Schema(description = "失败数")
	private int failCount;

	/**
	 * 结果列表
	 *
	 * <p>每条用户的注册结果，包含成功/失败状态和错误信息
	 *
	 * @see BatchRegisterResultItem
	 */
	@Schema(description = "结果列表")
	private List<BatchRegisterResultItem> results;

}
