package com.pig4cloud.pig.takeaway.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

	private Long customerUserId;

	private Long merchantUserId;

	private Long deliveryAddressId;

	private String remark;

	private List<DishPurchaseItemDTO> items;

}
