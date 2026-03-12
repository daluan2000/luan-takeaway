package com.luan.takeaway.takeaway.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.log.annotation.SysLog;
import com.luan.takeaway.takeaway.common.api.TakeawayApiConstants;
import com.luan.takeaway.takeaway.common.dto.OrderDTO;
import com.luan.takeaway.takeaway.common.entity.WmOrder;
import com.luan.takeaway.takeaway.order.service.WmOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理控制器
 *
 * @author pig
 */
@RestController
@AllArgsConstructor
@Tag(name = "订单服务")
public class WmOrderController {

	private final WmOrderService wmOrderService;

	@PostMapping(TakeawayApiConstants.ORDER_PATH)
	@Operation(summary = "用户下单")
	@SysLog("用户下单")
	public R<OrderDTO> create(@RequestBody OrderDTO request) {
		return R.ok(wmOrderService.createOrder(request));
	}

	@GetMapping(TakeawayApiConstants.ORDER_PATH + "/page")
	@Operation(summary = "订单分页查询")
	public R<Page<OrderDTO>> page(@ParameterObject Page<OrderDTO> page,
			@RequestParam(required = false) Long customerUserId,
			@RequestParam(required = false) Long merchantUserId, @RequestParam(required = false) Long deliveryUserId,
			@RequestParam(required = false) String status) {
		return R.ok(wmOrderService.queryPage(page, customerUserId, merchantUserId, deliveryUserId, status));
	}

	@GetMapping(TakeawayApiConstants.ORDER_PATH + "/{orderId}")
	@Operation(summary = "订单详情")
	public R<OrderDTO> detail(@PathVariable("orderId") Long orderId) {
		return R.ok(wmOrderService.detail(orderId));
	}

	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/{orderId}/cancel")
	@Operation(summary = "取消订单")
	public R<Boolean> cancel(@PathVariable("orderId") Long orderId) {
		return R.ok(wmOrderService.cancel(orderId));
	}

	@GetMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}")
	@Operation(summary = "查询订单")
	public R<WmOrder> getById(@PathVariable("orderId") Long orderId) {
		return R.ok(wmOrderService.getById(orderId));
	}

	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/pay-success")
	@Operation(summary = "支付成功")
	public R<Boolean> paySuccess(@PathVariable("orderId") Long orderId) {
		return R.ok(wmOrderService.markPaid(orderId));
	}

	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/merchant-accept")
	@Operation(summary = "商家接单")
	public R<Boolean> merchantAccept(@PathVariable("orderId") Long orderId) {
		return R.ok(wmOrderService.merchantAccept(orderId));
	}

	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/delivery-start/{deliveryUserId}")
	@Operation(summary = "抢单并开始配送")
	public R<Boolean> deliveryStart(@PathVariable("orderId") Long orderId,
			@PathVariable("deliveryUserId") Long deliveryUserId) {
		return R.ok(wmOrderService.deliveryStart(orderId, deliveryUserId));
	}

	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/finish")
	@Operation(summary = "订单完成")
	public R<Boolean> finish(@PathVariable("orderId") Long orderId) {
		return R.ok(wmOrderService.finish(orderId));
	}

}
