package com.luan.takeaway.takeaway.common.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 混合检索候选菜品 DTO。
 * <p>
 * 用于 AI 模块的 Hybrid Retrieval 阶段，承载菜品基础信息和知识文档。
 */
@Data
public class HybridDishCandidateDTO {

	private Long dishId;

	private Long merchantUserId;

	private String dishName;

	private String dishDesc;

	private String dishImage;

	private BigDecimal price;

	private Integer stock;

	private String saleStatus;

	/**
	 * 菜品知识文档（含结构化字段、语义字段和描述字段）。
	 */
	private DishKnowledgeDoc knowledgeDoc;

	/**
	 * 语义相似度分（来自 SemanticSearchService 向量检索）。
	 * <p>
	 * transient，不持久化，仅在 AI 推荐链路中传递。
	 */
	private Double semanticScore;

}
