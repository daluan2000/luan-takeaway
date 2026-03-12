package com.luan.takeaway.takeaway.order.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.takeaway.order.constant.OrderAutoCancelMqConstants;
import com.luan.takeaway.takeaway.order.mq.dto.OrderAutoCancelEvent;
import com.luan.takeaway.takeaway.order.service.WmOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 订单超时自动取消消费者。
 */
@Slf4j
@Component
@AllArgsConstructor
public class OrderAutoCancelConsumer {

	/**
	 * JSON 序列化/反序列化工具。
	 */
	private final ObjectMapper objectMapper;

	/**
	 * 订单服务。
	 *
	 * 这里调用的是“条件更新”方法：仅当订单仍为待支付时才取消，
	 * 可以天然规避“刚好支付成功但取消消息到达”的并发竞态问题。
	 */
	private final WmOrderService wmOrderService;

	/**
	 * 消费死信队列中的超时订单消息，触发自动取消。
	 *
	 * 消费逻辑采用“尽量不抛异常中断队列”的策略：
	 * 1) 空消息/脏消息直接忽略；
	 * 2) 解析或业务异常记录错误日志，避免阻塞后续消息消费。
	 */
	@RabbitListener(queues = OrderAutoCancelMqConstants.DEAD_LETTER_QUEUE)
	public void onMessage(String payloadText) {
		// 防御式判断：避免空字符串触发不必要反序列化异常。
		if (!StringUtils.hasText(payloadText)) {
			return;
		}
		try {
			OrderAutoCancelEvent event = objectMapper.readValue(payloadText, OrderAutoCancelEvent.class);
			// 关键字段缺失说明消息不完整，直接忽略。
			if (event == null || event.getOrderId() == null) {
				return;
			}
			boolean canceled = wmOrderService.autoCancelIfUnpaid(event.getOrderId());
			if (canceled) {
				log.info("订单超时自动取消成功, orderId={}, orderNo={}", event.getOrderId(), event.getOrderNo());
			}
			// 若 canceled=false，代表订单已支付/已取消等，无需额外处理。
		} catch (Exception e) {
			// 记录 payload 便于线上排查“具体哪条消息”反序列化或业务处理失败。
			log.error("订单超时自动取消消费失败, payload={}", payloadText, e);
		}
	}

}
