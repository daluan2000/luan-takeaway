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
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OpenAI 兼容接口识别器。
 * <p>
 * 负责和大模型交互，核心能力：
 * <ul>
 *   <li>模式决策（辅助标注，非路由决定）</li>
 *   <li>Query Understanding：结构化意图 + 语义意图统一解析</li>
 *   <li>RAG 建议生成</li>
 *   <li>候选重排</li>
 *   <li>菜品知识文档生成</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiIntentRecognizer {

	private final AiAssistantProperties properties;

	private final ObjectMapper objectMapper;

	private final RestTemplateBuilder restTemplateBuilder;

	// ==================== 公开方法 ====================

	/**
	 * 判定路由模式标签（TOOL_CALLING / RAG）。
	 * <p>
	 * 辅助下游做参考解释，非路由决定。
	 * 当用户有明确可筛选条件（品类/预算/辣度等）优先 TOOL_CALLING，
	 * 当用户是模糊健康或场景诉求（如胃不舒服/上火）优先 RAG。
	 */
	public IntentMode decideMode(String query) throws Exception {
		String systemPrompt = "你是外卖点餐请求路由器。请判断用户请求应走 TOOL_CALLING 还是 RAG。"
				+ "当用户有明确可筛选条件(预算/辣度/人数/品类等)优先 TOOL_CALLING；"
				+ "当用户是模糊健康或场景诉求(如胃不舒服/上火/不想太油)优先 RAG。"
				+ "禁止输出思考过程、解释或多余文本，只输出 JSON: {\"mode\":\"TOOL_CALLING\"} 或 {\"mode\":\"RAG\"}";
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
	 * Query Understanding 主入口：统一解析结构化意图 + 语义意图。
	 * <p>
	 * 由 LLM 完成两步解析：
	 * <ol>
	 *   <li>识别请求模式标签（TOOL_CALLING / RAG），用于意图提取与结果解释辅助。</li>
	 *   <li>提取结构化意图（如类别、预算、辣度、营养区间、场景标签等）和语义意图。</li>
	 * </ol>
	 * <p>
	 * 约束模型仅输出 JSON，避免自然语言污染解析流程。
	 *
	 * @param query 用户自然语言输入
	 * @return 解析后的结构化 + 语义意图
	 */
	public IntentResult parseQueryUnderstanding(String query) throws Exception {
		String systemPrompt = buildQueryUnderstandingSystemPrompt();
		String content = chatCompletion("query-understanding", systemPrompt, query, 0.1, 1024);
		log.debug("[LLM][query-understanding][output] content={}", safeLog(content));
		return parseIntentFromQueryUnderstanding(extractJsonObject(content), query);
	}

	/**
	 * 生成菜品知识文档。
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
				+ "只输出 JSON，格式: {\"selected\":[{\"dishId\":123,\"reason\":\"...\"}]}。"
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

	// ==================== 内部解析方法 ====================

	private String buildQueryUnderstandingSystemPrompt() {
		return "你是外卖点餐 Query Understanding 解析器。"
				+ "请把用户需求同时提取为结构化约束（硬过滤条件）和语义偏好（软约束），只输出 JSON，不要其它文字。"
				+ "\n【结构化约束字段 - 硬过滤条件】"
				+ "\n- mode: 模式标签(TOOL_CALLING|RAG)，用于解释参考，非路由决定"
				+ "\n- category: 菜品类别，如 面/粥/米饭/火锅/饮品/小吃/甜点"
				+ "\n- priceMax: 预算上限（数字）"
				+ "\n- spicy: 是否辣(true/false)"
				+ "\n- spicyLevel: 辣度等级(0-5)"
				+ "\n- lightTaste: 是否清淡(true/false)"
				+ "\n- oily: 是否油腻(true/false)"
				+ "\n- soupBased: 是否汤类(true/false)"
				+ "\n- vegetarian: 是否素食(true/false)"
				+ "\n- caloriesMin/caloriesMax: 热量范围"
				+ "\n- proteinMin/proteinMax: 蛋白质范围"
				+ "\n- fatMin/fatMax: 脂肪范围"
				+ "\n- carbohydrateMin/carbohydrateMax: 碳水范围"
				+ "\n- mealTime: 餐段数组，如 [\"lunch\",\"dinner\"]，可选 breakfast/lunch/dinner/midnight"
				+ "\n- portionSize: 分量(small/medium/large)"
				+ "\n- people: 就餐人数"
				+ "\n【语义偏好字段 - 软约束，用于语义相似度计算】"
				+ "\n- keywords: 语义关键词数组，如 [\"清淡\",\"易消化\"]"
				+ "\n- tags: 语义标签数组，可选 低脂/高蛋白/高碳水/高纤维/清淡/重口味/暖胃/解腻/低糖/高热量/易消化/饱腹感强"
				+ "\n- suitableScenes: 适合场景数组，可选 减脂/健身恢复/胃不舒服/工作午餐/夜宵/两人分享/聚餐/快速解决一餐/补充能量/天气寒冷"
				+ "\n- avoidScenes: 避免场景数组，可选 睡前/空腹/肠胃敏感/上火/减脂期间/运动前"
				+ "\n- suitablePeople: 适合人群数组，可选 学生/办公室/健身/老人/儿童/熬夜人群/重体力劳动"
				+ "\n- queryRewrite: 查询改写，把用户口语化表达改写为标准推荐意图描述"
				+ "\n【示例】"
				+ "\n用户输入: \"今天有点上火，想吃清淡、好消化的\""
				+ "\n解析结果: {\"mode\":\"RAG\",\"tags\":[\"清淡\",\"易消化\"],\"avoidScenes\":[\"上火\"],\"lightTaste\":true,\"spicy\":false,\"queryRewrite\":\"清淡易消化，适合上火期间食用\"}"
				+ "\n【规则】"
				+ "\n- 字段缺失时用 null，数组字段没有值时返回空数组 []"
				+ "\n- 禁止输出思考过程、解释或 Markdown 代码块"
				+ "\n- 只输出完整 JSON 对象";
	}

	private IntentResult parseIntentFromQueryUnderstanding(String json, String query) throws Exception {
		JsonNode parsed = objectMapper.readTree(json);
		IntentResult result = new IntentResult();
		result.setOriginalQuery(query);

		// 模式标签
		String modeRaw = parsed.path("mode").asText("TOOL_CALLING");
		result.setMode("RAG".equalsIgnoreCase(modeRaw) ? IntentMode.RAG : IntentMode.TOOL_CALLING);

		// 结构化约束
		result.setCategory(normalizeCategory(asNullable(parsed.path("category").asText(null))));
		if (parsed.has("priceMax") && !parsed.get("priceMax").isNull()) {
			result.setPriceMax(new BigDecimal(parsed.get("priceMax").asText()));
		}
		if (parsed.has("spicy") && !parsed.get("spicy").isNull()) {
			result.setSpicy(parsed.get("spicy").asBoolean());
		}
		if (parsed.has("spicyLevel") && !parsed.get("spicyLevel").isNull()) {
			result.setSpicyLevel(parsed.get("spicyLevel").asInt());
		}
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
		if (parsed.has("people") && !parsed.get("people").isNull()) {
			result.setPeople(parsed.get("people").asInt());
		}

		// 语义偏好
		if (parsed.has("keywords") && parsed.get("keywords").isArray()) {
			for (JsonNode keyword : parsed.get("keywords")) {
				String value = asNullable(keyword.asText(null));
				if (value != null) {
					result.getKeywords().add(value);
				}
			}
		}
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

	// ==================== LLM 调用封装 ====================

	/**
	 * 封装 chat/completions 调用。
	 */
	private String chatCompletion(String stage, String systemPrompt, String userPrompt, double temperature,
			int maxTokens) throws Exception {
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

}
