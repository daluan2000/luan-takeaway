package com.luan.takeaway.takeaway.order.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.admin.api.constant.WsPushMqConstants;
import com.luan.takeaway.admin.api.dto.WsPushMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 订单状态通知 MQ 发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusMqPublisher {

	private final RabbitTemplate rabbitTemplate;

	private final ObjectMapper objectMapper;

	@Value("${takeaway.ws.push.mq.enabled:true}")
	private boolean mqEnabled;

	public boolean publish(Long orderId, Long userId, String eventType, String messageText) {
		// 通过开关可在本地联调或故障演练时快速关闭 MQ 推送，不影响主业务流程验证。
		if (!mqEnabled) {
			return false;
		}

		// 统一封装成 WsPushMessageDTO，是为了和 upms 侧消费者协议保持一致。
		// 业务方只需要告诉“推给谁(sessionKeys) + 推什么(messageText)”。
		WsPushMessageDTO payload = new WsPushMessageDTO().setMessageText(messageText)
			.setSessionKeys(Collections.singletonList(String.valueOf(userId)));

		try {
			// MQ 里传字符串可以减少跨服务反序列化兼容问题（尤其是 DTO 版本演进时）。
			String payloadText = objectMapper.writeValueAsString(payload);
			rabbitTemplate.convertAndSend(WsPushMqConstants.EXCHANGE, WsPushMqConstants.ROUTING_KEY, payloadText);
			log.info("订单状态通知MQ发送成功, orderId={}, userId={}, eventType={}", orderId, userId, eventType);
			return true;
		}
		catch (JsonProcessingException e) {
			// 这类错误通常是对象结构异常或字段不可序列化，属于代码层问题，按 error 打印。
			log.error("订单状态通知MQ消息序列化失败, orderId={}, userId={}, eventType={}", orderId, userId, eventType, e);
		}
		catch (Exception e) {
			// MQ 异常先记录告警并返回 false，由上层决定是否重试/补偿。
			log.warn("订单状态通知MQ发送失败, orderId={}, userId={}, eventType={}, message={}", orderId, userId, eventType,
					e.getMessage());
		}

		return false;
	}

}