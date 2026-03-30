package com.luan.takeaway.boot.call;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.call.DishServiceCallFacade;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeUpsertRequest;
import com.luan.takeaway.takeaway.common.dto.HybridDishCandidateDTO;
import com.luan.takeaway.takeaway.common.dto.HybridDishSearchRequest;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import com.luan.takeaway.takeaway.dish.service.WmDishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜品服务本地调用实现（单体模式）
 * <p>
 * 直接注入本地 Service，无网络开销
 *
 * @author luan
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class LocalDishServiceCallFacadeImpl implements DishServiceCallFacade {

	private final WmDishService dishService;

	@Override
	public R<Boolean> deductStock(DeductStockRequest request) {
		try {
			boolean result = dishService.deductStock(request);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地扣减库存失败", e);
			return R.failed(e.getMessage());
		}
	}

	@Override
	public R<List<WmDish>> listByIds(Long merchantUserId, List<Long> ids) {
		try {
			List<WmDish> result = dishService.listByMerchantAndIds(merchantUserId, ids);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地查询菜品列表失败", e);
			return R.failed(e.getMessage());
		}
	}

	@Override
	public R<Page<WmDish>> pageDish(long current, long size, String dishName, Long merchantUserId, String saleStatus) {
		try {
			Page<WmDish> page = new Page<>(current, size);
			WmDish query = new WmDish();
			query.setDishName(dishName);
			query.setMerchantUserId(merchantUserId);
			query.setSaleStatus(saleStatus);
			Page<WmDish> result = dishService.pageByQuery(page, query);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地分页查询菜品失败", e);
			return R.failed(e.getMessage());
		}
	}

	@Override
	public R<Page<WmDish>> servicePageDish(long current, long size, String dishName, Long merchantUserId,
			String saleStatus) {
		return pageDish(current, size, dishName, merchantUserId, saleStatus);
	}

	@Override
	public R<List<HybridDishCandidateDTO>> searchHybridCandidates(HybridDishSearchRequest request) {
		try {
			List<HybridDishCandidateDTO> result = dishService.searchHybridCandidates(request);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地混合检索菜品失败", e);
			return R.failed(e.getMessage());
		}
	}

	@Override
	public R<DishKnowledgeDoc> getKnowledgeDoc(Long dishId) {
		try {
			DishKnowledgeDoc result = dishService.getKnowledgeDoc(dishId);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地查询菜品知识文档失败", e);
			return R.failed(e.getMessage());
		}
	}

	@Override
	public R<Boolean> upsertKnowledgeDoc(DishKnowledgeUpsertRequest request) {
		try {
			boolean result = dishService.upsertKnowledgeDoc(request.getDishId(), request.getKnowledgeDoc());
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地更新菜品知识文档失败", e);
			return R.failed(e.getMessage());
		}
	}

}
