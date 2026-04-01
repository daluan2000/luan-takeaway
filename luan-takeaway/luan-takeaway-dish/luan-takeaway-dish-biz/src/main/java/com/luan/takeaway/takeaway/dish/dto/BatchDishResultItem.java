package com.luan.takeaway.takeaway.dish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜品批量导入结果项
 *
 * <p>功能说明：记录单个菜品导入的结果，包含成功/失败状态和相关信息。
 *
 * @author system
 * @see BatchDishResult 批量导入结果汇总
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜品批量导入结果项")
public class BatchDishResultItem {

	/**
	 * 菜品名称
	 *
	 * <p>对应 BatchDishDTO.dishName
	 */
	@Schema(description = "菜品名称")
	private String dishName;

	/**
	 * 商家用户ID
	 *
	 * <p>菜品所属商家的用户ID
	 */
	@Schema(description = "商家用户ID")
	private Long merchantUserId;

	/**
	 * 是否成功
	 *
	 * <p>true 表示导入成功，false 表示导入失败
	 */
	@Schema(description = "是否成功")
	private boolean success;

	/**
	 * 错误信息
	 *
	 * <p>失败时返回具体的错误原因
	 */
	@Schema(description = "错误信息（失败时返回）")
	private String errorMessage;

	/**
	 * 菜品ID
	 *
	 * <p>导入成功时返回，用于后续关联操作
	 */
	@Schema(description = "菜品ID（成功时返回）")
	private Long dishId;

}
