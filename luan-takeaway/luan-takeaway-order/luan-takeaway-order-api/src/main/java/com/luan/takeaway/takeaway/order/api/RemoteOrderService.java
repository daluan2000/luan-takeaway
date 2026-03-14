package com.luan.takeaway.takeaway.order.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.constant.TakeawayServiceNameConstants;
import com.luan.takeaway.takeaway.common.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单远程服务接口
 *
 * @author pig
 */
@FeignClient(contextId = "remoteOrderService", value = TakeawayServiceNameConstants.TAKEAWAY_ORDER_SERVICE)
public interface RemoteOrderService {

	/**
	 * 分页查询订单列表
	 * @param current 当前页
	 * @param size 每页大小
	 * @param customerUserId 客户用户ID
	 * @param merchantUserId 商家用户ID
	 * @param deliveryUserId 配送员用户ID
	 * @param status 订单状态
	 * @return 订单分页结果
	 */
	@GetMapping("/order/page")
	R<Page<OrderDTO>> page(@RequestParam("current") long current, @RequestParam("size") long size,
			@RequestParam(value = "customerUserId", required = false) Long customerUserId,
			@RequestParam(value = "merchantUserId", required = false) Long merchantUserId,
			@RequestParam(value = "deliveryUserId", required = false) Long deliveryUserId,
			@RequestParam(value = "status", required = false) String status);

}
