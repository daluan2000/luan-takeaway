package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.ai.api.dto.AiAssistantRequest;
import com.luan.takeaway.ai.api.dto.AiAssistantResponse;
import com.luan.takeaway.ai.api.dto.IntentView;
import com.luan.takeaway.ai.api.dto.RecommendationItem;
import com.luan.takeaway.ai.model.IntentMode;
import com.luan.takeaway.ai.model.IntentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiAssistantFacadeService {

	private final AiAssistantProperties properties;

	private final IntentRecognitionService intentRecognitionService;

	private final ToolCallingService toolCallingService;

	private final RagRecommendationService ragRecommendationService;

	public AiAssistantResponse recommend(AiAssistantRequest request) {
		validateRequest(request);
		int limit = request.getLimit() == null || request.getLimit() <= 0 ? properties.getMaxRecommendation()
				: request.getLimit();

		IntentMode mode = intentRecognitionService.decideMode(request.getMessage());
		IntentResult intent;
		List<RecommendationItem> recommendations;
		List<String> knowledgeEvidence;
		String decisionPath;
		String summary;

		if (mode == IntentMode.RAG) {
			RagRecommendationService.RagOutput ragOutput = ragRecommendationService.recommend(request.getMessage(),
					request.getMerchantUserId(), limit);
			intent = ragOutput.intent();
			recommendations = ragOutput.recommendations();
			knowledgeEvidence = ragOutput.knowledgeEvidence();
			decisionPath = "rag";
			summary = ragOutput.summary();
		}
		else {
			intent = intentRecognitionService.extractToolIntent(request.getMessage());
			recommendations = toolCallingService.recommend(intent, request.getMerchantUserId(), limit, true);
			knowledgeEvidence = List.of();
			decisionPath = "tool-calling";
			summary = toolCallingService.buildSummary(intent, recommendations);
		}

		AiAssistantResponse response = new AiAssistantResponse();
		response.setDecisionPath(decisionPath);
		response.setIntent(toIntentView(intent));
		response.setRecommendations(recommendations);
		response.setKnowledgeEvidence(knowledgeEvidence);
		response.setSummary(summary);
		return response;
	}

	private void validateRequest(AiAssistantRequest request) {
		if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
			throw new IllegalArgumentException("message 不能为空");
		}
	}

	private IntentView toIntentView(IntentResult intent) {
		IntentView view = new IntentView();
		view.setRoute(intent.getMode().name());
		view.setOriginalQuery(intent.getOriginalQuery());
		view.setCategory(intent.getCategory());
		view.setPriceMax(intent.getPriceMax());
		view.setSpicy(intent.getSpicy());
		view.setPeople(intent.getPeople());
		view.setPreferLight(intent.getPreferLight());
		view.setKeywords(intent.getKeywords());
		return view;
	}

}