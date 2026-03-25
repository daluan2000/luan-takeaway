package com.luan.takeaway.takeaway.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class HybridDishSearchRequest {

	private Long merchantUserId;

	/**
	 * 检索返回条数上限。
	 */
	private Integer limit;

	// ==================== 结构化检索条件（硬约束） ====================

	/**
	 * 菜品类别。
	 */
	private String category;

	/**
	 * 价格上限。
	 */
	private BigDecimal priceMax;

	/**
	 * 是否辣。
	 */
	private Boolean spicy;

	/**
	 * 辣度等级。
	 */
	private Integer spicyLevel;

	/**
	 * 是否清淡。
	 */
	private Boolean lightTaste;

	/**
	 * 是否油腻。
	 */
	private Boolean oily;

	/**
	 * 是否汤类。
	 */
	private Boolean soupBased;

	/**
	 * 是否素食。
	 */
	private Boolean vegetarian;

	/**
	 * 热量下限。
	 */
	private Integer caloriesMin;

	/**
	 * 热量上限。
	 */
	private Integer caloriesMax;

	/**
	 * 蛋白质下限。
	 */
	private Integer proteinMin;

	/**
	 * 蛋白质上限。
	 */
	private Integer proteinMax;

	/**
	 * 脂肪下限。
	 */
	private Integer fatMin;

	/**
	 * 脂肪上限。
	 */
	private Integer fatMax;

	/**
	 * 碳水下限。
	 */
	private Integer carbohydrateMin;

	/**
	 * 碳水上限。
	 */
	private Integer carbohydrateMax;

	/**
	 * 适用餐段。
	 */
	private List<String> mealTime = new ArrayList<>();

	/**
	 * 分量。
	 */
	private String portionSize;

	// ==================== 语义候选 ID（软约束，来自向量检索） ====================

	/**
	 * 语义向量检索命中的菜品 ID 列表。
	 * <p>
	 * 当语义检索有效时，这些 ID 用于构建候选集。
	 * 后续会与结构化过滤结果合并，形成最终候选。
	 */
	private List<Long> semanticCandidateIds;

}
