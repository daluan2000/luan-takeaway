package com.luan.takeaway.ai.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 结构化意图模型。
 * <p>
 * 该对象贯穿 AI 推荐流程，是“自然语言 -> 可执行筛选条件”的核心载体。
 * 字段覆盖价格、口味、营养、场景、人群和标签等多个维度。
 */
@Data
public class IntentResult {

	private IntentMode mode = IntentMode.TOOL_CALLING;

	private String originalQuery;

	private String category;

	private BigDecimal priceMax;

	private Boolean spicy;

	private Integer people;

	private Boolean preferLight;

	private List<String> keywords = new ArrayList<>();

	private Integer spicyLevel;

	private Boolean lightTaste;

	private Boolean oily;

	private Boolean soupBased;

	private Boolean vegetarian;

	private Integer caloriesMin;

	private Integer caloriesMax;

	private Integer proteinMin;

	private Integer proteinMax;

	private Integer fatMin;

	private Integer fatMax;

	private Integer carbohydrateMin;

	private Integer carbohydrateMax;

	private List<String> mealTime = new ArrayList<>();

	private String portionSize;

	private List<String> tags = new ArrayList<>();

	private List<String> suitableScenes = new ArrayList<>();

	private List<String> avoidScenes = new ArrayList<>();

	private List<String> suitablePeople = new ArrayList<>();

	private String queryRewrite;

}