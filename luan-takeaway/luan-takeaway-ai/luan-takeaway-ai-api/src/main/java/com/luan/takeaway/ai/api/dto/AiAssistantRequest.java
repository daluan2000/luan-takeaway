package com.luan.takeaway.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 点餐助手请求体。
 * <p>
 * 该对象会在用户侧接口（通常由 user 端或网关转发）中传入 AI 模块，
 * 作为整条推荐链路的输入：意图识别 -> 候选召回 -> 重排输出。
 */
@Data
@Schema(description = "AI 点餐请求")
public class AiAssistantRequest {

	/**
	 * 用户原始自然语言输入，例如“胃不舒服，预算 30，想吃点清淡的”。
	 */
	@Schema(description = "用户输入", requiredMode = Schema.RequiredMode.REQUIRED)
	private String message;

	/**
	 * 可选商家 ID。
	 * <p>
	 * 传入后会把推荐范围限制在指定商家，适用于“进入某商家后再让 AI 推荐”的场景。
	 */
	@Schema(description = "可选：限定商家用户ID")
	private Long merchantUserId;

	/**
	 * 可选返回条数。
	 * <p>
	 * 若不传或小于等于 0，由服务端按配置默认值兜底。
	 */
	@Schema(description = "可选：推荐条数上限")
	private Integer limit;

}