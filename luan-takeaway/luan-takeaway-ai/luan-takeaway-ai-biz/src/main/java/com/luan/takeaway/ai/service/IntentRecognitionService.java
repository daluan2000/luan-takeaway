package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.ai.model.IntentMode;
import com.luan.takeaway.ai.model.IntentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntentRecognitionService {

	private final AiAssistantProperties properties;

	private final OpenAiIntentRecognizer openAiIntentRecognizer;

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

	public IntentResult extractToolIntent(String query) {
		return extractStructuredIntent(query, IntentMode.TOOL_CALLING, null);
	}

	public IntentResult extractRagIntent(String query, String ragAdvice) {
		return extractStructuredIntent(query, IntentMode.RAG, ragAdvice);
	}

	public String generateRagAdvice(String query, List<String> knowledgeEvidence) {
		ensureLlmEnabled();
		try {
			String advice = openAiIntentRecognizer.generateRagAdvice(query, knowledgeEvidence);
			if (advice == null || advice.isBlank()) {
				throw new IllegalStateException("LLM 返回了空的 RAG 建议");
			}
			return advice;
		}
		catch (Exception e) {
			throw new IllegalStateException("LLM RAG建议生成失败: " + e.getMessage(), e);
		}
	}

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

	private void ensureLlmEnabled() {
		if (!properties.getLlm().isEnabled()) {
			throw new IllegalStateException("LLM 已禁用，请设置 ai.assistant.llm.enabled=true");
		}
	}

}