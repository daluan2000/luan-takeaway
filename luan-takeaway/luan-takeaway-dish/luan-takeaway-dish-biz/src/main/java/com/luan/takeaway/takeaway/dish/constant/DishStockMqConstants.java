package com.luan.takeaway.takeaway.dish.constant;

/**
 * 菜品库存异步落库 MQ 常量。
 */
public final class DishStockMqConstants {

	private DishStockMqConstants() {
	}

	public static final String EXCHANGE = "takeaway.dish.stock.exchange";

	public static final String QUEUE = "takeaway.dish.stock.queue";

	public static final String ROUTING_KEY = "takeaway.dish.stock.deduct";

}
