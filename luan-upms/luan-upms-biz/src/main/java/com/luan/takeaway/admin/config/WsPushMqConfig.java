package com.luan.takeaway.admin.config;

import com.luan.takeaway.admin.api.constant.WsPushMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebSocket 推送 RabbitMQ 配置。
 */
@Configuration
@ConditionalOnProperty(prefix = "takeaway.ws.push.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WsPushMqConfig {

	@Bean
	public DirectExchange wsPushExchange() {
		return new DirectExchange(WsPushMqConstants.EXCHANGE, true, false);
	}

	@Bean
	public Queue wsPushQueue() {
		return new Queue(WsPushMqConstants.QUEUE, true);
	}

	@Bean
	public Binding wsPushBinding(Queue wsPushQueue, DirectExchange wsPushExchange) {
		return BindingBuilder.bind(wsPushQueue).to(wsPushExchange).with(WsPushMqConstants.ROUTING_KEY);
	}

}
