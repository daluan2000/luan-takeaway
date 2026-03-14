package com.luan.takeaway.ai.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.constant.TakeawayServiceNameConstants;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "dishToolClient", value = TakeawayServiceNameConstants.TAKEAWAY_DISH_SERVICE)
public interface DishToolClient {

	@GetMapping("/dish/page")
	R<Page<WmDish>> page(@RequestParam("current") long current, @RequestParam("size") long size,
			@RequestParam(value = "dishName", required = false) String dishName,
			@RequestParam(value = "merchantUserId", required = false) Long merchantUserId,
			@RequestParam(value = "saleStatus", required = false) String saleStatus);

}