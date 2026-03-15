package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.ai.model.IntentMode;
import com.luan.takeaway.ai.model.IntentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 意图识别编排服务。
 * <p>
 * 该类统一封装“模式决策 + 结构化提取 + RAG 建议生成”的调用入口，
 * 当前实现依赖 LLM，未启用时会直接抛出明确异常。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntentRecognitionService {

	private final AiAssistantProperties properties;

	private final OpenAiIntentRecognizer openAiIntentRecognizer;

	/**
	 * 判定当前问题走 TOOL_CALLING 还是 RAG。
	 */
	public IntentMode decideMode(String query) {
		ensureLlmEnabled();
		log.debug("AI decideMode start, llmEnabled={}, source={}", properties.getLlm().isEnabled(),
				properties.getLlm().getSource());
		try {
			IntentMode mode = openAiIntentRecognizer.decideMode(query);
			log.debug("AI decideMode by LLM, mode={}", mode);
			return mode;
		}
		catch (Exception e) {
			throw new IllegalStateException("LLM 模式识别失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 提取结构化条件（混合场景，可指定模式）。
	 */
	public IntentResult extractHybridIntent(String query, IntentMode mode) {
		return extractStructuredIntent(query, mode == null ? IntentMode.TOOL_CALLING : mode, null);
	}

	/**
	 * 调用 LLM 输出结构化意图。
	 */
	private IntentResult extractStructuredIntent(String query, IntentMode targetMode, String extraContext) {
		ensureLlmEnabled();
		try {
			IntentResult llmResult = openAiIntentRecognizer.extractStructuredIntent(query, targetMode, extraContext);
			if (llmResult == null) {
				throw new IllegalStateException("LLM 返回了空的结构化意图");
			}
			llmResult.setMode(targetMode);
			return llmResult;
		}
		catch (Exception e) {
			throw new IllegalStateException("LLM 结构化提取失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 强制校验 LLM 开关，避免误用导致不可预期降级。
	 */
	private void ensureLlmEnabled() {
		if (!properties.getLlm().isEnabled()) {
			throw new IllegalStateException("LLM 已禁用，请设置 ai.assistant.llm.enabled=true");
		}
	}

}