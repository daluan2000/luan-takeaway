package com.luan.takeaway.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.ai.api.dto.RecommendationItem;
import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.ai.model.IntentMode;
import com.luan.takeaway.ai.model.IntentResult;
import com.luan.takeaway.takeaway.common.constant.DishBusinessDict;
import com.luan.takeaway.takeaway.common.constant.DishSemanticDict;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeGenerateEvent;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * OpenAI 兼容接口识别器。
 * <p>
 * 负责和大模型交互：模式决策、结构化意图提取、RAG 建议生成、候选重排、
 * 菜品知识文档生成等能力都在这里统一落地。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiIntentRecognizer {

	private final AiAssistantProperties properties;

	private final ObjectMapper objectMapper;

	private final RestTemplateBuilder restTemplateBuilder;

	/**
	 * 判定路由模式（TOOL_CALLING / RAG）。
	 */
	public IntentMode decideMode(String query) throws Exception {
		String systemPrompt = "你是外卖点餐请求路由器。请判断用户请求应走 TOOL_CALLING 还是 RAG。"
				+ "当用户有明确可筛选条件(预算/辣度/人数/品类等)优先 TOOL_CALLING；"
				+ "当用户是模糊健康或场景诉求(如胃不舒服/上火/不想太油)优先 RAG。"
				+ "禁止输出思考过程、解释或多余文本，只输出 JSON: {\\\"mode\\\":\\\"TOOL_CALLING\\\"} 或 {\\\"mode\\\":\\\"RAG\\\"}";
		String content = chatCompletion("mode-decision", systemPrompt, query, 0.0, 128);
		log.debug("[LLM][mode-decision][output] content={}", safeLog(content));

		String normalized = content.trim().toUpperCase(Locale.ROOT);
		if ("RAG".equals(normalized)) {
			return IntentMode.RAG;
		}
		if ("TOOL_CALLING".equals(normalized)) {
			return IntentMode.TOOL_CALLING;
		}

		JsonNode parsed = objectMapper.readTree(extractJsonObject(content));
		String modeRaw = parsed.path("mode").asText("TOOL_CALLING");
		return "RAG".equalsIgnoreCase(modeRaw) ? IntentMode.RAG : IntentMode.TOOL_CALLING;
	}

	/**
	 * 提取结构化意图。
	 * <p>
	 * 约束模型仅输出 JSON，避免自然语言污染解析流程。
	 */
	public IntentResult extractStructuredIntent(String query, IntentMode mode, String extraContext) throws Exception {
		String context = extraContext == null || extraContext.isBlank() ? "" : "\n补充上下文: " + extraContext;
		String systemPrompt = "你是外卖点餐结构化条件提取器。"
				+ "请把用户需求提取为可查询条件，只输出 JSON，不要其它文字。"
				+ "JSON 字段: mode(TOOL_CALLING|RAG), category, priceMax, spicy, spicyLevel, people, preferLight,"
				+ "lightTaste, oily, soupBased, vegetarian, caloriesMin, caloriesMax, proteinMin, proteinMax,"
				+ "fatMin, fatMax, carbohydrateMin, carbohydrateMax, mealTime(array), portionSize,"
				+ "keywords(array), tags(array), suitableScenes(array), avoidScenes(array), suitablePeople(array), queryRewrite。"
				+ "字段缺失时用 null，数组字段没有值时返回空数组。"
				+ "mode 固定输出 " + mode.name() + "。"
				+ "禁止输出思考过程、解释或 Markdown 代码块。";
		String content = chatCompletion("structured-intent-" + mode.name().toLowerCase(Locale.ROOT), systemPrompt,
				query + context, 0.1, 256);
		log.debug("[LLM][structured-intent][output] mode={} content={}", mode, safeLog(content));
		return parseIntent(extractJsonObject(content), query, mode);
	}

	/**
	 * 生成菜品知识文档。
	 * <p>
	 * 这里对 category/tags/scene 等字段做了“字典约束提示”，降低模型越界概率。
	 */
	public DishKnowledgeDoc generateDishKnowledgeDoc(DishKnowledgeGenerateEvent event) throws Exception {
		String systemPrompt = "你是菜品知识结构化生成器。根据菜品基础信息生成 DishKnowledgeDoc。"
				+ "必须严格输出 JSON，不要输出解释。"
				+ "category 仅能从 " + DishBusinessDict.CATEGORY + " 中选；"
				+ "mealTime 仅能从 " + DishBusinessDict.MEAL_TIME + " 中选；"
				+ "portionSize 仅能从 " + DishBusinessDict.PORTION_SIZE + " 中选；"
				+ "spicyLevel 仅能从 " + DishBusinessDict.SPICY_LEVEL + " 中选；"
				+ "tags 仅能从 " + DishSemanticDict.TAGS + " 中选；"
				+ "suitableScenes 仅能从 " + DishSemanticDict.SUITABLE_SCENES + " 中选；"
				+ "avoidScenes 仅能从 " + DishSemanticDict.AVOID_SCENES + " 中选；"
				+ "suitablePeople 仅能从 " + DishSemanticDict.SUITABLE_PEOPLE + " 中选。"
				+ "需要输出字段: category, spicy, spicyLevel, lightTaste, oily, soupBased, vegetarian, calories, protein, fat, carbohydrate, mealTime, portionSize, tags, suitableScenes, avoidScenes, suitablePeople, embeddingText, flavorDescription, llmSummary, recommendationReason。";
		String userPrompt = "菜品名称: " + event.getDishName()
				+ "\n菜品描述: " + event.getDishDesc()
				+ "\n价格: " + event.getPrice();

		String content = chatCompletion("knowledge-generate", systemPrompt, userPrompt, 0.2, 768);
		JsonNode node = objectMapper.readTree(extractJsonObject(content));

		DishKnowledgeDoc doc = new DishKnowledgeDoc();
		doc.setDishId(event.getDishId());
		doc.setCategory(asNullable(node.path("category").asText(null)));
		if (node.has("spicy") && !node.get("spicy").isNull()) {
			doc.setSpicy(node.get("spicy").asBoolean());
		}
		if (node.has("spicyLevel") && !node.get("spicyLevel").isNull()) {
			doc.setSpicyLevel(node.get("spicyLevel").asInt());
		}
		if (node.has("lightTaste") && !node.get("lightTaste").isNull()) {
			doc.setLightTaste(node.get("lightTaste").asBoolean());
		}
		if (node.has("oily") && !node.get("oily").isNull()) {
			doc.setOily(node.get("oily").asBoolean());
		}
		if (node.has("soupBased") && !node.get("soupBased").isNull()) {
			doc.setSoupBased(node.get("soupBased").asBoolean());
		}
		if (node.has("vegetarian") && !node.get("vegetarian").isNull()) {
			doc.setVegetarian(node.get("vegetarian").asBoolean());
		}
		if (node.has("calories") && !node.get("calories").isNull()) {
			doc.setCalories(node.get("calories").asInt());
		}
		if (node.has("protein") && !node.get("protein").isNull()) {
			doc.setProtein(node.get("protein").asInt());
		}
		if (node.has("fat") && !node.get("fat").isNull()) {
			doc.setFat(node.get("fat").asInt());
		}
		if (node.has("carbohydrate") && !node.get("carbohydrate").isNull()) {
			doc.setCarbohydrate(node.get("carbohydrate").asInt());
		}
		doc.setMealTime(readStringArray(node.path("mealTime")));
		doc.setPortionSize(asNullable(node.path("portionSize").asText(null)));
		doc.setTags(readStringArray(node.path("tags")));
		doc.setSuitableScenes(readStringArray(node.path("suitableScenes")));
		doc.setAvoidScenes(readStringArray(node.path("avoidScenes")));
		doc.setSuitablePeople(readStringArray(node.path("suitablePeople")));
		doc.setEmbeddingText(asNullable(node.path("embeddingText").asText(null)));
		doc.setFlavorDescription(asNullable(node.path("flavorDescription").asText(null)));
		doc.setLlmSummary(asNullable(node.path("llmSummary").asText(null)));
		doc.setRecommendationReason(asNullable(node.path("recommendationReason").asText(null)));
		return doc;
	}

	/**
	 * 对候选推荐做二次筛选与重排。
	 * <p>
	 * 仅允许从候选列表中选择，不允许模型凭空新增菜品。
	 */
	public List<RecommendationItem> rerankRecommendations(IntentResult intent, List<RecommendationItem> candidates,
			int limit) throws Exception {
		if (candidates == null || candidates.isEmpty()) {
			return List.of();
		}

		String userQuery = intent == null || intent.getOriginalQuery() == null ? "" : intent.getOriginalQuery();
		String structured = intent == null ? "{}" : objectMapper.writeValueAsString(intent);
		// Map.of does not allow null values; use mutable maps to preserve nullable fields in candidate payload.
		String candidateJson = objectMapper.writeValueAsString(candidates.stream().map(item -> {
			Map<String, Object> candidate = new LinkedHashMap<>();
			candidate.put("dishId", item.getDishId());
			candidate.put("dishName", item.getDishName());
			candidate.put("dishDesc", item.getDishDesc());
			candidate.put("price", item.getPrice());
			candidate.put("tags", item.getTags());
			candidate.put("reason", item.getReason());
			candidate.put("score", item.getScore());
			return candidate;
		}).collect(Collectors.toList()));

		String systemPrompt = "你是外卖推荐重排器。你只能从候选菜品中挑选最优结果，不能新增菜品。"
				+ "请结合用户原始需求和结构化意图，对候选做最终筛选和排序。"
				+ "只输出 JSON，格式: {\\\"selected\\\":[{\\\"dishId\\\":123,\\\"reason\\\":\\\"...\\\"}]}。"
				+ "selected 最多返回 " + limit + " 个，dishId 必须来自候选列表。";
		String userPrompt = "用户原始需求: " + userQuery + "\n结构化意图: " + structured + "\n候选菜品JSON: " + candidateJson;
		log.debug("[LLM][post-filter-rerank][input] limit={} userQuery={} structuredIntent={} candidates={}", limit,
				safeLog(userQuery), safeLog(structured), safeLog(candidateJson));

		String content = chatCompletion("post-filter-rerank", systemPrompt, userPrompt, 0.1, 512);
		log.debug("[LLM][post-filter-rerank][output] content={}", safeLog(content));

		JsonNode parsed = objectMapper.readTree(extractJsonObject(content));
		JsonNode selected = parsed.path("selected");
		log.debug("[LLM][post-filter-rerank][output-parsed] selected={}", safeLog(selected.toString()));
		if (!selected.isArray() || selected.isEmpty()) {
			throw new IllegalStateException("LLM 二次筛选未返回有效 selected 列表");
		}

		Map<Long, RecommendationItem> candidateMap = new LinkedHashMap<>();
		for (RecommendationItem candidate : candidates) {
			if (candidate.getDishId() != null) {
				candidateMap.put(candidate.getDishId(), candidate);
			}
		}

		List<RecommendationItem> reranked = new java.util.ArrayList<>();
		for (JsonNode node : selected) {
			Long dishId = parseDishId(node.path("dishId"));
			if (dishId == null || !candidateMap.containsKey(dishId)) {
				continue;
			}
			RecommendationItem pick = candidateMap.get(dishId);
			String llmReason = asNullable(node.path("reason").asText(null));
			if (llmReason != null) {
				pick.setReason(llmReason);
			}
			reranked.add(pick);
			if (reranked.size() >= limit) {
				break;
			}
		}

		if (reranked.isEmpty()) {
			throw new IllegalStateException("LLM 二次筛选结果为空或 dishId 无效");
		}
		return reranked;
	}

	/**
	 * 兼容 dishId 为数字或纯数字字符串两种返回形态。
	 */
	private Long parseDishId(JsonNode dishIdNode) {
		if (dishIdNode == null || dishIdNode.isMissingNode() || dishIdNode.isNull()) {
			return null;
		}
		if (dishIdNode.isIntegralNumber()) {
			return dishIdNode.asLong();
		}

		String dishIdText = asNullable(dishIdNode.asText(null));
		if (dishIdText == null || !dishIdText.matches("\\d+")) {
			return null;
		}

		try {
			return Long.parseLong(dishIdText);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	/**
	 * 封装 chat/completions 调用。
	 * <p>
	 * 支持 local/remote 双源配置，并统一做超时、日志、返回结构解析。
	 */
	private String chatCompletion(String stage, String systemPrompt, String userPrompt, double temperature,
			int maxTokens)
			throws Exception {
		AiAssistantProperties.Llm llm = properties.getLlm();
		String baseUrl = llm.resolveBaseUrl();
		String apiKey = llm.resolveApiKey();
		String model = llm.resolveModel();
		if (baseUrl == null || baseUrl.isBlank() || model == null || model.isBlank()) {
			throw new IllegalStateException("LLM 配置不完整，请检查 ai.assistant.llm 的 source/base-url/model 配置");
		}

		RestTemplate restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofMillis(llm.getTimeoutMs()))
				.setReadTimeout(Duration.ofMillis(llm.getTimeoutMs()))
				.build();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (apiKey != null && !apiKey.isBlank()) {
			headers.setBearerAuth(apiKey);
		}

		Map<String, Object> payload = new HashMap<>();
		payload.put("model", model);
		payload.put("temperature", temperature);
		payload.put("max_tokens", maxTokens);
		if ("local".equalsIgnoreCase(llm.getSource())) {
			// For local vLLM + Qwen, disable reasoning/thinking output so parser receives final JSON content directly.
			payload.put("chat_template_kwargs", Map.of("enable_thinking", false));
		}
		payload.put("messages", List.of(
				Map.of("role", "system", "content", systemPrompt),
				Map.of("role", "user", "content", userPrompt)));

		String endpoint = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
		log.debug("[LLM][{}][input] endpoint={} model={} temperature={} maxTokens={} systemPrompt={} userPrompt={}",
				stage, endpoint, model, temperature, maxTokens, safeLog(systemPrompt), safeLog(userPrompt));

		ResponseEntity<String> response = restTemplate.postForEntity(endpoint, new HttpEntity<>(payload, headers),
				String.class);
		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			log.debug("[LLM][{}][raw-response] status={} body=<empty>", stage, response.getStatusCode());
			throw new IllegalStateException("LLM 调用失败，HTTP状态=" + response.getStatusCode());
		}

		log.debug("[LLM][{}][raw-response] status={} body={}", stage, response.getStatusCode(),
				safeLog(response.getBody()));

		JsonNode root = objectMapper.readTree(response.getBody());
		JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
		if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
			log.debug("[LLM][{}][parsed-content] <empty>", stage);
			throw new IllegalStateException("LLM 返回内容为空，stage=" + stage);
		}
		return contentNode.asText();
	}

	/**
	 * 把模型 JSON 解析成内部意图对象。
	 */
	private IntentResult parseIntent(String json, String query, IntentMode fallbackMode) throws Exception {
		JsonNode parsed = objectMapper.readTree(json);
		IntentResult result = new IntentResult();
		result.setOriginalQuery(query);
		String modeRaw = parsed.path("mode").asText(fallbackMode == null ? "TOOL_CALLING" : fallbackMode.name());
		result.setMode("RAG".equalsIgnoreCase(modeRaw) ? IntentMode.RAG : IntentMode.TOOL_CALLING);
		result.setCategory(normalizeCategory(asNullable(parsed.path("category").asText(null))));
		if (parsed.hasNonNull("priceMax")) {
			result.setPriceMax(new BigDecimal(parsed.get("priceMax").asText()));
		}
		if (parsed.has("spicy") && !parsed.get("spicy").isNull()) {
			result.setSpicy(parsed.get("spicy").asBoolean());
		}
		if (parsed.has("people") && !parsed.get("people").isNull()) {
			result.setPeople(parsed.get("people").asInt());
		}
		if (parsed.has("preferLight") && !parsed.get("preferLight").isNull()) {
			result.setPreferLight(parsed.get("preferLight").asBoolean());
		}
		if (parsed.has("keywords") && parsed.get("keywords").isArray()) {
			for (JsonNode keyword : parsed.get("keywords")) {
				String value = keyword.asText();
				if (!value.isBlank()) {
					result.getKeywords().add(value);
				}
			}
		}

		result.setSpicyLevel(parseIntField(parsed, "spicyLevel"));
		if (parsed.has("lightTaste") && !parsed.get("lightTaste").isNull()) {
			result.setLightTaste(parsed.get("lightTaste").asBoolean());
		}
		if (parsed.has("oily") && !parsed.get("oily").isNull()) {
			result.setOily(parsed.get("oily").asBoolean());
		}
		if (parsed.has("soupBased") && !parsed.get("soupBased").isNull()) {
			result.setSoupBased(parsed.get("soupBased").asBoolean());
		}
		if (parsed.has("vegetarian") && !parsed.get("vegetarian").isNull()) {
			result.setVegetarian(parsed.get("vegetarian").asBoolean());
		}
		result.setCaloriesMin(parseIntField(parsed, "caloriesMin"));
		result.setCaloriesMax(parseIntField(parsed, "caloriesMax"));
		result.setProteinMin(parseIntField(parsed, "proteinMin"));
		result.setProteinMax(parseIntField(parsed, "proteinMax"));
		result.setFatMin(parseIntField(parsed, "fatMin"));
		result.setFatMax(parseIntField(parsed, "fatMax"));
		result.setCarbohydrateMin(parseIntField(parsed, "carbohydrateMin"));
		result.setCarbohydrateMax(parseIntField(parsed, "carbohydrateMax"));
		result.setMealTime(readStringArray(parsed.path("mealTime")));
		result.setPortionSize(asNullable(parsed.path("portionSize").asText(null)));
		result.setTags(readStringArray(parsed.path("tags")));
		result.setSuitableScenes(readStringArray(parsed.path("suitableScenes")));
		result.setAvoidScenes(readStringArray(parsed.path("avoidScenes")));
		result.setSuitablePeople(readStringArray(parsed.path("suitablePeople")));
		result.setQueryRewrite(asNullable(parsed.path("queryRewrite").asText(null)));

		return result;
	}

	private Integer parseIntField(JsonNode root, String fieldName) {
		if (!root.has(fieldName) || root.get(fieldName).isNull()) {
			return null;
		}
		return root.get(fieldName).asInt();
	}

	private List<String> readStringArray(JsonNode node) {
		if (node == null || !node.isArray()) {
			return List.of();
		}
		List<String> values = new java.util.ArrayList<>();
		for (JsonNode item : node) {
			String value = asNullable(item.asText(null));
			if (value != null) {
				values.add(value);
			}
		}
		return values;
	}

	/**
	 * 类目标准化：处理英文别名、过滤不可识别英文类目。
	 */
	private String normalizeCategory(String rawCategory) {
		if (rawCategory == null) {
			return null;
		}
		String value = rawCategory.trim();
		if (value.isEmpty()) {
			return null;
		}

		String lower = value.toLowerCase(Locale.ROOT);
		Map<String, String> alias = Map.ofEntries(
				Map.entry("light", "轻食"),
				Map.entry("salad", "沙拉"),
				Map.entry("porridge", "粥"),
				Map.entry("congee", "粥"),
				Map.entry("soup", "汤"),
				Map.entry("noodle", "面"),
				Map.entry("noodles", "面"),
				Map.entry("rice", "米饭"),
				Map.entry("hotpot", "火锅"),
				Map.entry("spicy", "辣")
		);
		if (alias.containsKey(lower)) {
			String mapped = alias.get(lower);
			log.debug("[LLM][category-normalize] raw={} mapped={}", rawCategory, mapped);
			return mapped;
		}

		// Unknown pure English categories are dropped to avoid polluting dish retrieval.
		if (lower.matches("[a-z][a-z0-9_\\-\\s]*")) {
			log.debug("[LLM][category-normalize] drop unknown english category={}", rawCategory);
			return null;
		}

		return value;
	}

	/**
	 * 清理模型可能返回的 Markdown 代码块包装。
	 */
	private String cleanJson(String raw) {
		String value = raw.trim();
		if (value.startsWith("```") && value.endsWith("```")) {
			int firstLine = value.indexOf('\n');
			value = firstLine > 0 ? value.substring(firstLine + 1, value.length() - 3) : value;
		}
		return value.trim();
	}

	/**
	 * 从混杂文本中提取完整 JSON 对象。
	 * <p>
	 * 使用括号深度与字符串状态机，避免误截断。
	 */
	private String extractJsonObject(String raw) {
		String value = cleanJson(raw);
		int start = value.indexOf('{');
		if (start < 0) {
			throw new IllegalStateException("LLM 返回内容不是 JSON"
					+ (containsThinkingPrefix(value) ? "（检测到 Thinking 前缀）" : "") + "，raw前300="
					+ previewRaw(value));
		}

		int depth = 0;
		boolean inString = false;
		boolean escaped = false;
		for (int i = start; i < value.length(); i++) {
			char c = value.charAt(i);
			if (escaped) {
				escaped = false;
				continue;
			}
			if (c == '\\') {
				escaped = true;
				continue;
			}
			if (c == '"') {
				inString = !inString;
				continue;
			}
			if (inString) {
				continue;
			}
			if (c == '{') {
				depth++;
			}
			else if (c == '}') {
				depth--;
				if (depth == 0) {
					return value.substring(start, i + 1);
				}
			}
		}

		throw new IllegalStateException("LLM 返回 JSON 不完整，raw前300=" + previewRaw(value));
	}

	/**
	 * 判断是否出现 Thinking 前缀，便于错误定位。
	 */
	private boolean containsThinkingPrefix(String value) {
		String lower = value.toLowerCase(Locale.ROOT);
		return lower.startsWith("thinking") || lower.contains("thinking process");
	}

	private String previewRaw(String value) {
		if (value == null) {
			return "<null>";
		}
		String normalized = value.replace("\n", "\\n").replace("\r", "\\r");
		int limit = 300;
		if (normalized.length() <= limit) {
			return normalized;
		}
		return normalized.substring(0, limit) + "...(truncated)";
	}

	private String asNullable(String value) {
		if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
			return null;
		}
		return value;
	}

	/**
	 * 统一日志安全裁剪，防止过长日志污染输出。
	 */
	private String safeLog(String raw) {
		if (raw == null) {
			return "<null>";
		}
		String normalized = raw.replace("\n", "\\n").replace("\r", "\\r");
		int maxLen = 2000;
		if (normalized.length() <= maxLen) {
			return normalized;
		}
		return normalized.substring(0, maxLen) + "...(truncated)";
	}

}