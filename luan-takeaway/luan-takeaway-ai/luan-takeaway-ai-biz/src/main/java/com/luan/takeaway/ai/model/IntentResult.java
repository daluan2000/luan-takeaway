package com.luan.takeaway.ai.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

}