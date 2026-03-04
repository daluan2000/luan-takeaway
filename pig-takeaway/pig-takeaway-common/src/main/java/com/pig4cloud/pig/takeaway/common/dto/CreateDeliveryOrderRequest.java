package com.pig4cloud.pig.takeaway.common.dto;

import lombok.Data;

@Data
public class CreateDeliveryOrderRequest {

	private Long orderId;

	private String orderNo;

	private Long merchantUserId;

}
