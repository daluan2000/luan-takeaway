package com.luan.takeaway.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "推荐菜品项")
public class RecommendationItem {

	private Long dishId;

	private Long merchantUserId;

	private String dishName;

	private String dishDesc;

	private BigDecimal price;

	private List<String> tags;

	private String reason;

	private Double score;

}