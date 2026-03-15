package com.luan.takeaway.takeaway.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishKnowledgeGenerateEvent {

	private Long dishId;

	private Long merchantUserId;

	private String dishName;

	private String dishDesc;

	private BigDecimal price;

}
