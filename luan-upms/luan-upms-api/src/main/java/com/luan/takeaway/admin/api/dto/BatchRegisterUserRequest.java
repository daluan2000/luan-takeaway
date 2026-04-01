package com.luan.takeaway.admin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量注册用户请求 DTO
 *
 * <p>功能说明：用于管理员批量注册系统用户，适用于初始化数据或批量导入用户场景。
 * 系统会为每个用户分配指定的角色（ROLE_MERCHANT/ROLE_CUSTOMER/ROLE_DELIVERY）。
 *
 * @author system
 * @see BatchRegisterUserDTO 单个用户信息
 * @see BatchRegisterResult 批量注册结果
 */
@Data
@Schema(description = "批量注册用户请求对象")
public class BatchRegisterUserRequest {

	/**
	 * 用户列表
	 *
	 * @see BatchRegisterUserDTO
	 */
	@NotEmpty(message = "用户列表不能为空")
	@Schema(description = "用户列表", requiredMode = Schema.RequiredMode.REQUIRED)
	private List<BatchRegisterUserDTO> users;

}
