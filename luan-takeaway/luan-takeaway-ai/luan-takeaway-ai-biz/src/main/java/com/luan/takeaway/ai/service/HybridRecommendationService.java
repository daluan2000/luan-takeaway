package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.api.dto.RecommendationItem;
import com.luan.takeaway.ai.model.IntentMode;
import com.luan.takeaway.ai.model.IntentResult;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.dto.HybridDishCandidateDTO;
import com.luan.takeaway.takeaway.common.dto.HybridDishSearchRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import com.luan.takeaway.takeaway.dish.api.RemoteDishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 混合推荐服务（结构化 + 语义 + 业务信号）。
 * 
 * AI推荐核心类，走完整个流程
 * 
 * 
 * <p>
 * 跨模块交互说明：通过 {@link RemoteDishService#searchHybridCandidates} 从菜品模块召回候选，
 * 本模块完成融合打分后，再交给 LLM 做小范围重排，得到最终推荐结果。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HybridRecommendationService {

	private final IntentRecognitionService intentRecognitionService;

	private final OpenAiIntentRecognizer openAiIntentRecognizer;

	private final RemoteDishService remoteDishService;

	/**
	 * 推荐主入口。
	 * <p>
	 * 流程：意图识别 -> 候选召回 -> 融合打分 -> LLM 重排 -> 组装摘要。
	 */
	public HybridOutput recommend(String query, Long merchantUserId, int limit) {
		IntentMode mode = intentRecognitionService.decideMode(query);
		IntentResult intent = intentRecognitionService.extractHybridIntent(query, mode);
		if (!StringUtils.hasText(intent.getOriginalQuery())) {
			intent.setOriginalQuery(query);
		}

		HybridDishSearchRequest request = new HybridDishSearchRequest();
		request.setMerchantUserId(merchantUserId);
		request.setLimit(Math.max(limit * 8, 120));
		request.setCategory(intent.getCategory());
		request.setPriceMax(intent.getPriceMax());
		request.setSpicy(intent.getSpicy());
		request.setSpicyLevel(intent.getSpicyLevel());
		request.setLightTaste(intent.getLightTaste() != null ? intent.getLightTaste() : intent.getPreferLight());
		request.setOily(intent.getOily());
		request.setSoupBased(intent.getSoupBased());
		request.setVegetarian(intent.getVegetarian());
		request.setCaloriesMin(intent.getCaloriesMin());
		request.setCaloriesMax(intent.getCaloriesMax());
		request.setProteinMin(intent.getProteinMin());
		request.setProteinMax(intent.getProteinMax());
		request.setFatMin(intent.getFatMin());
		request.setFatMax(intent.getFatMax());
		request.setCarbohydrateMin(intent.getCarbohydrateMin());
		request.setCarbohydrateMax(intent.getCarbohydrateMax());
		request.setMealTime(intent.getMealTime());
		request.setPortionSize(intent.getPortionSize());

		// package com.luan.takeaway.takeaway.dish.service.impl.WmDishServiceImpl.searchHybridCandidates;
		R<List<HybridDishCandidateDTO>> response = remoteDishService.searchHybridCandidates(request);
		List<HybridDishCandidateDTO> candidates = response == null || response.getData() == null ? List.of() : response.getData();

		List<ScoredCandidate> scoredCandidates = scoreCandidates(query, intent, candidates);
		if (scoredCandidates.isEmpty()) {
			return new HybridOutput(List.of(), List.of(), "当前没有检索到匹配菜品，建议放宽筛选条件重试。", intent,
					mode == IntentMode.RAG ? "rag" : "tool-calling");
		}

		scoredCandidates.sort(Comparator.comparing(ScoredCandidate::fusionScore).reversed());
		int rerankPoolSize = Math.min(scoredCandidates.size(), Math.max(limit * 4, 12));
		List<RecommendationItem> rerankPool = scoredCandidates.subList(0, rerankPoolSize)
			.stream()
			.map(ScoredCandidate::recommendation)
			.collect(Collectors.toList());

		List<RecommendationItem> finalItems;
		try {
			finalItems = openAiIntentRecognizer.rerankRecommendations(intent, rerankPool, limit);
		}
		catch (Exception ex) {
			log.warn("LLM 候选选取失败，回退规则排序: {}", ex.getMessage());
			finalItems = rerankPool.stream().limit(limit).collect(Collectors.toList());
		}

		List<String> evidence = scoredCandidates.stream()
			.limit(3)
			.map(item -> buildEvidence(item.candidate(), item.semanticScore()))
			.collect(Collectors.toList());
		String summary = buildSummary(mode, finalItems, evidence);
		return new HybridOutput(finalItems, evidence, summary, intent, mode == IntentMode.RAG ? "rag" : "tool-calling");
	}

	/**
	 * 对候选菜品逐个打分，生成可排序对象。
	 */
	private List<ScoredCandidate> scoreCandidates(String query, IntentResult intent, List<HybridDishCandidateDTO> candidates) {
		List<ScoredCandidate> scored = new ArrayList<>();
		for (HybridDishCandidateDTO candidate : candidates) {
			double structuredScore = computeStructuredScore(intent, candidate);
			double semanticScore = computeSemanticScore(query, intent, candidate.getKnowledgeDoc());
			double businessScore = computeBusinessScore(candidate);
			double fusionScore = structuredScore * 0.45D + semanticScore * 0.35D + businessScore * 0.20D;

			RecommendationItem recommendation = new RecommendationItem();
			recommendation.setDishId(candidate.getDishId());
			recommendation.setMerchantUserId(candidate.getMerchantUserId());
			recommendation.setDishName(candidate.getDishName());
			recommendation.setDishDesc(candidate.getDishDesc());
			recommendation.setPrice(candidate.getPrice());
			recommendation.setTags(buildTags(candidate.getKnowledgeDoc()));
			recommendation.setReason(buildReason(candidate.getKnowledgeDoc()));
			recommendation.setScore(fusionScore);
			scored.add(new ScoredCandidate(candidate, recommendation, structuredScore, semanticScore, businessScore, fusionScore));
		}
		return scored;
	}

	/**
	 * 结构化匹配分：类别、价格、辣度、餐段等硬条件命中分。
	 */
	private double computeStructuredScore(IntentResult intent, HybridDishCandidateDTO candidate) {
		double score = 0D;
		DishKnowledgeDoc doc = candidate.getKnowledgeDoc();

		if (intent.getCategory() != null && doc != null && intent.getCategory().equalsIgnoreCase(empty(doc.getCategory()))) {
			score += 2.2D;
		}
		if (intent.getPriceMax() != null && candidate.getPrice() != null && candidate.getPrice().compareTo(intent.getPriceMax()) <= 0) {
			score += 1.8D;
		}
		if (intent.getSpicy() != null && doc != null && intent.getSpicy().equals(doc.getSpicy())) {
			score += 1.2D;
		}
		if (intent.getLightTaste() != null && doc != null && intent.getLightTaste().equals(doc.getLightTaste())) {
			score += 1.0D;
		}
		if (intent.getMealTime() != null && !intent.getMealTime().isEmpty() && doc != null && overlap(intent.getMealTime(), doc.getMealTime())) {
			score += 0.8D;
		}
		return score;
	}

	/**
	 * 语义分：意图词与知识文档词的重合度。
	 */
	private double computeSemanticScore(String query, IntentResult intent, DishKnowledgeDoc doc) {
		if (doc == null) {
			return 0D;
		}
		Set<String> intentTerms = new LinkedHashSet<>();
		intentTerms.addAll(intent.getKeywords() == null ? List.of() : intent.getKeywords());
		intentTerms.addAll(intent.getTags() == null ? List.of() : intent.getTags());
		intentTerms.addAll(intent.getSuitableScenes() == null ? List.of() : intent.getSuitableScenes());
		intentTerms.addAll(intent.getSuitablePeople() == null ? List.of() : intent.getSuitablePeople());
		intentTerms.addAll(extractTerms(query));

		Set<String> docTerms = new LinkedHashSet<>();
		docTerms.addAll(doc.getTags() == null ? List.of() : doc.getTags());
		docTerms.addAll(doc.getSuitableScenes() == null ? List.of() : doc.getSuitableScenes());
		docTerms.addAll(doc.getSuitablePeople() == null ? List.of() : doc.getSuitablePeople());
		docTerms.addAll(extractTerms(doc.getEmbeddingText()));
		docTerms.addAll(extractTerms(doc.getLlmSummary()));
		docTerms.addAll(extractTerms(doc.getFlavorDescription()));

		if (intentTerms.isEmpty() || docTerms.isEmpty()) {
			return 0D;
		}

		int hit = 0;
		for (String term : intentTerms) {
			if (docTerms.contains(term)) {
				hit++;
			}
		}
		return Math.min(3.5D, hit * 0.45D);
	}

	/**
	 * 业务分：库存与价格带来的可售性权重。
	 */
	private double computeBusinessScore(HybridDishCandidateDTO candidate) {
		double score = 0D;
		if (candidate.getStock() != null) {
			score += Math.min(1.2D, candidate.getStock() / 30.0D);
		}
		if (candidate.getPrice() != null) {
			double price = candidate.getPrice().doubleValue();
			score += price <= 20D ? 0.8D : price <= 35D ? 0.4D : 0D;
		}
		return score;
	}

	private List<String> buildTags(DishKnowledgeDoc doc) {
		if (doc == null || doc.getTags() == null) {
			return List.of();
		}
		return doc.getTags().stream().filter(StringUtils::hasText).limit(5).collect(Collectors.toList());
	}

	/**
	 * 生成给用户展示的推荐理由。
	 */
	private String buildReason(DishKnowledgeDoc doc) {
		if (doc == null) {
			return "综合结构化条件与语义偏好匹配";
		}
		if (StringUtils.hasText(doc.getRecommendationReason())) {
			return doc.getRecommendationReason();
		}
		if (StringUtils.hasText(doc.getLlmSummary())) {
			return doc.getLlmSummary();
		}
		return "综合结构化条件与语义偏好匹配";
	}

	/**
	 * 生成知识证据文本，便于调试与可解释展示。
	 */
	private String buildEvidence(HybridDishCandidateDTO candidate, double semanticScore) {
		DishKnowledgeDoc doc = candidate.getKnowledgeDoc();
		String evidence = doc == null ? null : (StringUtils.hasText(doc.getLlmSummary()) ? doc.getLlmSummary() : doc.getEmbeddingText());
		if (!StringUtils.hasText(evidence)) {
			evidence = candidate.getDishName() + " 命中结构化筛选";
		}
		return candidate.getDishName() + "（semantic=" + String.format(Locale.ROOT, "%.2f", semanticScore) + "）: " + evidence;
	}

	/**
	 * 构建最终摘要文本。
	 */
	private String buildSummary(IntentMode mode, List<RecommendationItem> recommendations, List<String> evidence) {
		StringBuilder builder = new StringBuilder();
		builder.append(mode == IntentMode.RAG ? "已完成语义+结构融合推荐" : "已完成结构化+语义混合推荐");
		builder.append("，共推荐 ").append(recommendations.size()).append(" 道菜。\n");
		for (int i = 0; i < recommendations.size(); i++) {
			RecommendationItem item = recommendations.get(i);
			builder.append(i + 1).append(". ").append(item.getDishName())
				.append("（").append(item.getPrice()).append("元） - ")
				.append(item.getReason()).append("\n");
		}
		return builder.toString().trim();
	}

	private List<String> extractTerms(String text) {
		if (!StringUtils.hasText(text)) {
			return List.of();
		}
		String cleaned = text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", " ");
		List<String> terms = new ArrayList<>();
		for (String token : cleaned.split("\\s+")) {
			if (token.length() >= 2) {
				terms.add(token.toLowerCase(Locale.ROOT));
			}
		}
		return terms;
	}

	private boolean overlap(List<String> a, List<String> b) {
		if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
			return false;
		}
		Set<String> set = b.stream().filter(StringUtils::hasText).map(String::trim).collect(Collectors.toSet());
		return a.stream().filter(StringUtils::hasText).map(String::trim).anyMatch(set::contains);
	}

	private String empty(String value) {
		return value == null ? null : value.trim();
	}

	/**
	 * 混合推荐输出对象。
	 */
	public record HybridOutput(List<RecommendationItem> recommendations, List<String> knowledgeEvidence, String summary,
			IntentResult intent, String decisionPath) {
	}

	private record ScoredCandidate(HybridDishCandidateDTO candidate, RecommendationItem recommendation, double structuredScore,
			double semanticScore, double businessScore, double fusionScore) {
	}

}
