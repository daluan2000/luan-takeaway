package com.pig4cloud.pig.takeaway.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建配送单请求对象
 *
 * @author pig
 */
@Data
@Schema(description = "创建配送单请求对象")
public class CreateDeliveryOrderRequest {

	@Schema(description = "订单ID")
	private Long orderId;

	@Schema(description = "订单号")
	private String orderNo;

	@Schema(description = "商家用户ID")
	private Long merchantUserId;

}
