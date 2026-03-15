package com.luan.takeaway.takeaway.dish.config;

import com.luan.takeaway.takeaway.common.constant.TakeawayAiMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DishKnowledgeMqConfig {

	@Bean
	public TopicExchange dishKnowledgeExchange() {
		return new TopicExchange(TakeawayAiMqConstants.DISH_KNOWLEDGE_EXCHANGE, true, false);
	}

	@Bean
	public Queue dishKnowledgeUpsertQueue() {
		return new Queue(TakeawayAiMqConstants.DISH_KNOWLEDGE_UPSERT_QUEUE, true);
	}

	@Bean
	public Binding dishKnowledgeUpsertBinding(Queue dishKnowledgeUpsertQueue, TopicExchange dishKnowledgeExchange) {
		return BindingBuilder.bind(dishKnowledgeUpsertQueue)
			.to(dishKnowledgeExchange)
			.with(TakeawayAiMqConstants.DISH_KNOWLEDGE_UPSERT_ROUTING_KEY);
	}

}
