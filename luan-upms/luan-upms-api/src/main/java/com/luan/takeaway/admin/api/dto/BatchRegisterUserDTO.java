package com.luan.takeaway.admin.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 批量注册用户 DTO
 *
 * <p>功能说明：定义单个用户的注册信息，用于批量注册接口的数据传输。
 *
 * @author system
 */
@Data
@Schema(description = "批量注册用户传输对象")
public class BatchRegisterUserDTO {

	/**
	 * 用户名（登录账号）
	 *
	 * <p>必填，唯一标识，用于系统登录。
	 */
	@NotBlank(message = "用户名不能为空")
	@Schema(description = "用户名（登录账号）")
	private String username;

	/**
	 * 密码
	 *
	 * <p>必填，用于账户登录认证。
	 */
	@NotBlank(message = "密码不能为空")
	@Schema(description = "密码")
	private String password;

	/**
	 * 昵称（显示名称）
	 *
	 * <p>可选，用于前端展示。
	 */
	@Schema(description = "昵称")
	private String nickname;

	/**
	 * 真实姓名
	 *
	 * <p>可选，用于实名认证或配送场景。
	 */
	@Schema(description = "姓名")
	private String name;

	/**
	 * 手机号
	 *
	 * <p>可选，用于联系方式绑定。
	 */
	@Schema(description = "手机号")
	private String phone;

	/**
	 * 邮箱
	 *
	 * <p>可选，用于邮箱绑定。
	 */
	@Schema(description = "邮箱")
	private String email;

	/**
	 * 角色编码
	 *
	 * <p>必填，指定用户角色类型：
	 * - ROLE_MERCHANT：商家
	 * - ROLE_CUSTOMER：客户
	 * - ROLE_DELIVERY：骑手
	 */
	@NotBlank(message = "角色编码不能为空")
	@Schema(description = "角色编码：ROLE_MERCHANT / ROLE_CUSTOMER / ROLE_DELIVERY")
	private String roleCode;

}
