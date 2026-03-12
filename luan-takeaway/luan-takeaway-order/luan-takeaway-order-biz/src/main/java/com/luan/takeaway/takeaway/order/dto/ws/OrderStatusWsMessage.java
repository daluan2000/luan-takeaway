package com.luan.takeaway.takeaway.order.dto.ws;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 订单状态 WebSocket 消息。
 */
@Data
@Accessors(chain = true)
public class OrderStatusWsMessage {

	public static final String CATEGORY = "BUSINESS";

	public static final String BUSINESS_TYPE = "ORDER";

	private String category;

	private String businessType;

	private String eventType;

	private String status;

	private String title;

	private String content;

	private Long timestamp;

	private Long orderId;

	private Long userId;

	private Long riderUserId;

	public static OrderStatusWsMessage merchantAccepted(Long orderId, Long userId) {
		OrderStatusWsMessage message = base(orderId, userId, "MERCHANT_ACCEPTED");
		message.setTitle("订单状态通知");
		message.setContent("商家已接单，正在为您备餐");
		return message;
	}

	public static OrderStatusWsMessage riderAccepted(Long orderId, Long userId, Long riderUserId) {
		OrderStatusWsMessage message = base(orderId, userId, "RIDER_ACCEPTED");
		message.setTitle("订单状态通知");
		message.setContent("骑手已接单，正在为您配送");
		message.setRiderUserId(riderUserId);
		return message;
	}

	public static OrderStatusWsMessage deliveryFinished(Long orderId, Long userId, Long riderUserId) {
		OrderStatusWsMessage message = base(orderId, userId, "DELIVERY_FINISHED");
		message.setTitle("订单状态通知");
		message.setContent("订单已送达，祝您用餐愉快");
		message.setRiderUserId(riderUserId);
		return message;
	}

	private static OrderStatusWsMessage base(Long orderId, Long userId, String eventType) {
		OrderStatusWsMessage message = new OrderStatusWsMessage();
		message.setCategory(CATEGORY);
		message.setBusinessType(BUSINESS_TYPE);
		message.setEventType(eventType);
		message.setStatus("SUCCESS");
		message.setTimestamp(System.currentTimeMillis());
		message.setOrderId(orderId);
		message.setUserId(userId);
		return message;
	}

}