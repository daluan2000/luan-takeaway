package com.pig4cloud.pig.takeaway.common.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateResultVO {

	private Long orderId;

	private String orderNo;

	private BigDecimal totalAmount;

	private BigDecimal payAmount;

}
