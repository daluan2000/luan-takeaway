package com.luan.takeaway.takeaway.dish.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.log.annotation.SysLog;
import com.luan.takeaway.common.security.annotation.Inner;
import com.luan.takeaway.takeaway.common.api.TakeawayApiConstants;
import com.luan.takeaway.takeaway.common.constant.TakeawayStatusConstants;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeUpsertRequest;
import com.luan.takeaway.takeaway.common.dto.HybridDishCandidateDTO;
import com.luan.takeaway.takeaway.common.dto.HybridDishSearchRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import com.luan.takeaway.takeaway.dish.dto.DishUpsertRequest;
import com.luan.takeaway.takeaway.dish.service.WmDishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理控制器
 *
 * @author pig
 */
@RestController
@AllArgsConstructor
@Tag(name = "菜品服务")
public class WmDishController {

	private final WmDishService wmDishService;

	@GetMapping(TakeawayApiConstants.DISH_PATH + "/page")
	@Operation(summary = "分页查询菜品")
	public R<Page<WmDish>> page(@ParameterObject Page<WmDish> page, @ParameterObject WmDish query) {
		return R.ok(wmDishService.pageByQuery(page, query));
	}

	@PostMapping(TakeawayApiConstants.DISH_PATH)
	@SysLog("新增菜品")
	@Operation(summary = "新增菜品")
	public R<Boolean> save(@RequestBody DishUpsertRequest request) {
		WmDish dish = toDish(request);
		if (dish.getSaleStatus() == null) {
			dish.setSaleStatus(TakeawayStatusConstants.Dish.SALE_ON);
		}
		boolean success = wmDishService.save(dish);
		if (success && Boolean.TRUE.equals(request.getAutoGenerateKnowledge())) {
			wmDishService.generateKnowledgeDocSync(dish);
		}
		return R.ok(success);
	}

	@PutMapping(TakeawayApiConstants.DISH_PATH)
	@SysLog("修改菜品")
	@Operation(summary = "修改菜品")
	public R<Boolean> update(@RequestBody DishUpsertRequest request) {
		WmDish dish = toDish(request);
		boolean success = wmDishService.updateById(dish);
		if (success && Boolean.TRUE.equals(request.getAutoGenerateKnowledge())) {
			wmDishService.generateKnowledgeDocSync(dish);
		}
		return R.ok(success);
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

	@PostMapping(TakeawayApiConstants.DISH_PATH + "/service/stock/deduct")
	@Operation(summary = "扣减库存")
	public R<Boolean> deductStock(@RequestBody DeductStockRequest request) {
		return R.ok(wmDishService.deductStock(request));
	}

	@GetMapping(TakeawayApiConstants.DISH_PATH + "/service/ids")
	@Operation(summary = "按ID查询菜品")
	public R<List<WmDish>> listByIds(@RequestParam("merchantUserId") Long merchantUserId,
			@RequestParam("ids") List<Long> ids) {
		return R.ok(wmDishService.listByMerchantAndIds(merchantUserId, ids));
	}

	@GetMapping(TakeawayApiConstants.DISH_PATH + "/{dishId}/knowledge-doc")
	@Operation(summary = "查询菜品知识文档")
	public R<DishKnowledgeDoc> getKnowledgeDoc(@PathVariable("dishId") Long dishId) {
		return R.ok(wmDishService.getKnowledgeDoc(dishId));
	}

	@PutMapping(TakeawayApiConstants.DISH_PATH + "/{dishId}/knowledge-doc")
	@Operation(summary = "更新菜品知识文档")
	// 面向前端/商家端调用：走标准登录鉴权，保留用户身份语义。
	public R<Boolean> updateKnowledgeDoc(@PathVariable("dishId") Long dishId, @RequestBody DishKnowledgeDoc knowledgeDoc) {
		return R.ok(wmDishService.upsertKnowledgeDoc(dishId, knowledgeDoc));
	}

	@PostMapping(TakeawayApiConstants.DISH_PATH + "/{dishId}/knowledge-doc/generate")
	@Operation(summary = "同步生成菜品知识文档")
	public R<DishKnowledgeDoc> generateKnowledgeDoc(@PathVariable("dishId") Long dishId) {
		return doGenerateKnowledgeDoc(dishId);
	}

	@PostMapping(TakeawayApiConstants.DISH_PATH + "/knowledge-doc/generate")
	@Operation(summary = "同步生成菜品知识文档（按现有POST接口风格）")
	public R<DishKnowledgeDoc> generateKnowledgeDocByParam(@RequestParam("dishId") Long dishId) {
		return doGenerateKnowledgeDoc(dishId);
	}

	private R<DishKnowledgeDoc> doGenerateKnowledgeDoc(Long dishId) {
		WmDish dish = wmDishService.getById(dishId);
		if (dish == null) {
			throw new IllegalArgumentException("菜品不存在");
		}
		return R.ok(wmDishService.generateKnowledgeDocSync(dish));
	}

	@PostMapping(TakeawayApiConstants.DISH_PATH + "/service/hybrid/candidates")
	@Operation(summary = "混合检索候选菜品")
	public R<List<HybridDishCandidateDTO>> searchHybridCandidates(@RequestBody HybridDishSearchRequest request) {
		return R.ok(wmDishService.searchHybridCandidates(request));
	}

	@GetMapping(TakeawayApiConstants.DISH_PATH + "/service/knowledge/doc")
	@Operation(summary = "按菜品ID查询知识文档")
	public R<DishKnowledgeDoc> getKnowledgeDocForService(@RequestParam("dishId") Long dishId) {
		return R.ok(wmDishService.getKnowledgeDoc(dishId));
	}

	@PostMapping(TakeawayApiConstants.DISH_PATH + "/service/knowledge/upsert")
	@Operation(summary = "内部：回写菜品知识文档")
	@Inner
	// 面向服务间/MQ异步链路：允许无用户token访问，但必须携带内部来源标识 from=Y。
	public R<Boolean> upsertKnowledgeDocForService(@RequestBody DishKnowledgeUpsertRequest request) {
		if (request == null || request.getDishId() == null || request.getKnowledgeDoc() == null) {
			throw new IllegalArgumentException("请求参数不完整");
		}
		return R.ok(wmDishService.upsertKnowledgeDoc(request.getDishId(), request.getKnowledgeDoc()));
	}

	private WmDish toDish(DishUpsertRequest request) {
		WmDish dish = new WmDish();
		dish.setId(request.getId());
		dish.setMerchantUserId(request.getMerchantUserId());
		dish.setDishImage(request.getDishImage());
		dish.setDishName(request.getDishName());
		dish.setDishDesc(request.getDishDesc());
		dish.setPrice(request.getPrice());
		dish.setStock(request.getStock());
		dish.setSaleStatus(request.getSaleStatus());
		return dish;
	}

}
