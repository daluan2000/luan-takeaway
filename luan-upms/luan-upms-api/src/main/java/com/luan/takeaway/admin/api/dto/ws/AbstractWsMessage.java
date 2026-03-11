package com.luan.takeaway.admin.api.dto.ws;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 业务 WebSocket 消息抽象基类。
 */
@Data
@Accessors(chain = true)
public abstract class AbstractWsMessage {

	/**
	 * 消息类别。
	 */
	private String category;

	/**
	 * 业务标识。
	 */
	private String businessType;

	/**
	 * 事件类型。
	 */
	private String eventType;

	/**
	 * 消息状态。
	 */
	private WsMessageStatus status;

	/**
	 * 标题。
	 */
	private String title;

	/**
	 * 内容。
	 */
	private String content;

	/**
	 * 消息时间戳（毫秒）。
	 */
	private Long timestamp;

}
