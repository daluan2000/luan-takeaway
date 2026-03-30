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
import com.luan.takeaway.takeaway.dish.api.RemoteDishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜品服务 Feign 调用实现（微服务模式）
 *
 * @author luan
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteDishServiceCallFacadeImpl implements DishServiceCallFacade {

	private final RemoteDishService dishService;

	@Override
	public R<Boolean> deductStock(DeductStockRequest request) {
		return dishService.deductStock(request);
	}

	@Override
	public R<List<WmDish>> listByIds(Long merchantUserId, List<Long> ids) {
		return dishService.listByIds(merchantUserId, ids);
	}

	@Override
	public R<Page<WmDish>> pageDish(long current, long size, String dishName, Long merchantUserId, String saleStatus) {
		return dishService.page(current, size, dishName, merchantUserId, saleStatus);
	}

	@Override
	public R<Page<WmDish>> servicePageDish(long current, long size, String dishName, Long merchantUserId, String saleStatus) {
		return dishService.servicePage(current, size, dishName, merchantUserId, saleStatus);
	}

	@Override
	public R<List<HybridDishCandidateDTO>> searchHybridCandidates(HybridDishSearchRequest request) {
		return dishService.searchHybridCandidates(request);
	}

	@Override
	public R<DishKnowledgeDoc> getKnowledgeDoc(Long dishId) {
		return dishService.getKnowledgeDoc(dishId);
	}

	@Override
	public R<Boolean> upsertKnowledgeDoc(DishKnowledgeUpsertRequest request) {
		return dishService.upsertKnowledgeDoc(request);
	}

}
