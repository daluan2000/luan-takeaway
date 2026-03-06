package com.pig4cloud.pig.takeaway.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.common.dto.CreateDeliveryOrderRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryOrder;
import com.pig4cloud.pig.takeaway.user.dto.WmDeliveryDTO;
import com.pig4cloud.pig.takeaway.user.service.WmDeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 配送管理控制器
 *
 * @author pig
 */
@RestController
@AllArgsConstructor
@Tag(name = "配送服务")
public class WmDeliveryController {

	private final WmDeliveryService wmDeliveryService;

	@PostMapping(TakeawayApiConstants.DELIVERY_PATH + "/rider")
	@Operation(summary = "骑手新增")
	@SysLog("骑手新增")
	public R<WmDeliveryDTO> createRider(@RequestBody WmDeliveryDTO riderDTO) {
		return R.ok(wmDeliveryService.createRider(riderDTO));
	}

	@GetMapping(TakeawayApiConstants.DELIVERY_PATH + "/rider/current")
	@Operation(summary = "获取当前用户骑手扩展信息")
	public R<WmDeliveryDTO> current() {
		return R.ok(wmDeliveryService.current());
	}

	@PutMapping(TakeawayApiConstants.DELIVERY_PATH + "/rider")
	@Operation(summary = "骑手更新")
	@SysLog("骑手更新")
	public R<Boolean> updateRider(@RequestBody WmDeliveryDTO riderDTO) {
		return R.ok(wmDeliveryService.updateRider(riderDTO));
	}

	@GetMapping(TakeawayApiConstants.DELIVERY_PATH + "/order/page")
	@Operation(summary = "配送单分页查询")
	public R<Page<WmDeliveryOrder>> page(@ParameterObject Page<WmDeliveryOrder> page,
			@RequestParam(required = false) Long deliveryUserId, @RequestParam(required = false) String status) {
		return R.ok(wmDeliveryService.pageOrders(page, deliveryUserId, status));
	}

	@PostMapping(TakeawayApiConstants.DELIVERY_PATH + "/service/create")
	@Operation(summary = "创建配送单")
	public R<Boolean> create(@RequestBody CreateDeliveryOrderRequest request) {
		return R.ok(wmDeliveryService.createDeliveryOrder(request));
	}

}
