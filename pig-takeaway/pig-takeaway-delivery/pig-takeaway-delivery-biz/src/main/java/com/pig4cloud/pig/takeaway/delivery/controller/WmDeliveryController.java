package com.pig4cloud.pig.takeaway.delivery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.common.dto.CreateDeliveryOrderRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryOrder;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryUserExt;
import com.pig4cloud.pig.takeaway.delivery.service.WmDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Tag(name = "配送服务")
public class WmDeliveryController {

	private final WmDeliveryService wmDeliveryService;

	@PostMapping(TakeawayApiConstants.DELIVERY_PATH + "/rider")
	@Operation(summary = "骑手注册/更新")
	@SysLog("骑手注册")
	public R<Boolean> rider(@RequestBody WmDeliveryUserExt rider) {
		return R.ok(wmDeliveryService.registerRider(rider));
	}

	@GetMapping(TakeawayApiConstants.DELIVERY_PATH + "/order/page")
	@Operation(summary = "配送单分页查询")
	public R<Page<WmDeliveryOrder>> page(@ParameterObject Page<WmDeliveryOrder> page,
			@RequestParam(required = false) Long deliveryUserId, @RequestParam(required = false) String status) {
		return R.ok(wmDeliveryService.pageOrders(page, deliveryUserId, status));
	}

	@PostMapping(TakeawayApiConstants.DELIVERY_PATH + "/order/{orderId}/accept/{deliveryUserId}")
	@Operation(summary = "骑手接单")
	@SysLog("骑手接单")
	public R<Boolean> accept(@PathVariable("orderId") Long orderId,
			@PathVariable("deliveryUserId") Long deliveryUserId) {
		return R.ok(wmDeliveryService.acceptOrder(orderId, deliveryUserId));
	}

	@PostMapping(TakeawayApiConstants.DELIVERY_PATH + "/order/{orderId}/complete/{deliveryUserId}")
	@Operation(summary = "配送完成")
	@SysLog("配送完成")
	public R<Boolean> complete(@PathVariable("orderId") Long orderId,
			@PathVariable("deliveryUserId") Long deliveryUserId) {
		return R.ok(wmDeliveryService.completeOrder(orderId, deliveryUserId));
	}

	@PostMapping(TakeawayApiConstants.INTERNAL_DELIVERY_PATH + "/create")
	@Operation(summary = "内部调用-创建配送单")
	public R<Boolean> create(@RequestBody CreateDeliveryOrderRequest request) {
		return R.ok(wmDeliveryService.createDeliveryOrder(request));
	}

}
