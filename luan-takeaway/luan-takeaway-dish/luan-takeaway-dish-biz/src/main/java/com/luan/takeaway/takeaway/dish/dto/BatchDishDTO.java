package com.luan.takeaway.takeaway.dish.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 菜品批量导入 DTO
 *
 * <p>功能说明：定义单个菜品的导入格式，用于管理员或商家批量导入菜品信息。
 *
 * <p>使用场景：
 * - 管理员批量导入所有商家的菜品（需指定 merchantUserId）
 * - 商家批量导入自己的菜品（merchantUserId 由系统自动填充）
 *
 * @author system
 * @see BatchDishRequest 批量导入请求
 * @see BatchDishResult 批量导入结果
 */
@Data
@Schema(description = "菜品批量导入项")
public class BatchDishDTO {

	/**
	 * 商家用户ID
	 *
	 * <p>说明：
	 * - 管理员模式：可选，若指定则导入到对应商家，若不指定则使用请求级别的 merchantUserId
	 * - 商家模式：忽略，由系统自动使用当前登录商家的 userId
	 */
	@Schema(description = "商家用户ID（可选，管理员模式可指定，商家模式忽略）")
	private Long merchantUserId;

	/**
	 * 菜品图片URL
	 *
	 * <p>菜品的展示图片地址
	 */
	@Schema(description = "菜品图片URL")
	private String dishImage;

	/**
	 * 菜品名称
	 *
	 * <p>必填项，菜品的显示名称
	 */
	@NotBlank(message = "菜品名称不能为空")
	@Schema(description = "菜品名称", requiredMode = Schema.RequiredMode.REQUIRED)
	private String dishName;

	/**
	 * 菜品描述
	 *
	 * <p>菜品的详细介绍或卖点
	 */
	@Schema(description = "菜品描述")
	private String dishDesc;

	/**
	 * 价格
	 *
	 * <p>必填项，菜品销售价格
	 */
	@NotNull(message = "价格不能为空")
	@Schema(description = "价格（元）", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal price;

	/**
	 * 库存
	 *
	 * <p>必填项，菜品当前库存数量
	 */
	@NotNull(message = "库存不能为空")
	@Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer stock;

	/**
	 * 销售状态
	 *
	 * <p>默认上架（SALE_ON）
	 * - SALE_ON：上架（可供销售）
	 * - SALE_OFF：下架（暂停销售）
	 */
	@Schema(description = "销售状态：SALE_ON=上架, SALE_OFF=下架")
	private String saleStatus;

	/**
	 * 是否自动生成知识文档
	 *
	 * <p>若为 true，系统将自动为该菜品生成 RAG 知识文档
	 */
	@Schema(description = "是否自动生成知识文档")
	private Boolean autoGenerateKnowledge;

}
