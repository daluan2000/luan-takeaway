package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.ai.api.dto.AiAssistantRequest;
import com.luan.takeaway.ai.api.dto.AiAssistantResponse;
import com.luan.takeaway.ai.api.dto.IntentView;
import com.luan.takeaway.ai.model.IntentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AI 助手门面服务。
 * <p>
 * 负责请求参数兜底、调用混合推荐主流程、以及把内部意图模型转换为对外展示模型。
 * <p>
 * 完整链路（文档 # 8）：
 * Query Understanding → Hybrid Retrieval → Fusion Ranking → LLM Selection and Generation
 */
@Service
@RequiredArgsConstructor
public class AiAssistantFacadeService {

	private final AiAssistantProperties properties;

	private final HybridRecommendationService hybridRecommendationService;

	/**
	 * 执行 AI 推荐主流程。
	 * <p>
	 * 流程：校验请求 -> 计算 limit -> 调用 HybridRecommendationService -> 组装响应。
	 */
	public AiAssistantResponse recommend(AiAssistantRequest request) {
		validateRequest(request);
		int limit = request.getLimit() == null || request.getLimit() <= 0
				? properties.getMaxRecommendation()
				: request.getLimit();

		HybridRecommendationService.HybridOutput output = hybridRecommendationService.recommend(
				request.getMessage(), request.getMerchantUserId(), limit);

		IntentResult intent = output.intent();
		AiAssistantResponse response = new AiAssistantResponse();
		response.setDecisionPath(output.decisionPath());
		response.setIntent(toIntentView(intent));
		response.setRecommendations(output.recommendations());
		response.setSummary(output.summary());
		return response;
	}

	/**
	 * 基础参数校验，避免空 message 进入后续模型处理。
	 */
	private void validateRequest(AiAssistantRequest request) {
		if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
			throw new IllegalArgumentException("message 不能为空");
		}
	}

	/**
	 * 内部意图对象转为 API 展示对象。
	 */
	private IntentView toIntentView(IntentResult intent) {
		IntentView view = new IntentView();
		view.setRoute(intent.getMode() != null ? intent.getMode().name() : "TOOL_CALLING");
		view.setOriginalQuery(intent.getOriginalQuery());
		view.setCategory(intent.getCategory());
		view.setPriceMax(intent.getPriceMax());
		view.setSpicy(intent.getSpicy());
		view.setPeople(intent.getPeople());
		view.setKeywords(intent.getKeywords());
		view.setSpicyLevel(intent.getSpicyLevel());
		view.setLightTaste(intent.getLightTaste());
		view.setOily(intent.getOily());
		view.setSoupBased(intent.getSoupBased());
		view.setVegetarian(intent.getVegetarian());
		view.setCaloriesMin(intent.getCaloriesMin());
		view.setCaloriesMax(intent.getCaloriesMax());
		view.setMealTime(intent.getMealTime());
		view.setPortionSize(intent.getPortionSize());
		view.setTags(intent.getTags());
		view.setSuitableScenes(intent.getSuitableScenes());
		view.setAvoidScenes(intent.getAvoidScenes());
		view.setSuitablePeople(intent.getSuitablePeople());
		view.setQueryRewrite(intent.getQueryRewrite());
		return view;
	}

}
