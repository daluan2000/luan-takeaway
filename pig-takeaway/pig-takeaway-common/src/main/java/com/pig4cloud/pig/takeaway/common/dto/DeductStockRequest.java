package com.pig4cloud.pig.takeaway.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeductStockRequest {

	private Long merchantUserId;

	private List<DishPurchaseItemDTO> items;

}
