package com.pig4cloud.pig.takeaway.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单创建结果对象
 *
 * @author pig
 */
@Data
@Schema(description = "订单创建结果对象")
public class OrderCreateResultVO {

	@Schema(description = "订单ID")
	private Long orderId;

	@Schema(description = "订单号")
	private String orderNo;

	@Schema(description = "订单总金额")
	private BigDecimal totalAmount;

	@Schema(description = "实付金额")
	private BigDecimal payAmount;

}
