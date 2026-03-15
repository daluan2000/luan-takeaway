package com.luan.takeaway.ai.controller;

import com.luan.takeaway.ai.api.dto.AiAssistantRequest;
import com.luan.takeaway.ai.api.dto.AiAssistantResponse;
import com.luan.takeaway.ai.service.AiAssistantFacadeService;
import com.luan.takeaway.ai.service.DishKnowledgeGenerationService;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.security.annotation.Inner;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeGenerateEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 点餐对外控制器。
 * <p>
 * 该控制器是 AI 服务的 HTTP 入口，通常由上游业务模块通过 Feign 调用，
 * 不直接承载复杂逻辑，主要负责参数接收与服务编排。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/assistant")
@Tag(name = "AI 点餐助手")
public class AiAssistantController {

	private final AiAssistantFacadeService aiAssistantFacadeService;

	private final DishKnowledgeGenerationService dishKnowledgeGenerationService;

	/**
	 * AI 推荐入口。
	 * <p>
	 * 交互链路：调用方 -> 本接口 -> AiAssistantFacadeService -> 混合推荐服务。
	 */
	@PostMapping("/recommend")
	@Operation(summary = "AI 点餐推荐")
	public R<AiAssistantResponse> recommend(@RequestBody AiAssistantRequest request) {
		return R.ok(aiAssistantFacadeService.recommend(request));
	}

	/**
	 * 同步生成菜品知识文档。
	 * <p>
	 * 该接口用于系统内部调用（@Inner），常见于菜品变更后触发知识重建。
	 */
	@PostMapping("/knowledge/generate")
	@Inner
	@Operation(summary = "同步生成菜品知识文档")
	public R<DishKnowledgeDoc> generateKnowledgeDoc(@RequestBody DishKnowledgeGenerateEvent request) {
		return R.ok(dishKnowledgeGenerationService.generate(request));
	}

}