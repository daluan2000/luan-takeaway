package com.pig4cloud.pig.takeaway.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求对象
 *
 * @author pig
 */
@Data
@Schema(description = "创建订单请求对象")
public class CreateOrderRequest {

	@Schema(description = "客户用户ID")
	private Long customerUserId;

	@Schema(description = "商家用户ID")
	private Long merchantUserId;

	@Schema(description = "收货地址ID")
	private Long deliveryAddressId;

	@Schema(description = "备注")
	private String remark;

	@Schema(description = "下单菜品项")
	private List<DishPurchaseItemDTO> items;

}
