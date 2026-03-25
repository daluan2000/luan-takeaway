package com.luan.takeaway.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.ai.model.IntentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

/**
 * 语义向量检索服务（Hybrid Retrieval 语义召回阶段）。
 * <p>
 * 负责完成文档设计的 Hybrid Retrieval 第二步：
 * <ol>
 *   <li>根据语义推荐意图拼接 embedding text。</li>
 *   <li>调用 embedding 模型生成向量。</li>
 *   <li>在 Redis 中做向量搜索，选取 top-N 相似度菜品。</li>
 * </ol>
 * <p>
 * 当前实现基于 Redis String 存储序列化向量，配合余弦相似度做粗排召回。
 * 可扩展为专用向量数据库（如 Milvus / Qdrant）。
 *
 * @see <a href="https://github.com/ollama/ollama/blob/main/api/types.go">Ollama Embedding API</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticSearchService {

	private final AiAssistantProperties properties;

	private final ObjectMapper objectMapper;

	private final StringRedisTemplate stringRedisTemplate;

	private final RestTemplateBuilder restTemplateBuilder;

	/**
	 * 根据语义意图执行向量检索，返回匹配的菜品 ID 列表（按相似度降序）。
	 * <p>
	 * 流程：
	 * <ol>
	 *   <li>拼接语义 embedding text。</li>
	 *   <li>生成 query embedding 向量。</li>
	 *   <li>在 Redis 中扫描所有菜品 embedding，计算余弦相似度。</li>
	 *   <li>返回 Top-N 匹配结果。</li>
	 * </ol>
	 *
	 * @param intent 解析后的用户意图
	 * @param merchantUserId 可选商家限定
	 * @return 匹配的菜品 ID 列表（按相似度降序），最多 topK 条
	 */
	public List<SemanticMatch> searchBySemanticIntent(IntentResult intent, Long merchantUserId) {
		if (!properties.getSemantic().isEnabled()) {
			log.debug("[SemanticSearch] 语义检索已禁用，跳过");
			return List.of();
		}

		if (intent == null || !intent.hasSemanticConstraints()) {
			log.debug("[SemanticSearch] 无语义约束，跳过向量检索");
			return List.of();
		}

		String embeddingText = buildEmbeddingText(intent);
		if (!StringUtils.hasText(embeddingText)) {
			log.debug("[SemanticSearch] embeddingText 为空，跳过向量检索");
			return List.of();
		}

		log.debug("[SemanticSearch] embeddingText={}", embeddingText);
		float[] queryVector;
		try {
			queryVector = generateEmbedding(embeddingText);
		}
		catch (Exception e) {
			log.warn("[SemanticSearch] embedding 生成失败: {}", e.getMessage());
			return List.of();
		}

		List<SemanticMatch> matches = scanAndScoreVectors(queryVector, merchantUserId);
		matches.sort(Comparator.comparingDouble(SemanticMatch::similarity).reversed());

		int topK = properties.getSemantic().getTopK();
		if (matches.size() > topK) {
			matches = matches.subList(0, topK);
		}

		log.debug("[SemanticSearch] 向量检索完成, embeddingText={}, matchedCount={}", embeddingText, matches.size());
		return matches;
	}

	/**
	 * 根据语义意图拼接 embedding text。
	 * <p>
	 * 拼接规则（文档 # 8.1）：
	 * <pre>
	 * 类别:{category};
	 * 标签:{tags};
	 * 场景:{suitableScenes};
	 * 人群:{suitablePeople};
	 * 口味:{flavorDescription};
	 * </pre>
	 * 示例：
	 * <pre>
	 * 类别:川菜; 标签:辣味,下饭; 场景:聚餐; 人群:重口味用户; 口味:麻辣鲜香;
	 * </pre>
	 */
	private String buildEmbeddingText(IntentResult intent) {
		StringBuilder builder = new StringBuilder();

		if (StringUtils.hasText(intent.getCategory())) {
			builder.append("类别:").append(intent.getCategory()).append("; ");
		}

		if (intent.getTags() != null && !intent.getTags().isEmpty()) {
			builder.append("标签:").append(String.join(",", intent.getTags())).append("; ");
		}

		if (intent.getSuitableScenes() != null && !intent.getSuitableScenes().isEmpty()) {
			builder.append("场景:").append(String.join(",", intent.getSuitableScenes())).append("; ");
		}

		if (intent.getSuitablePeople() != null && !intent.getSuitablePeople().isEmpty()) {
			builder.append("人群:").append(String.join(",", intent.getSuitablePeople())).append("; ");
		}

		if (intent.getKeywords() != null && !intent.getKeywords().isEmpty()) {
			builder.append("关键词:").append(String.join(",", intent.getKeywords())).append("; ");
		}

		if (StringUtils.hasText(intent.getQueryRewrite())) {
			builder.append(intent.getQueryRewrite());
		}

		return builder.toString().trim();
	}

	/**
	 * 调用 embedding 模型生成向量。
	 * <p>
	 * 基于 Ollama / vLLM 的 OpenAI 兼容 /api/embeddings 接口。
	 */
	private float[] generateEmbedding(String text) throws Exception {
		AiAssistantProperties.Semantic cfg = properties.getSemantic();
		String url = cfg.getEmbeddingUrl();
		String model = cfg.getEmbeddingModel();

		RestTemplate restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofMillis(5000))
				.setReadTimeout(Duration.ofMillis(10000))
				.build();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> payload = new HashMap<>();
		payload.put("model", model);
		payload.put("prompt", text);

		log.debug("[SemanticSearch][embedding] url={} model={} text={}", url, model, text);

		ResponseEntity<String> response = restTemplate.postForEntity(
				url,
				new HttpEntity<>(payload, headers),
				String.class);

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new IllegalStateException("Embedding API 调用失败: " + response.getStatusCode());
		}

		JsonNode root = objectMapper.readTree(response.getBody());
		JsonNode embeddingNode = root.path("embedding");
		if (embeddingNode.isMissingNode() || !embeddingNode.isArray()) {
			throw new IllegalStateException("Embedding 返回格式异常，缺少 embedding 字段");
		}

		List<Float> vec = new ArrayList<>();
		for (JsonNode val : embeddingNode) {
			vec.add((float) val.asDouble());
		}

		if (vec.isEmpty()) {
			throw new IllegalStateException("Embedding 返回空向量");
		}

		float[] result = new float[vec.size()];
		for (int i = 0; i < vec.size(); i++) {
			result[i] = vec.get(i);
		}

		log.debug("[SemanticSearch][embedding] dimension={}", result.length);
		return result;
	}

	/**
	 * 扫描 Redis 中所有菜品 embedding，计算余弦相似度并排序。
	 * <p>
	 * 当前实现：
	 * <ul>
	 *   <li>按 merchantUserId 隔离 scan，避免跨商家干扰。</li>
	 *   <li>使用 SCAN 游标遍历，避免阻塞。</li>
	 *   <li>余弦相似度 = dot(a,b) / (|a| * |b|)。</li>
	 * </ul>
	 * <p>
	 * 扩展方向：接入 Redis Vector（RediSearch）、Milvus 等专用向量引擎。
	 */
	private List<SemanticMatch> scanAndScoreVectors(float[] queryVector, Long merchantUserId) {
		String prefix = properties.getSemantic().getRedisKeyPrefix();
		String pattern = merchantUserId != null
				? prefix + merchantUserId + ":*"
				: prefix + "*";

		List<SemanticMatch> matches = new ArrayList<>();
		Set<String> visitedKeys = new HashSet<>();

		try {
			org.springframework.data.redis.core.ScanOptions scanOptions = org.springframework.data.redis.core.ScanOptions.scanOptions()
					.match(pattern)
					.count(200)
					.build();

			try (var cursor = stringRedisTemplate.scan(scanOptions)) {
				while (cursor.hasNext()) {
					String key = new String(cursor.next());
					if (!visitedKeys.contains(key)) {
						visitedKeys.add(key);
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("[SemanticSearch] Redis SCAN 遍历失败: {}", e.getMessage());
		}

		if (visitedKeys.isEmpty()) {
			log.debug("[SemanticSearch] Redis 中无菜品 embedding 数据，跳过向量检索");
			return List.of();
		}

		// 批量读取 embedding 向量并计算相似度
		List<String> keyList = new ArrayList<>(visitedKeys);
		List<String> vectors = stringRedisTemplate.opsForValue().multiGet(keyList);

		for (int i = 0; i < keyList.size(); i++) {
			String vectorStr = vectors != null ? vectors.get(i) : null;
			if (!StringUtils.hasText(vectorStr)) {
				continue;
			}

			float[] dishVector = parseVector(vectorStr);
			if (dishVector == null || dishVector.length != queryVector.length) {
				continue;
			}

			double similarity = cosineSimilarity(queryVector, dishVector);
			String key = keyList.get(i);

			Long dishId = parseDishIdFromKey(key);
			if (dishId != null) {
				matches.add(new SemanticMatch(dishId, similarity));
			}
		}

		return matches;
	}

	/**
	 * 余弦相似度计算。
	 */
	private double cosineSimilarity(float[] a, float[] b) {
		double dot = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < a.length; i++) {
			dot += a[i] * b[i];
			normA += a[i] * a[i];
			normB += b[i] * b[i];
		}
		if (normA == 0 || normB == 0) {
			return 0.0;
		}
		return dot / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	/**
	 * 将 JSON 数组字符串解析为 float 向量。
	 */
	private float[] parseVector(String json) {
		if (!StringUtils.hasText(json)) {
			return null;
		}
		try {
			List<Float> list = objectMapper.readValue(json, objectMapper.getTypeFactory()
					.constructCollectionType(List.class, Float.class));
			float[] result = new float[list.size()];
			for (int i = 0; i < list.size(); i++) {
				result[i] = list.get(i);
			}
			return result;
		}
		catch (Exception e) {
			log.warn("[SemanticSearch] 向量解析失败: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * 从 Redis key 中提取 dishId。
	 * <p>
	 * key 格式：takeaway:embedding:{merchantUserId}:{dishId}
	 */
	private Long parseDishIdFromKey(String key) {
		if (!StringUtils.hasText(key)) {
			return null;
		}
		String prefix = properties.getSemantic().getRedisKeyPrefix();
		if (!key.startsWith(prefix)) {
			return null;
		}
		String suffix = key.substring(prefix.length());
		String[] parts = suffix.split(":");
		if (parts.length < 2) {
			return null;
		}
		try {
			return Long.parseLong(parts[parts.length - 1]);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 语义检索匹配结果。
	 *
	 * @param dishId 菜品ID
	 * @param similarity 余弦相似度分数
	 */
	public record SemanticMatch(Long dishId, double similarity) {
	}

}
