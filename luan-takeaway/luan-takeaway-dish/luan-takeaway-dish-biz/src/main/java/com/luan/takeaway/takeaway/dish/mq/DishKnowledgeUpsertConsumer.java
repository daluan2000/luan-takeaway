package com.luan.takeaway.takeaway.dish.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.takeaway.common.constant.TakeawayAiMqConstants;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeUpsertRequest;
import com.luan.takeaway.takeaway.dish.service.WmDishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class DishKnowledgeUpsertConsumer {

	private final ObjectMapper objectMapper;

	private final WmDishService wmDishService;

	@RabbitListener(queues = TakeawayAiMqConstants.DISH_KNOWLEDGE_UPSERT_QUEUE)
	public void onMessage(String payloadText) {
		if (!StringUtils.hasText(payloadText)) {
			return;
		}
		try {
			DishKnowledgeUpsertRequest request = objectMapper.readValue(payloadText, DishKnowledgeUpsertRequest.class);
			if (request == null || request.getDishId() == null || request.getKnowledgeDoc() == null) {
				log.warn("菜品知识回写消息参数不完整, payload={}", payloadText);
				return;
			}
			wmDishService.upsertKnowledgeDoc(request.getDishId(), request.getKnowledgeDoc());
		}
		catch (Exception ex) {
			log.error("消费菜品知识回写消息失败, payload={}", payloadText, ex);
		}
	}

}
