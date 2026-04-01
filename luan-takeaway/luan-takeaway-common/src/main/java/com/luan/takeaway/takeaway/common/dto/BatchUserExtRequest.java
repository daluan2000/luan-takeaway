package com.luan.takeaway.takeaway.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 客户商家骑手扩展信息批量导入请求 DTO
 *
 * <p>功能说明：封装批量导入的用户扩展信息列表，用于管理员批量导入客户、商家、骑手的扩展资料。
 *
 * @author system
 * @see BatchUserExtDTO 单个导入项
 * @see BatchUserExtResult 批量导入结果
 */
@Data
@Schema(description = "客户商家骑手扩展信息批量导入请求")
public class BatchUserExtRequest {

	/**
	 * 导入项列表
	 *
	 * <p>必填项，不能为空列表
	 */
	@NotEmpty(message = "导入项列表不能为空")
	@Schema(description = "导入项列表", requiredMode = Schema.RequiredMode.REQUIRED)
	private List<BatchUserExtDTO> items;

}
