package com.luan.takeaway.ai.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 结构化意图模型（Query Understanding 输出）。
 * <p>
 * 该对象贯穿 AI 推荐全链路，是"自然语言 -> 可执行筛选条件"的核心载体。
 * <p>
 * 按文档设计（# 8.2 查询处理流程），意图分为两大类：
 * <ul>
 *   <li>结构化意图：可转成明确过滤条件的"硬约束"，用于菜品候选召回与结构化过滤。</li>
 *   <li>语义意图：不一定是硬过滤条件，但能表达偏好、场景和人群特征的"软约束"，用于语义向量检索。</li>
 * </ul>
 */
@Data
public class IntentResult {

	// ==================== 模式标注（辅助说明，非路由决定） ====================

	/**
	 * 请求模式标签（TOOL_CALLING/RAG），用于意图提取与结果解释辅助。
	 * <p>
	 * 不做二选一路由决定，模式标签与实际检索链路解耦。
	 */
	private IntentMode mode = IntentMode.TOOL_CALLING;

	/**
	 * 用户原始自然语言输入。
	 */
	private String originalQuery;

	// ==================== 结构化意图（硬约束） ====================

	/**
	 * 菜品类别，硬过滤条件。
	 * <p>
	 * 示例："晚饭想吃不辣的面" -> category=面
	 */
	private String category;

	/**
	 * 预算上限，硬过滤条件。
	 * <p>
	 * 示例："预算30以内" -> priceMax=30
	 */
	private BigDecimal priceMax;

	/**
	 * 是否辣，硬过滤条件。
	 */
	private Boolean spicy;

	/**
	 * 辣度等级，硬过滤条件。
	 */
	private Integer spicyLevel;

	/**
	 * 是否清淡，硬过滤条件。
	 */
	private Boolean lightTaste;

	/**
	 * 是否油腻，硬过滤条件。
	 */
	private Boolean oily;

	/**
	 * 是否汤类，硬过滤条件。
	 */
	private Boolean soupBased;

	/**
	 * 是否素食，硬过滤条件。
	 */
	private Boolean vegetarian;

	/**
	 * 热量下限，硬过滤条件。
	 */
	private Integer caloriesMin;

	/**
	 * 热量上限，硬过滤条件。
	 */
	private Integer caloriesMax;

	/**
	 * 蛋白质下限，硬过滤条件。
	 */
	private Integer proteinMin;

	/**
	 * 蛋白质上限，硬过滤条件。
	 */
	private Integer proteinMax;

	/**
	 * 脂肪下限，硬过滤条件。
	 */
	private Integer fatMin;

	/**
	 * 脂肪上限，硬过滤条件。
	 */
	private Integer fatMax;

	/**
	 * 碳水下限，硬过滤条件。
	 */
	private Integer carbohydrateMin;

	/**
	 * 碳水上限，硬过滤条件。
	 */
	private Integer carbohydrateMax;

	/**
	 * 适用餐段，硬过滤条件。
	 * <p>
	 * 示例："晚饭想吃不辣的面" -> mealTime=[dinner]
	 */
	private List<String> mealTime = new ArrayList<>();

	/**
	 * 分量，硬过滤条件。
	 */
	private String portionSize;

	/**
	 * 就餐人数，辅助计算分量。
	 */
	private Integer people;

	// ==================== 语义意图（软约束） ====================

	/**
	 * 语义关键词，软约束，用于语义相似度计算。
	 */
	private List<String> keywords = new ArrayList<>();

	/**
	 * 语义标签，软约束，用于语义相似度计算。
	 * <p>
	 * 示例："今天有点上火，想吃清淡、好消化的" -> tags=[清淡, 易消化]
	 */
	private List<String> tags = new ArrayList<>();

	/**
	 * 适合场景，软约束，用于语义相似度计算。
	 */
	private List<String> suitableScenes = new ArrayList<>();

	/**
	 * 避免场景，软约束，用于语义相似度计算。
	 * <p>
	 * 示例："今天有点上火" -> avoidScenes=[上火]
	 */
	private List<String> avoidScenes = new ArrayList<>();

	/**
	 * 适合人群，软约束，用于语义相似度计算。
	 */
	private List<String> suitablePeople = new ArrayList<>();

	/**
	 * 查询改写，语义改写后的表达，用于语义检索。
	 */
	private String queryRewrite;

	// ==================== 辅助方法 ====================

	/**
	 * 判断是否有结构化过滤条件（硬约束）。
	 */
	public boolean hasStructuredConstraints() {
		return category != null || priceMax != null || spicy != null || spicyLevel != null
				|| lightTaste != null || oily != null || soupBased != null || vegetarian != null
				|| caloriesMin != null || caloriesMax != null || proteinMin != null || proteinMax != null
				|| fatMin != null || fatMax != null || carbohydrateMin != null || carbohydrateMax != null
				|| (mealTime != null && !mealTime.isEmpty()) || portionSize != null;
	}

	/**
	 * 判断是否有语义过滤条件（软约束）。
	 */
	public boolean hasSemanticConstraints() {
		return (keywords != null && !keywords.isEmpty())
				|| (tags != null && !tags.isEmpty())
				|| (suitableScenes != null && !suitableScenes.isEmpty())
				|| (avoidScenes != null && !avoidScenes.isEmpty())
				|| (suitablePeople != null && !suitablePeople.isEmpty())
				|| (queryRewrite != null && !queryRewrite.isBlank());
	}

}
