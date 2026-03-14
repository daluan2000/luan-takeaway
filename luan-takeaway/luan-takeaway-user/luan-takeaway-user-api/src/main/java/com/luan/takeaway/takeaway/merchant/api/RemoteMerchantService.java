package com.luan.takeaway.takeaway.merchant.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.constant.TakeawayServiceNameConstants;
import com.luan.takeaway.takeaway.common.entity.WmMerchantUserExt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商家远程服务接口
 *
 * @author pig
 */
@FeignClient(contextId = "remoteMerchantService", value = TakeawayServiceNameConstants.TAKEAWAY_USER_SERVICE)
public interface RemoteMerchantService {

	/**
	 * 分页查询商家列表
	 * @param current 当前页
	 * @param size 每页大小
	 * @param userId 商家用户ID
	 * @param auditStatus 审核状态
	 * @param businessStatus 营业状态
	 * @param includeDishList 是否填充菜品列表
	 * @return 商家分页结果
	 */
	@GetMapping("/user/merchant/page")
	R<Page<WmMerchantUserExt>> page(@RequestParam("current") long current, @RequestParam("size") long size,
			@RequestParam(value = "userId", required = false) Long userId,
			@RequestParam(value = "auditStatus", required = false) String auditStatus,
			@RequestParam(value = "businessStatus", required = false) String businessStatus,
			@RequestParam(value = "includeDishList", required = false) Boolean includeDishList);

}
