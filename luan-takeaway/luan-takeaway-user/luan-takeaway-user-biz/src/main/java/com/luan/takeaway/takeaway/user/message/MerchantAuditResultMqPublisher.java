package com.luan.takeaway.takeaway.user.message;

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
 * 商家审核结果消息发布器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantAuditResultMqPublisher {

	private final RabbitTemplate rabbitTemplate;

	private final ObjectMapper objectMapper;

	@Value("${takeaway.ws.push.mq.enabled:true}")
	private boolean mqEnabled;

	public boolean publish(Long merchantId, Long userId, String messageText) {
		if (!mqEnabled) {
			return false;
		}

		WsPushMessageDTO payload = new WsPushMessageDTO().setMessageText(messageText)
			.setSessionKeys(Collections.singletonList(String.valueOf(userId)));

		try {
			String payloadText = objectMapper.writeValueAsString(payload);
			rabbitTemplate.convertAndSend(WsPushMqConstants.EXCHANGE, WsPushMqConstants.ROUTING_KEY, payloadText);
			log.info("商家审核结果MQ发送成功, merchantId={}, userId={}", merchantId, userId);
			return true;
		}
		catch (JsonProcessingException e) {
			log.error("商家审核结果MQ消息序列化失败, merchantId={}, userId={}", merchantId, userId, e);
		}
		catch (Exception e) {
			log.warn("商家审核结果MQ发送失败, merchantId={}, userId={}, message={}", merchantId, userId, e.getMessage());
		}

		return false;
	}

}
