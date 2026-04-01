package com.luan.takeaway.takeaway.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户商家骑手扩展信息批量导入结果项
 *
 * <p>功能说明：记录单个用户扩展信息导入的结果，包含成功/失败状态和相关信息。
 *
 * @author system
 * @see BatchUserExtResult 批量导入结果汇总
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量导入结果项")
public class BatchUserExtResultItem {

	/**
	 * 用户ID
	 *
	 * <p>对应 BatchUserExtDTO.userId
	 */
	@Schema(description = "用户ID")
	private Long userId;

	/**
	 * 用户类型
	 *
	 * <p>对应 BatchUserExtDTO.userType
	 */
	@Schema(description = "用户类型")
	private String userType;

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
	 * 扩展信息ID
	 *
	 * <p>导入成功时返回，用于后续关联操作
	 */
	@Schema(description = "扩展信息ID（成功时返回）")
	private Long extId;

}
