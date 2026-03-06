package com.pig4cloud.pig.takeaway.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author pig
 */
@RestController
@Tag(name = "健康检查")
public class HealthController {

	/**
	 * 健康检查
	 * @return 服务状态
	 */
	@GetMapping("/healthz")
	@Operation(summary = "健康检查", description = "返回当前服务健康状态")
	public Map<String, Object> healthz() {
		return Map.of("service", "pig-takeaway-user", "status", "UP");
	}

}
