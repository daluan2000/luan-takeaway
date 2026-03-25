package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.ai.model.IntentMode;
import com.luan.takeaway.ai.model.IntentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Query Understanding 服务。
 * <p>
 * 负责完成 AI 推荐链路的第一步：Query Understanding。
 * </p>
 * 由 LLM 完成两步解析：
 * <ol>
 *   <li>识别请求模式标签（TOOL_CALLING / RAG），用于意图提取与结果解释辅助，不做二选一路由。</li>
 *   <li>提取结构化意图（类别、预算、辣度、营养区间、场景标签等）和语义意图。</li>
 * </ol>
 * <p>
 * 该服务统一封装调用入口，不做模式二选一决策，实际检索链路由
 * {@link HybridRecommendationService} 根据意图内容自主判断。
 *
 * @see OpenAiIntentRecognizer#parseQueryUnderstanding(String)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryUnderstandingService {

	private final AiAssistantProperties properties;

	private final OpenAiIntentRecognizer openAiIntentRecognizer;

	/**
	 * 执行 Query Understanding 主入口。
	 * <p>
	 * 流程：
	 * <ol>
	 *   <li>调用 LLM 解析用户输入，同时产出模式标签 + 结构化意图 + 语义意图。</li>
	 *   <li>校验解析结果，空结果降级返回默认意图。</li>
	 * </ol>
	 *
	 * @param query 用户自然语言输入
	 * @return 解析后的结构化 + 语义意图
	 */
	public IntentResult understand(String query) {
		ensureLlmEnabled();
		if (query == null || query.isBlank()) {
			log.warn("[QueryUnderstanding] 收到空查询，直接返回默认意图");
			return buildDefaultIntent();
		}

		log.debug("[QueryUnderstanding] 开始解析 query={}", maskQuery(query));
		IntentResult result;
		try {
			result = openAiIntentRecognizer.parseQueryUnderstanding(query);
		}
		catch (Exception e) {
			log.warn("[QueryUnderstanding] LLM 解析失败，降级规则兜底: {}", e.getMessage());
			result = buildFallbackIntent(query);
		}

		if (result == null) {
			result = buildFallbackIntent(query);
		}

		if (result.getOriginalQuery() == null) {
			result.setOriginalQuery(query);
		}

		logStructuredIntent(result);
		return result;
	}

	/**
	 * 根据用户输入快速推断请求模式标签。
	 * <p>
	 * 辅助下游做参考解释，非路由决定。
	 * 当用户有明确可筛选条件（品类/预算/辣度等）时返回 TOOL_CALLING，
	 * 当用户是模糊健康或场景诉求（如胃不舒服/上火）时返回 RAG。
	 */
	public IntentMode inferMode(String query) {
		ensureLlmEnabled();
		try {
			return openAiIntentRecognizer.decideMode(query);
		}
		catch (Exception e) {
			log.warn("[QueryUnderstanding] 模式推断失败，默认 TOOL_CALLING: {}", e.getMessage());
			return IntentMode.TOOL_CALLING;
		}
	}

	private void ensureLlmEnabled() {
		if (!properties.getLlm().isEnabled()) {
			throw new IllegalStateException("LLM 已禁用，请设置 ai.assistant.llm.enabled=true");
		}
	}

	private IntentResult buildDefaultIntent() {
		IntentResult intent = new IntentResult();
		intent.setOriginalQuery("");
		intent.setMode(IntentMode.TOOL_CALLING);
		return intent;
	}

	private IntentResult buildFallbackIntent(String query) {
		IntentResult intent = new IntentResult();
		intent.setOriginalQuery(query);
		intent.setMode(IntentMode.TOOL_CALLING);
		// 简单关键词兜底
		String lower = query.toLowerCase();
		if (lower.contains("辣")) {
			intent.setSpicy(true);
		}
		if (lower.contains("不辣") || lower.contains("清淡")) {
			intent.setLightTaste(true);
			intent.setSpicy(false);
		}
		if (lower.contains("面")) {
			intent.setCategory("面");
		}
		else if (lower.contains("饭") || lower.contains("盖浇")) {
			intent.setCategory("米饭");
		}
		else if (lower.contains("粥")) {
			intent.setCategory("粥");
		}
		return intent;
	}

	private void logStructuredIntent(IntentResult result) {
		log.debug("[QueryUnderstanding] 解析完成, mode={}, category={}, priceMax={}, spicy={}, lightTaste={}, "
						+ "mealTime={}, tags={}, suitableScenes={}, avoidScenes={}, keywords={}, hasStructured={}, hasSemantic={}",
				result.getMode(),
				result.getCategory(),
				result.getPriceMax(),
				result.getSpicy(),
				result.getLightTaste(),
				result.getMealTime(),
				result.getTags(),
				result.getSuitableScenes(),
				result.getAvoidScenes(),
				result.getKeywords(),
				result.hasStructuredConstraints(),
				result.hasSemanticConstraints());
	}

	private String maskQuery(String query) {
		if (query == null) {
			return "<null>";
		}
		return query.length() > 100 ? query.substring(0, 100) + "..." : query;
	}

}
