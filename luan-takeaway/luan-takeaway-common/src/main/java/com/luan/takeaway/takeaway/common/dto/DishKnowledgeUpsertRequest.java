package com.luan.takeaway.takeaway.common.dto;

import lombok.Data;

@Data
public class DishKnowledgeUpsertRequest {

	private Long dishId;

	private DishKnowledgeDoc knowledgeDoc;

}
