package com.luan.takeaway.takeaway.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 客户商家骑手扩展信息批量导入 DTO
 *
 * <p>功能说明：定义单个用户扩展信息的导入格式，支持客户、商家、骑手三种类型的扩展数据。
 *
 * <p>使用场景：
 * - 管理员批量导入用户扩展信息（如商家店铺资料、骑手配送资质等）
 * - 适用于系统初始化或数据迁移场景
 *
 * @author system
 * @see BatchUserExtRequest 批量导入请求
 * @see BatchUserExtResult 批量导入结果
 */
@Data
@Schema(description = "客户商家骑手扩展信息批量导入项")
public class BatchUserExtDTO {

	/**
	 * 用户ID（必填）
	 */
	@NotNull(message = "用户ID不能为空")
	@Schema(description = "用户ID")
	private Long userId;

	/**
	 * 用户类型：customer/merchant/delivery（必填）
	 */
	@NotBlank(message = "用户类型不能为空")
	@Schema(description = "用户类型：customer=客户, merchant=商家, delivery=骑手")
	private String userType;

	/**
	 * 真实姓名
	 */
	@Schema(description = "真实姓名")
	private String realName;

	// ========== 商家专属字段 ==========

	/**
	 * 店铺名称
	 */
	@Schema(description = "店铺名称")
	private String storeName;

	/**
	 * 店铺地址ID
	 */
	@Schema(description = "店铺地址ID")
	private Long storeAddressId;

	/**
	 * 审核状态
	 */
	@Schema(description = "审核状态：AUDIT_PENDING=待审核, AUDIT_APPROVED=已通过, AUDIT_REJECTED=已拒绝")
	private String auditStatus;

	/**
	 * 营业状态
	 */
	@Schema(description = "营业状态：BUSINESS_OPEN=营业中, BUSINESS_CLOSED=已打烊")
	private String businessStatus;

	/**
	 * 配送范围（公里）
	 */
	@Schema(description = "配送范围（公里）")
	private BigDecimal deliveryScopeKm;

	// ========== 骑手专属字段 ==========

	/**
	 * 在线状态
	 */
	@Schema(description = "在线状态：ONLINE_ON=在线, ONLINE_OFF=离线")
	private String onlineStatus;

	/**
	 * 雇佣状态
	 */
	@Schema(description = "雇佣状态：EMPLOYMENT_ON=在职, EMPLOYMENT_OFF=离职")
	private String employmentStatus;

}
