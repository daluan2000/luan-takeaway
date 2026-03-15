package com.luan.takeaway.takeaway.common.dto;

import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HybridDishCandidateDTO {

	private Long dishId;

	private Long merchantUserId;

	private String dishName;

	private String dishDesc;

	private String dishImage;

	private BigDecimal price;

	private Integer stock;

	private String saleStatus;

	private DishKnowledgeDoc knowledgeDoc;

}
