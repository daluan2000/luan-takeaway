package com.luan.takeaway.takeaway.dish.config;

import com.luan.takeaway.takeaway.dish.constant.DishStockMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 菜品库存异步落库 MQ 配置。
 */
@Configuration
public class DishStockMqConfig {

	@Bean
	public DirectExchange dishStockExchange() {
		return new DirectExchange(DishStockMqConstants.EXCHANGE, true, false);
	}

	@Bean
	public Queue dishStockQueue() {
		return new Queue(DishStockMqConstants.QUEUE, true);
	}

	@Bean
	public Binding dishStockBinding(Queue dishStockQueue, DirectExchange dishStockExchange) {
		return BindingBuilder.bind(dishStockQueue).to(dishStockExchange).with(DishStockMqConstants.ROUTING_KEY);
	}

}
