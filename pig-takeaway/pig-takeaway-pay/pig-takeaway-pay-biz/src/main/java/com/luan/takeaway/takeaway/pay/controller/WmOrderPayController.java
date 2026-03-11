package com.luan.takeaway.takeaway.pay.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.log.annotation.SysLog;
import com.luan.takeaway.takeaway.common.api.TakeawayApiConstants;
import com.luan.takeaway.takeaway.common.dto.PayRequest;
import com.luan.takeaway.takeaway.common.entity.WmOrderPay;
import com.luan.takeaway.takeaway.pay.service.WmOrderPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 支付管理控制器
 *
 * @author pig
 */
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
