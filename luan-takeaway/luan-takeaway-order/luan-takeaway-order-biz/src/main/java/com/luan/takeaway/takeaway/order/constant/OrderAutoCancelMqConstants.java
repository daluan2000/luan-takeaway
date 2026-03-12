package com.luan.takeaway.takeaway.order.constant;

/**
 * 订单自动取消延时消息常量。
 */
public final class OrderAutoCancelMqConstants {

	private OrderAutoCancelMqConstants() {
	}

	/**
	 * 订单下单后 10 分钟未支付自动取消。
	 */
	public static final long AUTO_CANCEL_DELAY_MS = 10 * 60 * 1000L;

	/**
	 * 延时交换机：新下单事件先进入该交换机绑定的延时队列。
	 */
	public static final String DELAY_EXCHANGE = "takeaway.order.auto-cancel.delay.exchange";

	/**
	 * 延时队列：消息先在该队列等待 TTL 到期。
	 */
	public static final String DELAY_QUEUE = "takeaway.order.auto-cancel.delay.queue";

	/**
	 * 下单事件投递到延时交换机时使用的路由键。
	 */
	public static final String DELAY_ROUTING_KEY = "takeaway.order.auto-cancel.delay";

	/**
	 * 死信交换机：延时队列消息过期后自动路由到这里。
	 */
	public static final String DEAD_LETTER_EXCHANGE = "takeaway.order.auto-cancel.exchange";

	/**
	 * 实际消费队列：消费者监听该队列执行自动取消。
	 */
	public static final String DEAD_LETTER_QUEUE = "takeaway.order.auto-cancel.queue";

	/**
	 * 死信路由键：用于将过期消息精确投递到自动取消消费队列。
	 */
	public static final String DEAD_LETTER_ROUTING_KEY = "takeaway.order.auto-cancel";

}
