package com.luan.takeaway.admin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量注册结果项
 *
 * <p>功能说明：记录单个用户批量注册的结果，包含成功/失败状态和相关信息。
 *
 * @author system
 * @see BatchRegisterResult 批量注册结果汇总
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量注册结果项")
public class BatchRegisterResultItem {

	/**
	 * 用户名
	 *
	 * <p>对应 BatchRegisterUserDTO.username
	 */
	@Schema(description = "用户名")
	private String username;

	/**
	 * 是否成功
	 *
	 * <p>true 表示创建成功，false 表示创建失败
	 */
	@Schema(description = "是否成功")
	private boolean success;

	/**
	 * 错误信息
	 *
	 * <p>失败时返回具体的错误原因，如"用户名已存在"、"角色不存在"等
	 */
	@Schema(description = "错误信息（失败时返回）")
	private String errorMessage;

	/**
	 * 新创建的用户ID
	 *
	 * <p>创建成功时返回，用于后续关联操作
	 */
	@Schema(description = "新创建的用户ID（成功时返回）")
	private Long userId;

	/**
	 * 角色ID列表（仅用于内部处理，不返回）
	 */
	@Schema(description = "角色ID列表")
	private List<Long> roleIds;

}
