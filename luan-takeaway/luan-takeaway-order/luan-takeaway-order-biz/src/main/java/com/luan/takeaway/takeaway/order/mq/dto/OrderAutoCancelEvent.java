package com.luan.takeaway.takeaway.order.mq.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单自动取消事件。
 */
@Data
public class OrderAutoCancelEvent {

	/**
	 * 订单主键 ID。
	 *
	 * 自动取消执行时以该字段作为唯一定位条件。
	 */
	private Long orderId;

	/**
	 * 订单号。
	 *
	 * 主要用于日志排查，便于业务侧快速定位订单。
	 */
	private String orderNo;

	/**
	 * 订单创建时间。
	 *
	 * 当前自动取消逻辑主要依赖消息 TTL；该字段保留用于审计和后续扩展。
	 */
	private LocalDateTime createTime;

}
