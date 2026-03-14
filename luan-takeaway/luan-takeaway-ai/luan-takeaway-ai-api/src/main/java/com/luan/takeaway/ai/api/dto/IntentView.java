package com.luan.takeaway.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "意图识别结果")
public class IntentView {

	private String route;

	private String originalQuery;

	private String category;

	private BigDecimal priceMax;

	private Boolean spicy;

	private Integer people;

	private Boolean preferLight;

	private List<String> keywords;

}