package com.luan.takeaway.admin.controller;

import com.luan.takeaway.admin.api.dto.WsPushMessageDTO;
import com.luan.takeaway.admin.service.WsPushDispatchService;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.security.annotation.Inner;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一 WebSocket 消息推送入口。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/ws")
public class WsPushController {

	private final WsPushDispatchService wsPushDispatchService;

	@Inner
	@PostMapping("/push")
	public R<Boolean> push(@RequestBody WsPushMessageDTO dto) {
		if (dto == null || !StringUtils.hasText(dto.getMessageText())) {
			return R.failed(false, "推送内容不能为空");
		}

		if (!wsPushDispatchService.push(dto)) {
			return R.failed(false, "未配置 WebSocket 分发器");
		}

		return R.ok(Boolean.TRUE);
	}

}
