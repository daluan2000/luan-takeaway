package com.pig4cloud.pig.takeaway.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 下单菜品项传输对象
 *
 * @author pig
 */
@Data
@Schema(description = "下单菜品项传输对象")
public class DishPurchaseItemDTO {

	@Schema(description = "菜品ID")
	private Long dishId;

	@Schema(description = "购买数量")
	private Integer quantity;

}
