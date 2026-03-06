package com.pig4cloud.pig.takeaway.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.common.entity.WmMerchantUserExt;
import com.pig4cloud.pig.takeaway.merchant.dto.WmMerchantDTO;
import com.pig4cloud.pig.takeaway.merchant.service.WmMerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 商家管理控制器
 *
 * @author pig
 */
@RestController
@AllArgsConstructor
@Tag(name = "商家服务")
public class WmMerchantController {

	private final WmMerchantService wmMerchantService;

	@PostMapping(TakeawayApiConstants.MERCHANT_PATH)
	@Operation(summary = "新增商家信息")
	@SysLog("新增商家信息")
	public R<WmMerchantDTO> create(@RequestBody WmMerchantDTO merchantDTO) {
		return R.ok(wmMerchantService.createMerchant(merchantDTO));
	}

	@PostMapping(TakeawayApiConstants.MERCHANT_PATH + "/apply")
	@Operation(summary = "商家入驻申请")
	@SysLog("商家入驻申请")
	public R<Boolean> apply(@RequestBody WmMerchantUserExt merchant) {
		return R.ok(wmMerchantService.apply(merchant));
	}

	@PostMapping(TakeawayApiConstants.MERCHANT_PATH + "/{id}/audit/{auditStatus}")
	@Operation(summary = "商家审核")
	@SysLog("商家审核")
	public R<Boolean> audit(@PathVariable("id") Long id, @PathVariable("auditStatus") String auditStatus) {
		return R.ok(wmMerchantService.audit(id, auditStatus));
	}

	@PutMapping(TakeawayApiConstants.MERCHANT_PATH)
	@Operation(summary = "商家信息更新")
	@SysLog("商家信息更新")
	public R<Boolean> update(@RequestBody WmMerchantDTO merchantDTO) {
		return R.ok(wmMerchantService.updateMerchant(merchantDTO));
	}

	@GetMapping(TakeawayApiConstants.MERCHANT_PATH + "/current")
	@Operation(summary = "获取当前用户商家扩展信息")
	public R<WmMerchantDTO> current() {
		return R.ok(wmMerchantService.current());
	}

	@GetMapping(TakeawayApiConstants.MERCHANT_PATH + "/page")
	@Operation(summary = "商家分页查询")
	public R<Page<WmMerchantUserExt>> page(@ParameterObject Page<WmMerchantUserExt> page,
			@RequestParam(required = false) Long userId, @RequestParam(required = false) String auditStatus,
			@RequestParam(required = false) String businessStatus) {
		return R.ok(wmMerchantService.page(page, userId, auditStatus, businessStatus));
	}

	@PostMapping(TakeawayApiConstants.MERCHANT_PATH + "/{id}/business/{businessStatus}")
	@Operation(summary = "更新营业状态")
	@SysLog("更新营业状态")
	public R<Boolean> updateBusinessStatus(@PathVariable("id") Long id,
			@PathVariable("businessStatus") String businessStatus) {
		return R.ok(wmMerchantService.updateBusinessStatus(id, businessStatus));
	}

	@PostMapping(TakeawayApiConstants.MERCHANT_PATH + "/order/{orderId}/accept")
	@Operation(summary = "商家接单")
	@SysLog("商家接单")
	public R<Boolean> acceptOrder(@PathVariable("orderId") Long orderId) {
		return R.ok(wmMerchantService.acceptOrder(orderId));
	}

}
