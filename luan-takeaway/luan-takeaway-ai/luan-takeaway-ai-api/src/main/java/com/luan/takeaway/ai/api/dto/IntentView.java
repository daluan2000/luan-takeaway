package com.luan.takeaway.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 意图展示对象。
 * <p>
 * 这是给上游接口返回用的“可读版意图结构”，字段覆盖了口味、营养、场景、
 * 人群和查询改写等维度，方便前端调试和产品观察模型行为。
 */
@Data
@Schema(description = "意图识别结果")
public class IntentView {

	/**
	 * 路由模式（TOOL_CALLING / RAG）。
	 */
	private String route;

	/**
	 * 用户原始问题。
	 */
	private String originalQuery;

	private String category;

	private BigDecimal priceMax;

	private Boolean spicy;

	private Integer people;

	private Boolean preferLight;

	private List<String> keywords;

	private Integer spicyLevel;

	private Boolean lightTaste;

	private Boolean oily;

	private Boolean soupBased;

	private Boolean vegetarian;

	private Integer caloriesMin;

	private Integer caloriesMax;

	private List<String> mealTime;

	private String portionSize;

	private List<String> tags;

	private List<String> suitableScenes;

	private List<String> avoidScenes;

	private List<String> suitablePeople;

	/**
	 * 对用户请求进行语义改写后的表达，可用于排查召回效果。
	 */
	private String queryRewrite;

}