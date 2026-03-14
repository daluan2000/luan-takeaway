package com.luan.takeaway.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI 点餐请求")
public class AiAssistantRequest {

	@Schema(description = "用户输入", requiredMode = Schema.RequiredMode.REQUIRED)
	private String message;

	@Schema(description = "可选：限定商家用户ID")
	private Long merchantUserId;

	@Schema(description = "可选：推荐条数上限")
	private Integer limit;

}