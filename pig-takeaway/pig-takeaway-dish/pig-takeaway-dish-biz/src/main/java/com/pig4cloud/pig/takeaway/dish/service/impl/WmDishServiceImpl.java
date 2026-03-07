package com.pig4cloud.pig.takeaway.dish.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.dto.DeductStockRequest;
import com.pig4cloud.pig.takeaway.common.dto.DishPurchaseItemDTO;
import com.pig4cloud.pig.takeaway.common.entity.WmDish;
import com.pig4cloud.pig.takeaway.common.mapper.WmDishMapper;
import com.pig4cloud.pig.takeaway.dish.service.WmDishService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@AllArgsConstructor
public class WmDishServiceImpl extends ServiceImpl<WmDishMapper, WmDish> implements WmDishService {

	private static final int LOCK_WAIT_TIMEOUT_SECONDS = 3;

	private static final int LOCK_RETRY_TIMES = 2;

	private final JdbcTemplate jdbcTemplate;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deductStock(DeductStockRequest request) {
		// Keep lock waits short in this transaction to avoid long user-facing hangs.
		jdbcTemplate.execute("SET innodb_lock_wait_timeout = " + LOCK_WAIT_TIMEOUT_SECONDS);

		List<DishPurchaseItemDTO> items = request.getItems();
		if (items == null || items.isEmpty()) {
			return true;
		}

		// Merge repeated dish items and sort by dishId to keep a stable lock acquisition order.
		Map<Long, Integer> buyCountMap = new TreeMap<>();
		for (DishPurchaseItemDTO item : items) {
			if (item.getDishId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
				throw new IllegalArgumentException("菜品和购买数量必须合法");
			}
			buyCountMap.merge(item.getDishId(), item.getQuantity(), Integer::sum);
		}

		List<Long> dishIds = buyCountMap.keySet().stream().toList();
		List<WmDish> dishList = listByMerchantAndIds(request.getMerchantUserId(), dishIds);
		Map<Long, WmDish> dishMap = new HashMap<>(dishList.size());
		dishList.forEach(dish -> dishMap.put(dish.getId(), dish));

		for (Long dishId : dishIds) {
			WmDish dish = dishMap.get(dishId);
			if (dish == null) {
				throw new IllegalArgumentException("菜品不存在: " + dishId);
			}
			if (!TakeawayStatusConstants.Dish.SALE_ON.equals(dish.getSaleStatus())) {
				throw new IllegalStateException("菜品未上架: " + dish.getDishName());
			}
		}

		deductBatchWithRetry(buyCountMap, request.getMerchantUserId());

		return true;
	}

	private void deductBatchWithRetry(Map<Long, Integer> buyCountMap, Long merchantUserId) {
		for (int attempt = 1; attempt <= LOCK_RETRY_TIMES; attempt++) {
			try {
				int updatedRows = executeBatchDeductSql(buyCountMap, merchantUserId);
				if (updatedRows != buyCountMap.size()) {
					throw new IllegalStateException("库存不足或菜品状态变化，请刷新后重试");
				}
				return;
			}
			catch (CannotAcquireLockException e) {
				if (attempt >= LOCK_RETRY_TIMES) {
					throw new IllegalStateException("系统繁忙，请稍后重试下单", e);
				}
				try {
					Thread.sleep(80L * attempt);
				}
				catch (InterruptedException interruptedException) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException("系统繁忙，请稍后重试下单", interruptedException);
				}
			}
		}
	}

	private int executeBatchDeductSql(Map<Long, Integer> buyCountMap, Long merchantUserId) {
		StringBuilder sql = new StringBuilder("UPDATE wm_dish SET stock = CASE id");
		List<Object> params = new ArrayList<>();

		for (Map.Entry<Long, Integer> entry : buyCountMap.entrySet()) {
			sql.append(" WHEN ? THEN stock - ?");
			params.add(entry.getKey());
			params.add(entry.getValue());
		}

		sql.append(" ELSE stock END WHERE del_flag='0' AND merchant_user_id = ? AND sale_status = ? AND id IN (");
		params.add(merchantUserId);
		params.add(TakeawayStatusConstants.Dish.SALE_ON);

		int index = 0;
		for (Long dishId : buyCountMap.keySet()) {
			if (index++ > 0) {
				sql.append(",");
			}
			sql.append("?");
			params.add(dishId);
		}

		sql.append(") AND (");
		index = 0;
		for (Map.Entry<Long, Integer> entry : buyCountMap.entrySet()) {
			if (index++ > 0) {
				sql.append(" OR ");
			}
			sql.append("(id = ? AND stock >= ?)");
			params.add(entry.getKey());
			params.add(entry.getValue());
		}
		sql.append(")");

		return jdbcTemplate.update(sql.toString(), params.toArray());
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
