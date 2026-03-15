package com.luan.takeaway.takeaway.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("wm_dish_knowledge_doc")
@EqualsAndHashCode(callSuper = true)
public class WmDishKnowledgeDoc extends BaseTakeawayEntity {

	private Long dishId;

	private String category;

	private Boolean spicy;

	private Integer spicyLevel;

	private Boolean lightTaste;

	private Boolean oily;

	private Boolean soupBased;

	private Boolean vegetarian;

	private Integer calories;

	private Integer protein;

	private Integer fat;

	private Integer carbohydrate;

	@TableField("meal_time")
	private String mealTimeJson;

	private String portionSize;

	private String tags;

	private String suitableScenes;

	private String avoidScenes;

	private String suitablePeople;

	private String embeddingText;

	private String flavorDescription;

	private String llmSummary;

	private String recommendationReason;

}
