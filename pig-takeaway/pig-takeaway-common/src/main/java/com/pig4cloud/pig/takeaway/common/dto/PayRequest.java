package com.pig4cloud.pig.takeaway.common.dto;

import lombok.Data;

@Data
public class PayRequest {

	private Long orderId;

	private String payChannel;

}
