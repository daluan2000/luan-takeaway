package com.pig4cloud.pig.takeaway.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 扣减库存请求对象
 *
 * @author pig
 */
@Data
@Schema(description = "扣减库存请求对象")
public class DeductStockRequest {

	@Schema(description = "商家用户ID")
	private Long merchantUserId;

	@Schema(description = "扣减菜品项")
	private List<DishPurchaseItemDTO> items;

}
