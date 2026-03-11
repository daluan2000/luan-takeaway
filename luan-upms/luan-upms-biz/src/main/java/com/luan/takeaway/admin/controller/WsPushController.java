package com.luan.takeaway.admin.controller;

import com.luan.takeaway.admin.api.dto.WsPushMessageDTO;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.security.annotation.Inner;
import com.luan.takeaway.common.websocket.distribute.MessageDO;
import com.luan.takeaway.common.websocket.distribute.MessageDistributor;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一 WebSocket 消息推送入口。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/ws")
public class WsPushController {

	private final ObjectProvider<MessageDistributor> messageDistributorProvider;

	@Inner
	@PostMapping("/push")
	public R<Boolean> push(@RequestBody WsPushMessageDTO dto) {
		if (dto == null || !StringUtils.hasText(dto.getMessageText())) {
			return R.failed(false, "推送内容不能为空");
		}

		MessageDistributor messageDistributor = messageDistributorProvider.getIfAvailable();
		if (messageDistributor == null) {
			return R.failed(false, "未配置 WebSocket 分发器");
		}

		MessageDO messageDO = new MessageDO().setMessageText(dto.getMessageText())
			.setNeedBroadcast(dto.getNeedBroadcast())
			.setSessionKeys(dto.getSessionKeys());
		messageDistributor.distribute(messageDO);
		return R.ok(Boolean.TRUE);
	}

}
