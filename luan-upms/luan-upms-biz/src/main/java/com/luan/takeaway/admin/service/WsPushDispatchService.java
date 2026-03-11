package com.luan.takeaway.admin.service;

import com.luan.takeaway.admin.api.dto.WsPushMessageDTO;
import com.luan.takeaway.common.websocket.distribute.MessageDO;
import com.luan.takeaway.common.websocket.distribute.MessageDistributor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * WebSocket 推送分发服务，统一封装消息分发逻辑。
 */
@Service
@RequiredArgsConstructor
public class WsPushDispatchService {

	private final ObjectProvider<MessageDistributor> messageDistributorProvider;

	public boolean push(WsPushMessageDTO dto) {
		if (dto == null || !StringUtils.hasText(dto.getMessageText())) {
			return false;
		}

		MessageDistributor messageDistributor = messageDistributorProvider.getIfAvailable();
		if (messageDistributor == null) {
			return false;
		}

		MessageDO messageDO = new MessageDO().setMessageText(dto.getMessageText())
			.setNeedBroadcast(dto.getNeedBroadcast())
			.setSessionKeys(dto.getSessionKeys());
		messageDistributor.distribute(messageDO);
		return true;
	}

}
