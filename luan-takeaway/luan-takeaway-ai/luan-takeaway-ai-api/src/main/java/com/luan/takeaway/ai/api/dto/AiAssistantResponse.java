package com.luan.takeaway.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * AI 点餐助手统一响应体。
 * <p>
 * 由 AI 服务聚合意图识别、候选筛选和推荐文案后返回，
 * 上游模块（如 user 接口层）直接透传给前端即可。
 */
@Data
@Schema(description = "AI 点餐响应")
public class AiAssistantResponse {

	/**
	 * 本次推荐决策路径：
	 * tool-calling 表示偏结构化检索，rag 表示偏知识增强语义推荐。
	 */
	@Schema(description = "决策路径：tool-calling 或 rag")
	private String decisionPath;

	/**
	 * 从用户输入中提取出的结构化意图。
	 */
	@Schema(description = "识别出的用户意图")
	private IntentView intent;

	/**
	 * 最终推荐菜品列表，已经过模型/规则排序。
	 */
	@Schema(description = "推荐菜品")
	private List<RecommendationItem> recommendations;

	/**
	 * 给用户展示的摘要说明，包含推荐理由和整体建议。
	 */
	@Schema(description = "推荐总结")
	private String summary;

}