package com.luan.takeaway.takeaway.order.config;

import com.luan.takeaway.takeaway.order.constant.OrderAutoCancelMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单超时自动取消延时队列配置。
 */
@Configuration
public class OrderAutoCancelMqConfig {

	/**
	 * 延时交换机。
	 *
	 * 业务发送消息时统一投递到这里，再按 routingKey 进入延时队列。
	 */
	@Bean
	public DirectExchange orderAutoCancelDelayExchange() {
		return new DirectExchange(OrderAutoCancelMqConstants.DELAY_EXCHANGE, true, false);
	}

	/**
	 * 死信交换机。
	 *
	 * 延时队列中的消息到期后会自动成为死信并路由到该交换机。
	 */
	@Bean
	public DirectExchange orderAutoCancelDeadLetterExchange() {
		return new DirectExchange(OrderAutoCancelMqConstants.DEAD_LETTER_EXCHANGE, true, false);
	}

	/**
	 * 延时队列。
	 *
	 * 这里不配置固定队列 TTL，而是由生产者按消息粒度设置 expiration。
	 * 这样后续若有不同业务需要不同超时阈值，也可以复用同一套队列设施。
	 */
	@Bean
	public Queue orderAutoCancelDelayQueue() {
		Map<String, Object> args = new HashMap<>(2);
		// 指定死信交换机：消息过期后不丢弃，继续流转到自动取消消费链路。
		args.put("x-dead-letter-exchange", OrderAutoCancelMqConstants.DEAD_LETTER_EXCHANGE);
		// 指定死信路由键：确保死信能精确路由到自动取消队列。
		args.put("x-dead-letter-routing-key", OrderAutoCancelMqConstants.DEAD_LETTER_ROUTING_KEY);
		return new Queue(OrderAutoCancelMqConstants.DELAY_QUEUE, true, false, false, args);
	}

	/**
	 * 自动取消消费队列。
	 *
	 * 该队列不再做延时，消费者收到即执行“仅待支付可取消”的幂等更新。
	 */
	@Bean
	public Queue orderAutoCancelDeadLetterQueue() {
		return new Queue(OrderAutoCancelMqConstants.DEAD_LETTER_QUEUE, true);
	}

	/**
	 * 延时交换机 -> 延时队列 绑定关系。
	 */
	@Bean
	public Binding orderAutoCancelDelayBinding(Queue orderAutoCancelDelayQueue,
			DirectExchange orderAutoCancelDelayExchange) {
		return BindingBuilder.bind(orderAutoCancelDelayQueue)
			.to(orderAutoCancelDelayExchange)
			.with(OrderAutoCancelMqConstants.DELAY_ROUTING_KEY);
	}

	/**
	 * 死信交换机 -> 自动取消消费队列 绑定关系。
	 */
	@Bean
	public Binding orderAutoCancelDeadLetterBinding(Queue orderAutoCancelDeadLetterQueue,
			DirectExchange orderAutoCancelDeadLetterExchange) {
		return BindingBuilder.bind(orderAutoCancelDeadLetterQueue)
			.to(orderAutoCancelDeadLetterExchange)
			.with(OrderAutoCancelMqConstants.DEAD_LETTER_ROUTING_KEY);
	}

}
