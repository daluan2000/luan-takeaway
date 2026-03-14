package com.luan.takeaway.ai.controller;

import com.luan.takeaway.ai.api.dto.AiAssistantRequest;
import com.luan.takeaway.ai.api.dto.AiAssistantResponse;
import com.luan.takeaway.ai.service.AiAssistantFacadeService;
import com.luan.takeaway.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/assistant")
@Tag(name = "AI 点餐助手")
public class AiAssistantController {

	private final AiAssistantFacadeService aiAssistantFacadeService;

	@PostMapping("/recommend")
	@Operation(summary = "AI 点餐推荐")
	public R<AiAssistantResponse> recommend(@RequestBody AiAssistantRequest request) {
		return R.ok(aiAssistantFacadeService.recommend(request));
	}

}