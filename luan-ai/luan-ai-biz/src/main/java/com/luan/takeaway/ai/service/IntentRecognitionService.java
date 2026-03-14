package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.ai.model.IntentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntentRecognitionService {

	private final AiAssistantProperties properties;

	private final RuleBasedIntentRecognizer ruleBasedIntentRecognizer;

	private final OpenAiIntentRecognizer openAiIntentRecognizer;

	public IntentResult recognize(String query) {
		if (properties.getLlm().isEnabled()) {
			try {
				IntentResult llmResult = openAiIntentRecognizer.recognize(query);
				if (llmResult != null) {
					return llmResult;
				}
			}
			catch (Exception e) {
				log.warn("LLM 意图识别失败，回退规则识别: {}", e.getMessage());
			}
		}
		return ruleBasedIntentRecognizer.recognize(query);
	}

}