package com.luan.takeaway.takeaway.user.controller;

import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.log.annotation.SysLog;
import com.luan.takeaway.takeaway.common.api.TakeawayApiConstants;
import com.luan.takeaway.takeaway.common.entity.WmAddress;
import com.luan.takeaway.takeaway.user.service.WmAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Tag(name = "地址服务")
public class WmAddressController {

	private static final String ADDRESS_BASE_PATH = TakeawayApiConstants.ADDRESS_PATH;

	private final WmAddressService wmAddressService;

	@PostMapping(ADDRESS_BASE_PATH)
	@Operation(summary = "新增地址")
	@SysLog("新增地址")
	public R<WmAddress> create(@RequestBody WmAddress address) {
		return R.ok(wmAddressService.createAddress(address));
	}

	@PutMapping(ADDRESS_BASE_PATH)
	@Operation(summary = "更新地址")
	@SysLog("更新地址")
	public R<Boolean> update(@RequestBody WmAddress address) {
		return R.ok(wmAddressService.updateAddress(address));
	}

	@DeleteMapping(ADDRESS_BASE_PATH + "/{id}")
	@Operation(summary = "删除地址")
	@SysLog("删除地址")
	public R<Boolean> delete(@PathVariable("id") Long id) {
		return R.ok(wmAddressService.deleteAddress(id));
	}

	@GetMapping(ADDRESS_BASE_PATH + "/{id}")
	@Operation(summary = "查询地址详情")
	public R<WmAddress> get(@PathVariable("id") Long id) {
		return R.ok(wmAddressService.getAddress(id));
	}

	@GetMapping(ADDRESS_BASE_PATH + "/list")
	@Operation(summary = "查询当前用户地址列表")
	public R<List<WmAddress>> list() {
		return R.ok(wmAddressService.listCurrentUserAddresses());
	}

}
