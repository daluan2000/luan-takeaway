package com.luan.takeaway.takeaway.dish.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.api.TakeawayApiConstants;
import com.luan.takeaway.takeaway.common.constant.TakeawayServiceNameConstants;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.entity.WmDish;
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
@FeignClient(contextId = "remoteDishService", value = TakeawayServiceNameConstants.TAKEAWAY_DISH_SERVICE)
public interface RemoteDishService {

	/**
	 * 扣减库存
	 * @param request 扣减库存请求
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.DISH_PATH + "/service/stock/deduct")
	R<Boolean> deductStock(@RequestBody DeductStockRequest request);

	/**
	 * 按ID列表查询菜品
	 * @param merchantUserId 商家用户ID
	 * @param ids 菜品ID列表
	 * @return 菜品列表
	 */
	@GetMapping(TakeawayApiConstants.DISH_PATH + "/service/ids")
	R<List<WmDish>> listByIds(@RequestParam("merchantUserId") Long merchantUserId,
			@RequestParam("ids") List<Long> ids);

	/**
	 * 分页查询菜品列表
	 * @param current 当前页
	 * @param size 每页大小
	 * @param dishName 菜品名称（模糊）
	 * @param merchantUserId 商家用户ID
	 * @param saleStatus 上架状态
	 * @return 菜品分页结果
	 */
	@GetMapping("/dish/page")
	R<Page<WmDish>> page(@RequestParam("current") long current, @RequestParam("size") long size,
			@RequestParam(value = "dishName", required = false) String dishName,
			@RequestParam(value = "merchantUserId", required = false) Long merchantUserId,
			@RequestParam(value = "saleStatus", required = false) String saleStatus);

}
