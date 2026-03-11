package com.luan.takeaway.takeaway.dish.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.common.core.util.RedisUtils;
import com.luan.takeaway.takeaway.dish.constant.DishStockMqConstants;
import com.luan.takeaway.takeaway.dish.mq.dto.DishStockDeductEvent;
import com.luan.takeaway.takeaway.dish.service.WmDishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 菜品库存扣减消息消费者，负责异步落库。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DishStockDeductConsumer {

	private static final long CONSUME_DONE_EXPIRE_DAYS = 7;

	private final ObjectMapper objectMapper;

	private final WmDishService wmDishService;

	@RabbitListener(queues = DishStockMqConstants.QUEUE)
	public void onMessage(String payloadText) {
		// 空消息直接忽略，避免无效反序列化开销。
		if (!StringUtils.hasText(payloadText)) {
			return;
		}
		try {
			// 将消息体反序列化为库存扣减事件对象。
			DishStockDeductEvent event = objectMapper.readValue(payloadText, DishStockDeductEvent.class);

			// 基于订单号构建消费完成标记。
			// 在 MQ 至少一次投递模型下，可能出现重复消息，先查标记可避免重复扣减数据库库存。
			String doneKey = wmDishService.buildConsumeDoneKey(event.getOrderNo());
			if (doneKey != null && RedisUtils.hasKey(doneKey)) {
				return;
			}

			// 将 Redis 已预扣减的库存异步落库到 MySQL，保障最终一致性。
			// 如果该步骤抛异常，消息会按 MQ 配置进行重试。
			wmDishService.asyncDeductStockToDb(event.getItems(), event.getMerchantUserId());
			if (doneKey != null) {
				// 落库成功后写入完成标记，并设置过期时间防止 key 无限制增长。
				RedisUtils.set(doneKey, "1", CONSUME_DONE_EXPIRE_DAYS, TimeUnit.DAYS);
			}
		}
		catch (Exception e) {
			log.error("消费库存异步落库消息失败, payload={}", payloadText, e);
			throw new IllegalStateException("消费库存异步落库消息失败", e);
		}
	}

}
