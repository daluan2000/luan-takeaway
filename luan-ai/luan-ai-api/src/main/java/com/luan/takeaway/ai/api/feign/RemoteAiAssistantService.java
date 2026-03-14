package com.luan.takeaway.ai.api.feign;

import com.luan.takeaway.ai.api.dto.AiAssistantRequest;
import com.luan.takeaway.ai.api.dto.AiAssistantResponse;
import com.luan.takeaway.common.core.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI 点餐助手 Feign 接口
 */
@FeignClient(contextId = "remoteAiAssistantService", value = "luan-ai-biz")
public interface RemoteAiAssistantService {

	@PostMapping("/ai/assistant/recommend")
	R<AiAssistantResponse> recommend(@RequestBody AiAssistantRequest request);

}
