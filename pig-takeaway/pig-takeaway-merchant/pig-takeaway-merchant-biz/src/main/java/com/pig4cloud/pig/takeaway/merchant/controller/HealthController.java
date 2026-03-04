package com.pig4cloud.pig.takeaway.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

	@GetMapping("/healthz")
	public Map<String, Object> healthz() {
		return Map.of("service", "pig-takeaway-merchant", "status", "UP");
	}

}
