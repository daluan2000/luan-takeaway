package com.luan.takeaway.takeaway.dish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜品批量导入结果
 *
 * <p>功能说明：汇总批量导入操作的总体结果，包含成功/失败统计和逐条明细。
 *
 * @author system
 * @see BatchDishResultItem 单条导入结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜品批量导入结果")
public class BatchDishResult {

	/**
	 * 总数
	 *
	 * <p>本次批量导入请求的菜品总数量
	 */
	@Schema(description = "总数")
	private int total;

	/**
	 * 成功数
	 *
	 * <p>成功导入的菜品数量
	 */
	@Schema(description = "成功数")
	private int successCount;

	/**
	 * 失败数
	 *
	 * <p>导入失败的菜品数量
	 */
	@Schema(description = "失败数")
	private int failCount;

	/**
	 * 结果列表
	 *
	 * <p>每条菜品的导入结果，包含成功/失败状态和错误信息
	 */
	@Schema(description = "结果列表")
	private List<BatchDishResultItem> results;

}
