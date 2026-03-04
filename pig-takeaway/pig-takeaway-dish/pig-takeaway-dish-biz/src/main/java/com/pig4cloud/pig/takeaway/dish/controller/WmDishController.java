package com.pig4cloud.pig.takeaway.dish.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.dto.DeductStockRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmDish;
import com.pig4cloud.pig.takeaway.dish.service.WmDishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Tag(name = "菜品服务")
public class WmDishController {

	private final WmDishService wmDishService;

	@GetMapping(TakeawayApiConstants.DISH_PATH + "/page")
	@Operation(summary = "分页查询菜品")
	public R<Page<WmDish>> page(@ParameterObject Page<WmDish> page, @ParameterObject WmDish query) {
		return R.ok(wmDishService.page(page,
				Wrappers.<WmDish>lambdaQuery()
					.eq(query.getMerchantUserId() != null, WmDish::getMerchantUserId, query.getMerchantUserId())
					.like(query.getDishName() != null && !query.getDishName().isBlank(), WmDish::getDishName,
							query.getDishName())
					.eq(query.getSaleStatus() != null && !query.getSaleStatus().isBlank(), WmDish::getSaleStatus,
							query.getSaleStatus())
					.orderByDesc(WmDish::getCreateTime)));
	}

	@PostMapping(TakeawayApiConstants.DISH_PATH)
	@SysLog("新增菜品")
	@Operation(summary = "新增菜品")
	public R<Boolean> save(@RequestBody WmDish dish) {
		if (dish.getSaleStatus() == null) {
			dish.setSaleStatus(TakeawayStatusConstants.Dish.SALE_ON);
		}
		return R.ok(wmDishService.save(dish));
	}

	@PutMapping(TakeawayApiConstants.DISH_PATH)
	@SysLog("修改菜品")
	@Operation(summary = "修改菜品")
	public R<Boolean> update(@RequestBody WmDish dish) {
		return R.ok(wmDishService.updateById(dish));
	}

	@DeleteMapping(TakeawayApiConstants.DISH_PATH + "/{id}")
	@SysLog("删除菜品")
	@Operation(summary = "删除菜品")
	public R<Boolean> remove(@PathVariable("id") Long id) {
		return R.ok(wmDishService.removeById(id));
	}

	@PostMapping(TakeawayApiConstants.DISH_PATH + "/{id}/sale-on")
	@Operation(summary = "菜品上架")
	public R<Boolean> saleOn(@PathVariable("id") Long id) {
		WmDish dish = new WmDish();
		dish.setId(id);
		dish.setSaleStatus(TakeawayStatusConstants.Dish.SALE_ON);
		return R.ok(wmDishService.updateById(dish));
	}

	@PostMapping(TakeawayApiConstants.DISH_PATH + "/{id}/sale-off")
	@Operation(summary = "菜品下架")
	public R<Boolean> saleOff(@PathVariable("id") Long id) {
		WmDish dish = new WmDish();
		dish.setId(id);
		dish.setSaleStatus(TakeawayStatusConstants.Dish.SALE_OFF);
		return R.ok(wmDishService.updateById(dish));
	}

	@PostMapping(TakeawayApiConstants.INTERNAL_DISH_PATH + "/stock/deduct")
	@Operation(summary = "内部调用-扣减库存")
	public R<Boolean> deductStock(@RequestBody DeductStockRequest request) {
		return R.ok(wmDishService.deductStock(request));
	}

	@GetMapping(TakeawayApiConstants.INTERNAL_DISH_PATH + "/ids")
	@Operation(summary = "内部调用-按ID查询菜品")
	public R<List<WmDish>> listByIds(@RequestParam("merchantUserId") Long merchantUserId,
			@RequestParam("ids") List<Long> ids) {
		return R.ok(wmDishService.listByMerchantAndIds(merchantUserId, ids));
	}

}
