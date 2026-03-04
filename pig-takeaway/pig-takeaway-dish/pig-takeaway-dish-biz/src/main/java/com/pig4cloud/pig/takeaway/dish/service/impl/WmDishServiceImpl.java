package com.pig4cloud.pig.takeaway.dish.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.dto.DeductStockRequest;
import com.pig4cloud.pig.takeaway.common.dto.DishPurchaseItemDTO;
import com.pig4cloud.pig.takeaway.common.entity.WmDish;
import com.pig4cloud.pig.takeaway.dish.mapper.WmDishMapper;
import com.pig4cloud.pig.takeaway.dish.service.WmDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WmDishServiceImpl extends ServiceImpl<WmDishMapper, WmDish> implements WmDishService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deductStock(DeductStockRequest request) {
		List<DishPurchaseItemDTO> items = request.getItems();
		if (items == null || items.isEmpty()) {
			return true;
		}

		List<Long> dishIds = items.stream().map(DishPurchaseItemDTO::getDishId).distinct().toList();
		List<WmDish> dishList = listByMerchantAndIds(request.getMerchantUserId(), dishIds);
		Map<Long, WmDish> dishMap = new HashMap<>(dishList.size());
		dishList.forEach(dish -> dishMap.put(dish.getId(), dish));

		for (DishPurchaseItemDTO item : items) {
			WmDish dish = dishMap.get(item.getDishId());
			if (dish == null) {
				throw new IllegalArgumentException("菜品不存在: " + item.getDishId());
			}
			if (!TakeawayStatusConstants.Dish.SALE_ON.equals(dish.getSaleStatus())) {
				throw new IllegalStateException("菜品未上架: " + dish.getDishName());
			}
			if (dish.getStock() == null || dish.getStock() < item.getQuantity()) {
				throw new IllegalStateException("库存不足: " + dish.getDishName());
			}
			dish.setStock(dish.getStock() - item.getQuantity());
		}

		return updateBatchById(dishList);
	}

	@Override
	public List<WmDish> listByMerchantAndIds(Long merchantUserId, List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		return list(Wrappers.<WmDish>lambdaQuery()
			.eq(WmDish::getMerchantUserId, merchantUserId)
			.in(WmDish::getId, ids));
	}

}
