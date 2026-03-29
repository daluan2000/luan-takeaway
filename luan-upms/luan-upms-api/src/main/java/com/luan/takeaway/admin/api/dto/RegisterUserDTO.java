package com.luan.takeaway.admin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 注册用户 DTO
 *
 * @author lengleng
 * @date 2024/12/23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "注册用户传输对象")
public class RegisterUserDTO extends UserDTO {

	/**
	 * 角色编码，支持传入以下值（与 sys_role.role_code 一一对应）：
	 * ROLE_MERCHANT  / 商家
	 * ROLE_CUSTOMER  / 客户
	 * ROLE_DELIVERY   / 骑手
	 * 若传入 roleCode 则忽略 role 字段，后端自动解析为 roleId。
	 */
	@Schema(description = "角色编码：ROLE_MERCHANT / ROLE_CUSTOMER / ROLE_DELIVERY")
	private String roleCode;

}
