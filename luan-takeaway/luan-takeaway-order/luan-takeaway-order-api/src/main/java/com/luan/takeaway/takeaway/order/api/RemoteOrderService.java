package com.luan.takeaway.takeaway.order.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.api.TakeawayApiConstants;
import com.luan.takeaway.takeaway.common.constant.TakeawayServiceNameConstants;
import com.luan.takeaway.takeaway.common.dto.OrderDTO;
import com.luan.takeaway.takeaway.common.entity.WmOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务 Feign 接口
 *
 * @author pig
 */
@FeignClient(contextId = "remoteOrderService", value = TakeawayServiceNameConstants.TAKEAWAY_ORDER_SERVICE)
public interface RemoteOrderService {

	/**
	 * 按ID查询订单
	 * @param orderId 订单ID
	 * @return 订单信息
	 */
	@GetMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}")
	R<WmOrder> getById(@PathVariable("orderId") Long orderId);

	/**
	 * 标记订单已支付
	 * @param orderId 订单ID
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/pay-success")
	R<Boolean> markPaid(@PathVariable("orderId") Long orderId);

	/**
	 * 商家接单
	 * @param orderId 订单ID
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/merchant-accept")
	R<Boolean> merchantAccept(@PathVariable("orderId") Long orderId);

	/**
	 * 开始配送
	 * @param orderId 订单ID
	 * @param deliveryUserId 配送员用户ID
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/delivery-start/{deliveryUserId}")
	R<Boolean> deliveryStart(@PathVariable("orderId") Long orderId,
			@PathVariable("deliveryUserId") Long deliveryUserId);

	/**
	 * 完成订单
	 * @param orderId 订单ID
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/finish")
	R<Boolean> finish(@PathVariable("orderId") Long orderId);

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
