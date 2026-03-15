package com.luan.takeaway.takeaway.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HybridDishSearchRequest {

	private Long merchantUserId;

	private Integer limit;

	private String category;

	private Boolean spicy;

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

	private BigDecimal priceMax;

	private List<String> mealTime;

	private String portionSize;

}
