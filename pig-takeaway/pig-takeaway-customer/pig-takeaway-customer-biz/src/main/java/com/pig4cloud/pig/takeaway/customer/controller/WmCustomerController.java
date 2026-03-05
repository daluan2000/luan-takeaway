package com.pig4cloud.pig.takeaway.customer.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.customer.dto.WmCustomerDTO;
import com.pig4cloud.pig.takeaway.customer.service.WmCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
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

}
