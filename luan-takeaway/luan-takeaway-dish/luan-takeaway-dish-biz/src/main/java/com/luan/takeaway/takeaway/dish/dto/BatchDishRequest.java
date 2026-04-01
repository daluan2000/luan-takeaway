package com.luan.takeaway.takeaway.dish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 菜品批量导入请求 DTO
 *
 * <p>功能说明：封装批量导入的菜品列表，支持管理员模式和商家模式两种导入方式。
 *
 * <p>两种导入模式：
 * - <b>管理员模式</b>：指定 merchantUserId，批量导入指定商家的菜品
 * - <b>商家模式</b>：merchantUserId 为空，自动使用当前登录商家的 userId
 *
 * @author system
 * @see BatchDishDTO 单个菜品信息
 * @see BatchDishResult 批量导入结果
 */
@Data
@Schema(description = "菜品批量导入请求")
public class BatchDishRequest {

	/**
	 * 商家用户ID
	 *
	 * <p>说明：
	 * - 管理员模式：必填，指定要导入菜品的商家
	 * - 商家模式：可不填，系统自动获取当前登录商家
	 */
	@Schema(description = "商家用户ID（管理员模式必填，商家模式可不填）")
	private Long merchantUserId;

	/**
	 * 菜品列表
	 *
	 * <p>必填项，不能为空列表
	 */
	@NotEmpty(message = "菜品列表不能为空")
	@Schema(description = "菜品列表", requiredMode = Schema.RequiredMode.REQUIRED)
	private List<BatchDishDTO> dishes;

}
