package com.luan.takeaway.takeaway.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 支付请求对象
 *
 * @author pig
 */
@Data
@Schema(description = "支付请求对象")
public class PayRequest {

	@Schema(description = "订单ID")
	private Long orderId;

	@Schema(description = "支付渠道")
	private String payChannel;

}
