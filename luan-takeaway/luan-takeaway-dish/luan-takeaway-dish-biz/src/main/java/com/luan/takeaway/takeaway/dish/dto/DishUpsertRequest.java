package com.luan.takeaway.takeaway.dish.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishUpsertRequest {

	private Long id;

	private Long merchantUserId;

	private String dishImage;

	private String dishName;

	private String dishDesc;

	private BigDecimal price;

	private Integer stock;

	private String saleStatus;

	private Boolean autoGenerateKnowledge;

}
