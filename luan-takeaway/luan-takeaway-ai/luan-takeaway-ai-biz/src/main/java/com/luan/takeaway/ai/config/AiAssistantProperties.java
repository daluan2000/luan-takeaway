package com.luan.takeaway.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai.assistant")
public class AiAssistantProperties {

	private int maxRecommendation = 5;

	private Llm llm = new Llm();

	@Data
	public static class Llm {

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

		public String resolveBaseUrl() {
			if (isRemoteSource()) {
				return firstNonBlank(remoteBaseUrl, baseUrl);
			}
			return firstNonBlank(localBaseUrl, baseUrl);
		}

		public String resolveApiKey() {
			if (isRemoteSource()) {
				return firstNonBlank(remoteApiKey, apiKey);
			}
			return firstNonBlank(localApiKey, apiKey);
		}

		public String resolveModel() {
			if (isRemoteSource()) {
				return firstNonBlank(remoteModel, model);
			}
			return firstNonBlank(localModel, model);
		}

		private boolean isRemoteSource() {
			return "remote".equalsIgnoreCase(source);
		}

		private String firstNonBlank(String first, String second) {
			if (first != null && !first.isBlank()) {
				return first;
			}
			return second;
		}

	}

}