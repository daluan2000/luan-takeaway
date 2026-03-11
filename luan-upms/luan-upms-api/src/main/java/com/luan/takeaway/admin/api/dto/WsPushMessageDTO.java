package com.luan.takeaway.admin.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * WebSocket 推送消息参数。
 */
@Data
@Accessors(chain = true)
public class WsPushMessageDTO {

	/**
	 * 是否广播。
	 */
	private Boolean needBroadcast;

	/**
	 * 目标会话 key 列表。
	 */
	private List<Object> sessionKeys;

	/**
	 * 推送文本。
	 */
	private String messageText;

}
