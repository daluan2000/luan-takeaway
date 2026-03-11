package com.luan.takeaway.admin.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.admin.api.constant.WsPushMqConstants;
import com.luan.takeaway.admin.api.dto.WsPushMessageDTO;
import com.luan.takeaway.admin.service.WsPushDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * WebSocket 推送 MQ 消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "takeaway.ws.push.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WsPushMqConsumer {

	private final ObjectMapper objectMapper;

	private final WsPushDispatchService wsPushDispatchService;

	@RabbitListener(queues = WsPushMqConstants.QUEUE)
	public void onMessage(String payloadText) {
		if (!StringUtils.hasText(payloadText)) {
			return;
		}

		try {
			WsPushMessageDTO dto = objectMapper.readValue(payloadText, WsPushMessageDTO.class);
			wsPushDispatchService.push(dto);
		}
		catch (Exception e) {
			log.error("消费WebSocket推送MQ消息失败, payload={}", payloadText, e);
		}
	}

}
