package com.luan.takeaway.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.ai.api.dto.RecommendationItem;
import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.ai.model.IntentMode;
import com.luan.takeaway.ai.model.IntentResult;
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
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiIntentRecognizer {

	private final AiAssistantProperties properties;

	private final ObjectMapper objectMapper;

	private final RestTemplateBuilder restTemplateBuilder;

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

	public IntentResult extractStructuredIntent(String query, IntentMode mode, String extraContext) throws Exception {
		String context = extraContext == null || extraContext.isBlank() ? "" : "\n补充上下文: " + extraContext;
		String systemPrompt = "你是外卖点餐结构化条件提取器。"
				+ "请把用户需求提取为可查询条件，只输出 JSON，不要其它文字。"
				+ "JSON 字段: mode(TOOL_CALLING|RAG), category, priceMax, spicy, people, preferLight, keywords(array)。"
				+ "字段缺失时用 null，keywords 允许为空数组。"
				+ "mode 固定输出 " + mode.name() + "。"
				+ "禁止输出思考过程、解释或 Markdown 代码块。";
		String content = chatCompletion("structured-intent-" + mode.name().toLowerCase(Locale.ROOT), systemPrompt,
				query + context, 0.1, 256);
		log.debug("[LLM][structured-intent][output] mode={} content={}", mode, safeLog(content));
		return parseIntent(extractJsonObject(content), query, mode);
	}

	public String generateRagAdvice(String query, List<String> knowledgeEvidence) throws Exception {
		StringJoiner joiner = new StringJoiner("\n");
		for (int i = 0; i < knowledgeEvidence.size(); i++) {
			joiner.add((i + 1) + ". " + knowledgeEvidence.get(i));
		}

		String systemPrompt = "你是外卖饮食建议助手。先结合知识证据给出简短可执行建议，"
				+ "输出控制在120字内，不能虚构知识，不要输出 JSON。";
		String userPrompt = "用户问题: " + query + "\n知识证据:\n" + joiner;
		String content = chatCompletion("rag-advice", systemPrompt, userPrompt, 0.2, 256);
		log.debug("[LLM][rag-advice][output] content={}", safeLog(content));
		return content.trim();
	}

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

		return result;
	}

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

	private String cleanJson(String raw) {
		String value = raw.trim();
		if (value.startsWith("```") && value.endsWith("```")) {
			int firstLine = value.indexOf('\n');
			value = firstLine > 0 ? value.substring(firstLine + 1, value.length() - 3) : value;
		}
		return value.trim();
	}

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