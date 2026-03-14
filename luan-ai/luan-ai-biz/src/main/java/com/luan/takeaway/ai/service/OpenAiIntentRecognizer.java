package com.luan.takeaway.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.ai.model.IntentMode;
import com.luan.takeaway.ai.model.IntentResult;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiIntentRecognizer {

	private final AiAssistantProperties properties;

	private final ObjectMapper objectMapper;

	private final RestTemplateBuilder restTemplateBuilder;

	public IntentResult recognize(String query) throws Exception {
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
		payload.put("temperature", 0.1);
		payload.put("messages", List.of(
				Map.of("role", "system", "content",
						"你是外卖点餐意图解析器。只输出 JSON，不要输出其它文字。JSON 字段: mode(TOOL_CALLING|RAG), category, priceMax, spicy, people, preferLight, keywords(array)."),
				Map.of("role", "user", "content", query)));

		String endpoint = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
		ResponseEntity<String> response = restTemplate.postForEntity(endpoint, new HttpEntity<>(payload, headers),
				String.class);
		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			return null;
		}

		JsonNode root = objectMapper.readTree(response.getBody());
		JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
		if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
			return null;
		}
		String json = cleanJson(contentNode.asText());
		JsonNode parsed = objectMapper.readTree(json);

		IntentResult result = new IntentResult();
		result.setOriginalQuery(query);
		String modeRaw = parsed.path("mode").asText("TOOL_CALLING");
		result.setMode("RAG".equalsIgnoreCase(modeRaw) ? IntentMode.RAG : IntentMode.TOOL_CALLING);
		result.setCategory(asNullable(parsed.path("category").asText(null)));
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

	private String cleanJson(String raw) {
		String value = raw.trim();
		if (value.startsWith("```") && value.endsWith("```")) {
			int firstLine = value.indexOf('\n');
			value = firstLine > 0 ? value.substring(firstLine + 1, value.length() - 3) : value;
		}
		return value.trim();
	}

	private String asNullable(String value) {
		if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
			return null;
		}
		return value;
	}

}