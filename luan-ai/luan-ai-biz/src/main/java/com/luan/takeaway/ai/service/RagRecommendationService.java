package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.api.dto.RecommendationItem;
import com.luan.takeaway.ai.model.IntentResult;
import com.luan.takeaway.ai.model.KnowledgeSnippet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagRecommendationService {

	private final KnowledgeRetriever knowledgeRetriever;

	private final ToolCallingService toolCallingService;

	public RagOutput recommend(IntentResult intent, Long merchantUserId, int limit) {
		List<KnowledgeSnippet> snippets = knowledgeRetriever.retrieve(intent.getOriginalQuery(), 3);
		List<RecommendationItem> recommendations = toolCallingService.recommend(intent, merchantUserId, limit, false);
		List<String> evidence = snippets.stream()
				.map(snippet -> String.format("%s（score=%.1f）：%s", snippet.getTitle(), snippet.getScore(), snippet.getContent()))
				.collect(Collectors.toList());

		String summary = buildSummary(snippets, recommendations);
		return new RagOutput(recommendations, evidence, summary);
	}

	private String buildSummary(List<KnowledgeSnippet> snippets, List<RecommendationItem> recommendations) {
		List<String> lines = new ArrayList<>();
		if (!snippets.isEmpty()) {
			lines.add("根据你的描述，我先做了语义知识匹配：");
			for (KnowledgeSnippet snippet : snippets) {
				lines.add("- " + snippet.getContent());
			}
		}
		if (recommendations.isEmpty()) {
			lines.add("当前没有检索到足够匹配的菜品，建议换个关键词再试一次。\n");
		}
		else {
			lines.add("结合你当前可下单菜品，推荐如下：");
			for (int i = 0; i < recommendations.size(); i++) {
				RecommendationItem item = recommendations.get(i);
				lines.add((i + 1) + ". " + item.getDishName() + "（" + item.getPrice() + "元）- " + item.getReason());
			}
		}
		return String.join("\n", lines);
	}

	public record RagOutput(List<RecommendationItem> recommendations, List<String> knowledgeEvidence, String summary) {
	}

}