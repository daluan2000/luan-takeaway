package com.luan.takeaway.takeaway.user.controller;

import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.log.annotation.SysLog;
import com.luan.takeaway.takeaway.common.api.TakeawayApiConstants;
import com.luan.takeaway.takeaway.user.dto.WmCustomerDTO;
import com.luan.takeaway.takeaway.user.service.WmCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户管理控制器
 *
 * @author pig
 */
@RestController
@AllArgsConstructor
@Tag(name = "客户服务")
public class WmCustomerController {

	private final WmCustomerService wmCustomerService;

	@PostMapping(TakeawayApiConstants.CUSTOMER_PATH)
	@Operation(summary = "新增客户信息")
	@SysLog("新增客户信息")
	public R<WmCustomerDTO> create(@RequestBody WmCustomerDTO customerDTO) {
		return R.ok(wmCustomerService.createCustomer(customerDTO));
	}

	@PutMapping(TakeawayApiConstants.CUSTOMER_PATH)
	@Operation(summary = "客户信息更新")
	@SysLog("客户信息更新")
	public R<Boolean> update(@RequestBody WmCustomerDTO customerDTO) {
		return R.ok(wmCustomerService.updateCustomer(customerDTO));
	}

	@GetMapping(TakeawayApiConstants.CUSTOMER_PATH + "/current")
	@Operation(summary = "获取当前用户客户扩展信息")
	public R<WmCustomerDTO> current() {
		return R.ok(wmCustomerService.current());
	}

}
