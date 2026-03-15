package com.luan.takeaway.ai.api.feign;

import com.luan.takeaway.ai.api.dto.AiAssistantRequest;
import com.luan.takeaway.ai.api.dto.AiAssistantResponse;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.feign.annotation.NoToken;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeGenerateEvent;
import com.luan.takeaway.takeaway.common.constant.TakeawayServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI 点餐助手 Feign 接口。
 * <p>
 * 这是其他模块（例如用户端业务模块、聚合层）调用 AI 模块的标准入口，
 * 通过服务名 {@code TAKEAWAY_AI_SERVICE} 走服务发现/负载均衡转发。
 */
@FeignClient(contextId = "remoteAiAssistantService", value = TakeawayServiceNameConstants.TAKEAWAY_AI_SERVICE)
public interface RemoteAiAssistantService {

	/**
	 * 对外提供“点餐推荐”能力。
	 * <p>
	 * 交互说明：调用方提交用户自然语言请求，AI 模块返回结构化意图 + 推荐列表 + 摘要。
	 */
	@PostMapping("/ai/assistant/recommend")
	R<AiAssistantResponse> recommend(@RequestBody AiAssistantRequest request);

	/**
	 * 同步生成单个菜品的知识文档。
	 * <p>
	 * 交互说明：通常由菜品模块在菜品变更后触发，用于更新 AI 侧知识画像。
	 * 该接口标记 {@link NoToken}，用于系统内部无用户态令牌调用。
	 */
	@NoToken
	@PostMapping("/ai/assistant/knowledge/generate")
	R<DishKnowledgeDoc> generateKnowledgeDoc(@RequestBody DishKnowledgeGenerateEvent request);

}
