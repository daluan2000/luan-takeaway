package com.luan.takeaway.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 助手配置。
 * <p>
 * 对应配置前缀 {@code ai.assistant}，用于统一控制推荐条数、
 * 模型来源（本地/远程）、超时等行为。
 */
@Data
@ConfigurationProperties(prefix = "ai.assistant")
public class AiAssistantProperties {

	/**
	 * 默认推荐条数上限。
	 */
	private int maxRecommendation = 5;

	/**
	 * 语义向量检索配置。
	 */
	private Semantic semantic = new Semantic();

	/**
	 * LLM 相关配置。
	 */
	private Llm llm = new Llm();

	/**
	 * 模型接入参数。
	 * <p>
	 * 支持 local / remote 两套地址与鉴权配置，并通过 resolve 方法统一取值，
	 * 方便业务侧只关心“当前生效模型”而不关心具体来源。
	 */
	@Data
	public static class Llm {

		/** 是否启用 LLM 能力。 */
		private boolean enabled = false;

		/**
		 * 模型来源: local | remote，默认 local。
		 */
		private String source = "local";

		/**
		 * 旧配置，保留兼容。
		 */
		private String baseUrl = "http://127.0.0.1:11434/v1";

		private String apiKey;

		private String model = "qwen2.5:7b";

		/**
		 * 本地模型（如 vLLM/Ollama OpenAI 兼容接口）配置。
		 */
		private String localBaseUrl = "http://127.0.0.1:8000/v1";

		private String localApiKey;

		private String localModel = "qwen3.5-4b";

		/**
		 * 远程模型接口配置。
		 */
		private String remoteBaseUrl;

		private String remoteApiKey;

		private String remoteModel;

		private int timeoutMs = 3000;

		/**
		 * 解析当前生效的 baseUrl。
		 */
		public String resolveBaseUrl() {
			if (isRemoteSource()) {
				return firstNonBlank(remoteBaseUrl, baseUrl);
			}
			return firstNonBlank(localBaseUrl, baseUrl);
		}

		/**
		 * 解析当前生效的 API Key。
		 */
		public String resolveApiKey() {
			if (isRemoteSource()) {
				return firstNonBlank(remoteApiKey, apiKey);
			}
			return firstNonBlank(localApiKey, apiKey);
		}

		/**
		 * 解析当前生效的模型名。
		 */
		public String resolveModel() {
			if (isRemoteSource()) {
				return firstNonBlank(remoteModel, model);
			}
			return firstNonBlank(localModel, model);
		}

		/**
		 * 判定是否使用远程模型源。
		 */
		private boolean isRemoteSource() {
			return "remote".equalsIgnoreCase(source);
		}

		/**
		 * 双值兜底：优先 first，first 为空时回退 second。
		 */
		private String firstNonBlank(String first, String second) {
			if (first != null && !first.isBlank()) {
				return first;
			}
			return second;
		}

	}

	/**
	 * 语义向量检索配置。
	 * <p>
	 * 用于控制 embedding 生成和向量检索的行为。
	 */
	@Data
	public static class Semantic {

		/**
		 * 是否启用语义向量检索（embedding + Redis 向量匹配）。
		 * <p>
		 * 若禁用，则仅走结构化过滤，降级为纯条件召回。
		 */
		private boolean enabled = true;

		/**
		 * Embedding 模型服务地址（OpenAI 兼容接口）。
		 * <p>
		 * 支持 Ollama / vLLM 等部署的 embedding 模型。
		 * 默认使用 Ollama 本地服务。
		 */
		private String embeddingUrl = "http://127.0.0.1:11434/api/embeddings";

		/**
		 * Embedding 模型名称。
		 * <p>
		 * Ollama 推荐: mxbai-embed-large（1024维，高质量）
		 */
		private String embeddingModel = "mxbai-embed-large";

		/**
		 * Embedding 维度。
		 * <p>
		 * mxbai-embed-large 为 1024 维。
		 */
		private int embeddingDimension = 1024;

		/**
		 * 向量检索时返回的 Top-N 近邻数量。
		 * <p>
		 * 作为语义候选池，后续还会经过结构化过滤和融合排序。
		 */
		private int topK = 100;

		/**
		 * Redis 中存储 embedding 向量的 key 前缀。
		 */
		private String redisKeyPrefix = "takeaway:embedding:";

	}

}