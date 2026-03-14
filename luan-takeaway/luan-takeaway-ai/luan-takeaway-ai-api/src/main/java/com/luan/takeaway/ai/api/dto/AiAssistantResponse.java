package com.luan.takeaway.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AI 点餐响应")
public class AiAssistantResponse {

	@Schema(description = "决策路径：tool-calling 或 rag")
	private String decisionPath;

	@Schema(description = "识别出的用户意图")
	private IntentView intent;

	@Schema(description = "推荐菜品")
	private List<RecommendationItem> recommendations;

	@Schema(description = "RAG 召回证据")
	private List<String> knowledgeEvidence;

	@Schema(description = "推荐总结")
	private String summary;

}