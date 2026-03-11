package com.luan.takeaway.admin.api.constant;

/**
 * WebSocket 推送消息队列常量。
 */
public final class WsPushMqConstants {

	private WsPushMqConstants() {
	}

	public static final String EXCHANGE = "takeaway.ws.push.exchange";

	public static final String QUEUE = "takeaway.ws.push.queue";

	public static final String ROUTING_KEY = "takeaway.ws.push.routing-key";

}
