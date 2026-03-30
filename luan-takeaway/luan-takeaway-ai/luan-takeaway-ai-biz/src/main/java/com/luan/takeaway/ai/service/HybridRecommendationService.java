package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.api.dto.RecommendationItem;
import com.luan.takeaway.ai.model.IntentMode;
import com.luan.takeaway.ai.model.IntentResult;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.call.DishServiceCallFacade;
import com.luan.takeaway.takeaway.common.dto.HybridDishCandidateDTO;
import com.luan.takeaway.takeaway.common.dto.HybridDishSearchRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 混合推荐服务（结构化 + 语义 + 业务信号）。
 *
 * AI推荐核心类，实现文档设计的完整四阶段链路（# 8 AI 点餐助手混合智能架构）：
 *
 * <ol>
 *   <li><b>Query Understanding</b>：由 {@link QueryUnderstandingService} 完成，
 *       LLM 两步解析：模式标签 + 结构化意图 + 语义意图。</li>
 *
 *   <li><b>Hybrid Retrieval</b>：
 *       <ul>
 *         <li>结构化召回：按商家、在售状态、价格上限筛选菜品。</li>
 *         <li>语义召回：{@link SemanticSearchService} 基于 embedding 向量做余弦相似度检索，
 *             选取 top-N 相似度菜品。</li>
 *         <li>结构化过滤：结合 {@link DishKnowledgeDoc} 过滤类别、辣度、清淡/油腻、
 *             营养区间、餐段、分量等条件。</li>
 *       </ul>
 *       </li>
 *
 *   <li><b>Fusion Ranking</b>：对候选集合计算融合分并排序。
 *       <pre>
 *       FusionScore = StructuredScore * 0.45 + SemanticScore * 0.35 + BusinessScore * 0.20
 *       </pre>
 *       </li>
 *
 *   <li><b>LLM Selection and Generation</b>：取规则排序后的前置候选池（Top-N）交给 LLM 二次重排，
 *       若 LLM 失败则回退规则排序结果。</li>
 * </ol>
 *
 * @see QueryUnderstandingService
 * @see SemanticSearchService
 * @see OpenAiIntentRecognizer
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HybridRecommendationService {

	/** 融合分权重：结构化匹配 */
	private static final double WEIGHT_STRUCTURED = 0.45;

	/** 融合分权重：语义相关度 */
	private static final double WEIGHT_SEMANTIC = 0.35;

	/** 融合分权重：业务可售性 */
	private static final double WEIGHT_BUSINESS = 0.20;

	private final QueryUnderstandingService queryUnderstandingService;

	private final SemanticSearchService semanticSearchService;

	private final OpenAiIntentRecognizer openAiIntentRecognizer;

	private final DishServiceCallFacade dishCall;

	/**
	 * 推荐主入口。
	 * <p>
	 * 完整链路：Query Understanding → Hybrid Retrieval → Fusion Ranking → LLM Selection
	 */
	public HybridOutput recommend(String query, Long merchantUserId, int limit) {
		// ========== 阶段1: Query Understanding ==========
		log.info("[HybridRecommend] === 阶段1: Query Understanding ===");
		IntentResult intent = queryUnderstandingService.understand(query);
		IntentMode mode = intent.getMode() != null ? intent.getMode() : IntentMode.TOOL_CALLING;
		if (!StringUtils.hasText(intent.getOriginalQuery())) {
			intent.setOriginalQuery(query);
		}

		// ========== 阶段2: Hybrid Retrieval ==========
		log.info("[HybridRecommend] === 阶段2: Hybrid Retrieval ===");
		HybridRetrievalResult retrievalResult = performHybridRetrieval(intent, merchantUserId);
		List<HybridDishCandidateDTO> candidates = retrievalResult.candidates();

		if (candidates.isEmpty()) {
			log.warn("[HybridRecommend] 候选集为空，返回空结果");
			return new HybridOutput(List.of(), List.of(),
					"当前没有检索到匹配菜品，建议放宽筛选条件重试。",
					intent, mode.name().toLowerCase(Locale.ROOT));
		}

		// ========== 阶段3: Fusion Ranking ==========
		log.info("[HybridRecommend] === 阶段3: Fusion Ranking ===");
		List<ScoredCandidate> scored = scoreAndRank(intent, candidates, retrievalResult.semanticScoreMap());

		if (scored.isEmpty()) {
			return new HybridOutput(List.of(), List.of(),
					"当前没有检索到匹配菜品，建议放宽筛选条件重试。",
					intent, mode.name().toLowerCase(Locale.ROOT));
		}

		// 取排序后的候选池（Top-N，用于 LLM 重排）
		int rerankPoolSize = Math.min(scored.size(), Math.max(limit * 4, 12));
		List<RecommendationItem> rerankPool = scored.subList(0, rerankPoolSize)
				.stream()
				.map(ScoredCandidate::recommendation)
				.collect(Collectors.toList());

		// ========== 阶段4: LLM Selection and Generation ==========
		log.info("[HybridRecommend] === 阶段4: LLM Selection and Generation ===");
		List<RecommendationItem> finalItems;
		try {
			finalItems = openAiIntentRecognizer.rerankRecommendations(intent, rerankPool, limit);
		}
		catch (Exception ex) {
			log.warn("[HybridRecommend] LLM 候选选取失败，回退规则排序: {}", ex.getMessage());
			finalItems = rerankPool.stream().limit(limit).collect(Collectors.toList());
		}

		// 生成知识证据和摘要
		List<String> evidence = scored.stream()
				.limit(3)
				.map(item -> buildEvidence(item.candidate(), item.semanticScore()))
				.collect(Collectors.toList());

		String summary = buildSummary(mode, finalItems, evidence);

		return new HybridOutput(finalItems, evidence, summary, intent, mode.name().toLowerCase(Locale.ROOT));
	}

	/**
	 * 执行 Hybrid Retrieval 两步召回。
	 * <p>
	 * 步骤1：结构化召回（按商家、在售状态、价格上限）。
	 * 步骤2：语义召回（基于 embedding 向量 + 余弦相似度）。
	 * 步骤3：结构化过滤（结合 DishKnowledgeDoc 过滤）。
	 *
	 * @return HybridRetrievalResult 包含候选菜品列表和 dishId -> 语义分的映射
	 */
	private HybridRetrievalResult performHybridRetrieval(IntentResult intent, Long merchantUserId) {
		Set<Long> candidateIds = new LinkedHashSet<>();

		// --- 语义召回（向量检索）---
		// 按文档要求：先语义召回 top-100/200，再结构化过滤
		Map<Long, Double> semanticScoreMap = new java.util.HashMap<>();
		if (intent.hasSemanticConstraints()) {
			List<SemanticSearchService.SemanticMatch> semanticMatches =
					semanticSearchService.searchBySemanticIntent(intent, merchantUserId);
			if (!semanticMatches.isEmpty()) {
				log.debug("[HybridRetrieval] 语义召回命中 {} 条", semanticMatches.size());
				// 记录语义分
				for (SemanticSearchService.SemanticMatch m : semanticMatches) {
					semanticScoreMap.put(m.dishId(), m.similarity());
				}
				// 取语义匹配 top-200 作为候选（文档建议）
				semanticMatches.stream()
						.sorted(Comparator.comparingDouble(SemanticSearchService.SemanticMatch::similarity).reversed())
						.limit(200)
						.forEach(m -> candidateIds.add(m.dishId()));
			}
		}

		// --- 结构化召回（兜底 + 补充）---
		HybridDishSearchRequest structRequest = buildStructuredRequest(intent, merchantUserId);
		R<List<HybridDishCandidateDTO>> structResponse = dishCall.searchHybridCandidates(structRequest);
		List<HybridDishCandidateDTO> structCandidates = structResponse == null || structResponse.getData() == null
				? List.of() : structResponse.getData();

		if (!structCandidates.isEmpty()) {
			log.debug("[HybridRetrieval] 结构化召回命中 {} 条", structCandidates.size());
			// 结构化候选全部加入
			structCandidates.forEach(c -> {
				if (c.getDishId() != null) {
					candidateIds.add(c.getDishId());
				}
			});
		}

		if (candidateIds.isEmpty()) {
			log.debug("[HybridRetrieval] 候选集为空");
			return new HybridRetrievalResult(List.of(), Map.of());
		}

		// --- 合并候选并关联语义分 ---
		// 结构化召回已包含知识文档，直接使用
		List<HybridDishCandidateDTO> result = new ArrayList<>();
		for (HybridDishCandidateDTO c : structCandidates) {
			if (c.getDishId() != null && candidateIds.contains(c.getDishId())) {
				// 关联语义分
				Double semScore = semanticScoreMap.get(c.getDishId());
				if (semScore != null) {
					c.setSemanticScore(semScore);
				}
				result.add(c);
			}
		}

		log.debug("[HybridRetrieval] 最终候选集大小: {}", result.size());
		return new HybridRetrievalResult(result, semanticScoreMap);
	}

	/**
	 * 构建结构化检索请求。
	 */
	private HybridDishSearchRequest buildStructuredRequest(IntentResult intent, Long merchantUserId) {
		HybridDishSearchRequest request = new HybridDishSearchRequest();
		request.setMerchantUserId(merchantUserId);
		request.setLimit(Math.max(200, 300)); // 拉取充足候选

		// 结构化硬约束条件
		request.setCategory(intent.getCategory());
		request.setPriceMax(intent.getPriceMax());
		request.setSpicy(intent.getSpicy());
		request.setSpicyLevel(intent.getSpicyLevel());
		request.setLightTaste(intent.getLightTaste());
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

		return request;
	}

	/**
	 * 对候选菜品打分并排序。
	 * <p>
	 * 融合分 = StructuredScore * 0.45 + SemanticScore * 0.35 + BusinessScore * 0.20
	 *
	 * @param candidates 候选菜品列表
	 * @param semanticScoreMap dishId -> 向量相似度分的映射
	 */
	private List<ScoredCandidate> scoreAndRank(IntentResult intent, List<HybridDishCandidateDTO> candidates,
			Map<Long, Double> semanticScoreMap) {
		List<ScoredCandidate> scored = new ArrayList<>();

		for (HybridDishCandidateDTO candidate : candidates) {
			double structuredScore = computeStructuredScore(intent, candidate);
			double semanticScore = computeSemanticScore(intent, candidate.getKnowledgeDoc());
			double businessScore = computeBusinessScore(candidate);

			// 从候选中获取（已在 performHybridRetrieval 中设置）
			double embeddedScore = candidate.getSemanticScore() != null ? candidate.getSemanticScore() : 0.0;
			// 从向量检索映射中补充获取
			Double mapScore = semanticScoreMap.get(candidate.getDishId());
			if (mapScore != null && mapScore > embeddedScore) {
				embeddedScore = mapScore;
			}

			// 融合分 = 结构化分 * 0.45 + 向量检索分 * 0.35 + 业务分 * 0.20
			double fusionScore = structuredScore * WEIGHT_STRUCTURED
					+ embeddedScore * WEIGHT_SEMANTIC
					+ businessScore * WEIGHT_BUSINESS;

			RecommendationItem recommendation = toRecommendationItem(candidate);
			scored.add(new ScoredCandidate(candidate, recommendation,
					structuredScore, semanticScore, embeddedScore,
					businessScore, fusionScore));
		}

		// 按融合分降序
		scored.sort(Comparator.comparingDouble(ScoredCandidate::fusionScore).reversed());
		return scored;
	}

	/**
	 * 结构化匹配分：类别、价格、辣度、餐段等硬条件命中分。
	 * <p>
	 * 命中强度越高，得分越高。
	 */
	private double computeStructuredScore(IntentResult intent, HybridDishCandidateDTO candidate) {
		double score = 0D;
		DishKnowledgeDoc doc = candidate.getKnowledgeDoc();

		// 类别命中
		if (intent.getCategory() != null && doc != null
				&& intent.getCategory().equalsIgnoreCase(empty(doc.getCategory()))) {
			score += 2.2D;
		}

		// 价格在预算内
		if (intent.getPriceMax() != null && candidate.getPrice() != null
				&& candidate.getPrice().compareTo(intent.getPriceMax()) <= 0) {
			score += 1.8D;
		}

		// 辣度匹配
		if (intent.getSpicy() != null && doc != null && intent.getSpicy().equals(doc.getSpicy())) {
			score += 1.2D;
		}

		// 清淡匹配
		if (intent.getLightTaste() != null && doc != null && intent.getLightTaste().equals(doc.getLightTaste())) {
			score += 1.0D;
		}

		// 餐段匹配
		if (intent.getMealTime() != null && !intent.getMealTime().isEmpty() && doc != null
				&& overlap(intent.getMealTime(), doc.getMealTime())) {
			score += 0.8D;
		}

		return score;
	}

	/**
	 * 语义相关度分：基于意图词与知识文档词的重合程度。
	 * <p>
	 * 命中词数越高，得分越高。
	 */
	private double computeSemanticScore(IntentResult intent, DishKnowledgeDoc doc) {
		if (doc == null) {
			return 0D;
		}

		// 收集意图词
		Set<String> intentTerms = new LinkedHashSet<>();
		intentTerms.addAll(intent.getKeywords() == null ? List.of() : intent.getKeywords());
		intentTerms.addAll(intent.getTags() == null ? List.of() : intent.getTags());
		intentTerms.addAll(intent.getSuitableScenes() == null ? List.of() : intent.getSuitableScenes());
		intentTerms.addAll(intent.getSuitablePeople() == null ? List.of() : intent.getSuitablePeople());
		intentTerms.addAll(intent.getAvoidScenes() == null ? List.of() : intent.getAvoidScenes());

		// 收集文档词（embeddingText + llmSummary + flavorDescription）
		Set<String> docTerms = new LinkedHashSet<>();
		docTerms.addAll(doc.getTags() == null ? List.of() : doc.getTags());
		docTerms.addAll(doc.getSuitableScenes() == null ? List.of() : doc.getSuitableScenes());
		docTerms.addAll(doc.getSuitablePeople() == null ? List.of() : doc.getSuitablePeople());
		docTerms.addAll(doc.getAvoidScenes() == null ? List.of() : doc.getAvoidScenes());
		docTerms.addAll(extractTerms(doc.getEmbeddingText()));
		docTerms.addAll(extractTerms(doc.getLlmSummary()));
		docTerms.addAll(extractTerms(doc.getFlavorDescription()));

		if (intentTerms.isEmpty() || docTerms.isEmpty()) {
			return 0D;
		}

		// 计算词项重合度
		int hit = 0;
		for (String term : intentTerms) {
			if (docTerms.contains(term)) {
				hit++;
			}
			// 部分匹配（如包含关系）
			else {
				for (String docTerm : docTerms) {
					if (docTerm.contains(term) || term.contains(docTerm)) {
						hit++;
						break;
					}
				}
			}
		}

		return Math.min(3.5D, hit * 0.45D);
	}

	/**
	 * 业务可售分：库存充裕 + 价格带平价优先。
	 */
	private double computeBusinessScore(HybridDishCandidateDTO candidate) {
		double score = 0D;

		// 库存充裕加分
		if (candidate.getStock() != null) {
			score += Math.min(1.2D, candidate.getStock() / 30.0D);
		}

		// 价格带平价优先
		if (candidate.getPrice() != null) {
			double price = candidate.getPrice().doubleValue();
			if (price <= 20D) {
				score += 0.8D;
			}
			else if (price <= 35D) {
				score += 0.4D;
			}
		}

		return score;
	}

	/**
	 * 转换为推荐项。
	 */
	private RecommendationItem toRecommendationItem(HybridDishCandidateDTO candidate) {
		RecommendationItem item = new RecommendationItem();
		item.setDishId(candidate.getDishId());
		item.setMerchantUserId(candidate.getMerchantUserId());
		item.setDishName(candidate.getDishName());
		item.setDishDesc(candidate.getDishDesc());
		item.setPrice(candidate.getPrice());
		item.setTags(buildTags(candidate.getKnowledgeDoc()));
		item.setReason(buildReason(candidate.getKnowledgeDoc()));
		return item;
	}

	private List<String> buildTags(DishKnowledgeDoc doc) {
		if (doc == null || doc.getTags() == null) {
			return List.of();
		}
		return doc.getTags().stream().filter(StringUtils::hasText).limit(5).collect(Collectors.toList());
	}

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

	private String buildEvidence(HybridDishCandidateDTO candidate, double semanticScore) {
		DishKnowledgeDoc doc = candidate.getKnowledgeDoc();
		String evidence = doc == null ? null
				: (StringUtils.hasText(doc.getLlmSummary()) ? doc.getLlmSummary() : doc.getEmbeddingText());
		if (!StringUtils.hasText(evidence)) {
			evidence = candidate.getDishName() + " 命中结构化筛选";
		}
		return candidate.getDishName() + "（semantic=" + String.format(Locale.ROOT, "%.2f", semanticScore) + "）: "
				+ evidence;
	}

	private String buildSummary(IntentMode mode, List<RecommendationItem> recommendations, List<String> evidence) {
		String pathLabel = mode == IntentMode.RAG ? "语义+结构融合推荐" : "结构化+语义混合推荐";
		StringBuilder builder = new StringBuilder();
		builder.append("已完成").append(pathLabel).append("，共推荐 ").append(recommendations.size()).append(" 道菜。\n");

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

	// ==================== 内部数据结构 ====================

	/**
	 * 混合推荐输出对象。
	 */
	public record HybridOutput(
			List<RecommendationItem> recommendations,
			List<String> knowledgeEvidence,
			String summary,
			IntentResult intent,
			String decisionPath
	) {
	}

	/**
	 * 带分数的候选菜品。
	 * <p>
	 * fusionScore = structuredScore * 0.45 + embeddedScore * 0.35 + businessScore * 0.20
	 */
	private record ScoredCandidate(
			HybridDishCandidateDTO candidate,
			RecommendationItem recommendation,
			double structuredScore,
			double semanticScore,
			double embeddedScore,
			double businessScore,
			double fusionScore
	) {
	}

	/**
	 * Hybrid Retrieval 阶段结果。
	 */
	private record HybridRetrievalResult(
			List<HybridDishCandidateDTO> candidates,
			Map<Long, Double> semanticScoreMap
	) {
	}

}
