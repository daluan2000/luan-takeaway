package com.luan.takeaway.admin.api.feign;

import com.luan.takeaway.admin.api.dto.WsPushMessageDTO;
import com.luan.takeaway.common.core.constant.ServiceNameConstants;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.feign.annotation.NoToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 统一 WebSocket 推送远程接口。
 */
@FeignClient(contextId = "remoteWsPushService", value = ServiceNameConstants.UPMS_SERVICE)
public interface RemoteWsPushService {

	/**
	 * 向指定会话或全量会话推送消息。
	 * @param dto 推送参数
	 * @return 推送结果
	 */
	@NoToken
	@PostMapping("/ws/push")
	R<Boolean> push(@RequestBody WsPushMessageDTO dto);

}
