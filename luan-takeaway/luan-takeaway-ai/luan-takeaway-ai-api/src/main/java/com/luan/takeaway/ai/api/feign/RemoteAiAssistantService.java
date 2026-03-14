package com.luan.takeaway.ai.api.feign;

import com.luan.takeaway.ai.api.dto.AiAssistantRequest;
import com.luan.takeaway.ai.api.dto.AiAssistantResponse;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.constant.TakeawayServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI 点餐助手 Feign 接口
 */
@FeignClient(contextId = "remoteAiAssistantService", value = TakeawayServiceNameConstants.TAKEAWAY_AI_SERVICE)
public interface RemoteAiAssistantService {

	@PostMapping("/ai/assistant/recommend")
	R<AiAssistantResponse> recommend(@RequestBody AiAssistantRequest request);

}
