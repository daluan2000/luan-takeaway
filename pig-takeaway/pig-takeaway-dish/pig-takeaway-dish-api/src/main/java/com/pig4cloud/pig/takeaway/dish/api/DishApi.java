package com.pig4cloud.pig.takeaway.dish.api;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.feign.annotation.NoToken;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayServiceNameConstants;
import com.pig4cloud.pig.takeaway.common.dto.DeductStockRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmDish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 菜品服务 Feign 接口
 *
 * @author pig
 */
@FeignClient(contextId = "dishApi", value = TakeawayServiceNameConstants.TAKEAWAY_DISH_SERVICE)
public interface DishApi {

	/**
	 * 扣减库存
	 * @param request 扣减库存请求
	 * @return 是否成功
	 */
	@NoToken
	@PostMapping(TakeawayApiConstants.INTERNAL_DISH_PATH + "/stock/deduct")
	R<Boolean> deductStock(@RequestBody DeductStockRequest request);

	/**
	 * 按ID列表查询菜品
	 * @param merchantUserId 商家用户ID
	 * @param ids 菜品ID列表
	 * @return 菜品列表
	 */
	@NoToken
	@GetMapping(TakeawayApiConstants.INTERNAL_DISH_PATH + "/ids")
	R<List<WmDish>> listByIds(@RequestParam("merchantUserId") Long merchantUserId,
			@RequestParam("ids") List<Long> ids);

}
