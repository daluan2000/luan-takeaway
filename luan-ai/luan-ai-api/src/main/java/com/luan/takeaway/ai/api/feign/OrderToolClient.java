package com.luan.takeaway.ai.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.constant.TakeawayServiceNameConstants;
import com.luan.takeaway.takeaway.common.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "orderToolClient", value = TakeawayServiceNameConstants.TAKEAWAY_ORDER_SERVICE)
public interface OrderToolClient {

	@GetMapping("/order/page")
	R<Page<OrderDTO>> page(@RequestParam("current") long current, @RequestParam("size") long size,
			@RequestParam(value = "customerUserId", required = false) Long customerUserId,
			@RequestParam(value = "merchantUserId", required = false) Long merchantUserId,
			@RequestParam(value = "deliveryUserId", required = false) Long deliveryUserId,
			@RequestParam(value = "status", required = false) String status);

}