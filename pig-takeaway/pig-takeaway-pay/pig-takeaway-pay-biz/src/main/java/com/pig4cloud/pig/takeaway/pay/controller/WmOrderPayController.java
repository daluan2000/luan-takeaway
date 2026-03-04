package com.pig4cloud.pig.takeaway.pay.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.common.dto.PayRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmOrderPay;
import com.pig4cloud.pig.takeaway.pay.service.WmOrderPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Tag(name = "支付服务")
public class WmOrderPayController {

	private final WmOrderPayService wmOrderPayService;

	@PostMapping(TakeawayApiConstants.PAY_PATH + "/mock")
	@Operation(summary = "模拟支付")
	@SysLog("模拟支付")
	public R<Boolean> mockPay(@RequestBody PayRequest request) {
		return R.ok(wmOrderPayService.mockPay(request));
	}

	@GetMapping(TakeawayApiConstants.PAY_PATH + "/page")
	@Operation(summary = "支付记录分页")
	public R<Page<WmOrderPay>> page(@ParameterObject Page<WmOrderPay> page,
			@RequestParam(required = false) Long orderId) {
		return R.ok(wmOrderPayService.pageByOrderId(page, orderId));
	}

}
