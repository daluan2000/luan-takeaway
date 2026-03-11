package com.luan.takeaway.takeaway.dish.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luan.takeaway.takeaway.common.constant.TakeawayStatusConstants;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.dto.DishPurchaseItemDTO;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import com.luan.takeaway.takeaway.common.mapper.WmDishMapper;
import com.luan.takeaway.takeaway.dish.constant.DishStockMqConstants;
import com.luan.takeaway.takeaway.dish.mq.dto.DishStockDeductEvent;
import com.luan.takeaway.takeaway.dish.service.WmDishService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@AllArgsConstructor
public class WmDishServiceImpl extends ServiceImpl<WmDishMapper, WmDish> implements WmDishService {

	private static final String DISH_STOCK_CACHE_KEY_PREFIX = "takeaway:dish:stock:";

	private static final long REDIS_DEDUCT_SUCCESS = 1L;

	private static final long REDIS_DEDUCT_NOT_ENOUGH = 0L;

	private static final long REDIS_DEDUCT_KEY_MISSING = -2L;

	private static final DefaultRedisScript<Long> REDIS_STOCK_DEDUCT_SCRIPT;

	static {
		// Lua 脚本保证“校验库存 + 扣减库存”是一个原子操作，避免并发下超卖。
		// 返回值约定：
		// 1  -> 所有菜品库存都足够且扣减成功
		// 0  -> 至少一个菜品库存不足
		// -2 -> 至少一个库存 key 不存在（可能是首次访问或缓存过期）
		String lua = "for i=1,#KEYS do "
				+ "local stock=redis.call('GET', KEYS[i]); "
				+ "if (not stock) then return -2 end; "
				+ "if (tonumber(stock) < tonumber(ARGV[i])) then return 0 end; "
				+ "end; "
				+ "for i=1,#KEYS do redis.call('DECRBY', KEYS[i], ARGV[i]); end; "
				+ "return 1;";
		REDIS_STOCK_DEDUCT_SCRIPT = new DefaultRedisScript<>(lua, Long.class);
	}

	private final JdbcTemplate jdbcTemplate;

	private final StringRedisTemplate stringRedisTemplate;

	private final RabbitTemplate rabbitTemplate;

	private final ObjectMapper objectMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deductStock(DeductStockRequest request) {
		// 第一步：基础参数校验，空明细直接返回成功（无需扣减）。
		List<DishPurchaseItemDTO> items = request.getItems();
		if (items == null || items.isEmpty()) {
			return true;
		}

		if (request.getMerchantUserId() == null) {
			throw new IllegalArgumentException("商家ID不能为空");
		}

		// 第二步：将同一 dishId 的购买数量合并，并使用 TreeMap 按 dishId 排序。
		// 这样可以保证每次执行 Lua 脚本时 KEYS/ARGV 顺序稳定，方便排查与复现问题。
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

		// 第三步：准备 Redis 库存 key，并在 key 缺失时使用数据库库存进行“懒加载”初始化。
		List<String> redisStockKeys = buildDishStockKeys(request.getMerchantUserId(), dishIds);
		ensureRedisStockCache(redisStockKeys, dishMap);

		// 第四步：执行 Lua 原子扣减。
		// 若返回 -2，说明有 key 缺失，做一次补偿初始化后重试一次，避免因缓存失效导致误失败。
		Long deductResult = executeRedisStockDeduct(redisStockKeys, buyCountMap);
		if (REDIS_DEDUCT_KEY_MISSING == deductResult) {
			ensureRedisStockCache(redisStockKeys, dishMap);
			deductResult = executeRedisStockDeduct(redisStockKeys, buyCountMap);
		}

		if (REDIS_DEDUCT_NOT_ENOUGH == deductResult) {
			throw new IllegalStateException("库存不足，请刷新后重试");
		}
		if (REDIS_DEDUCT_SUCCESS != deductResult) {
			throw new IllegalStateException("库存扣减失败，请稍后重试");
		}

		// 第五步：Redis 预扣减成功后，投递 MQ 事件异步落库。
		// 这样下单主链路不阻塞数据库更新，提升高并发吞吐量。
		publishStockDeductEvent(request, buyCountMap);

		return true;
	}

	private List<String> buildDishStockKeys(Long merchantUserId, List<Long> dishIds) {
		List<String> lockKeys = new ArrayList<>(dishIds.size());
		for (Long dishId : dishIds) {
			lockKeys.add(DISH_STOCK_CACHE_KEY_PREFIX + merchantUserId + ":" + dishId);
		}
		return lockKeys;
	}

	private void ensureRedisStockCache(List<String> stockKeys, Map<Long, WmDish> dishMap) {
		for (String stockKey : stockKeys) {
			Long dishId = parseDishIdFromStockKey(stockKey);
			WmDish dish = dishMap.get(dishId);
			if (dish == null) {
				continue;
			}
			// 仅在 key 不存在时初始化，避免覆盖正在变化的 Redis 实时库存。
			Integer stock = dish.getStock() == null ? 0 : dish.getStock();
			stringRedisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(stock));
		}
	}

	private Long executeRedisStockDeduct(List<String> stockKeys, Map<Long, Integer> buyCountMap) {
		// Lua 脚本参数约定：KEYS 是库存 key 列表，ARGV 是对应扣减数量。
		List<String> args = new ArrayList<>(stockKeys.size());
		for (String stockKey : stockKeys) {
			Long dishId = parseDishIdFromStockKey(stockKey);
			Integer quantity = buyCountMap.get(dishId);
			args.add(String.valueOf(quantity));
		}
		Long result = stringRedisTemplate.execute(REDIS_STOCK_DEDUCT_SCRIPT, stockKeys, args.toArray());
		return result == null ? -1L : result;
	}

	private Long parseDishIdFromStockKey(String stockKey) {
		// key 格式：takeaway:dish:stock:{merchantUserId}:{dishId}
		// 这里通过最后一个冒号后的片段解析 dishId。
		int idx = stockKey.lastIndexOf(":");
		if (idx < 0 || idx + 1 >= stockKey.length()) {
			throw new IllegalStateException("库存key非法: " + stockKey);
		}
		return Long.parseLong(stockKey.substring(idx + 1));
	}

	private void publishStockDeductEvent(DeductStockRequest request, Map<Long, Integer> buyCountMap) {
		// MQ 消息中使用合并后的扣减明细，减少消息体大小并降低消费者处理复杂度。
		DishStockDeductEvent event = new DishStockDeductEvent();
		event.setMerchantUserId(request.getMerchantUserId());
		event.setOrderNo(request.getOrderNo());
		event.setItems(buyCountMap);
		try {
			rabbitTemplate.convertAndSend(DishStockMqConstants.EXCHANGE, DishStockMqConstants.ROUTING_KEY,
					objectMapper.writeValueAsString(event));
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("库存消息发送失败", e);
		}
		catch (Exception e) {
			throw new IllegalStateException("库存消息发送失败", e);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean asyncDeductStockToDb(Map<Long, Integer> buyCountMap, Long merchantUserId) {
		// 消费者异步落库入口：将 Redis 已扣减的库存同步到 MySQL，保证最终一致性。
		if (merchantUserId == null || buyCountMap == null || buyCountMap.isEmpty()) {
			return true;
		}

		// 限制数据库行锁等待时间，防止消费线程长时间阻塞导致堆积。
		jdbcTemplate.execute("SET innodb_lock_wait_timeout = 3");
		int updatedRows = executeBatchDeductSql(buyCountMap, merchantUserId);
		if (updatedRows != buyCountMap.size()) {
			log.error("异步落库库存扣减失败, merchantUserId={}, requestItems={}, updatedRows={}", merchantUserId, buyCountMap,
					updatedRows);
			throw new IllegalStateException("异步库存落库失败");
		}
		return true;
	}

	private int executeBatchDeductSql(Map<Long, Integer> buyCountMap, Long merchantUserId) {
		// 使用 CASE WHEN 批量更新，保证一次 SQL 完成多菜品扣减，减少数据库往返开销。
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

	@Override
	public String buildConsumeDoneKey(String orderNo) {
		if (!StringUtils.hasText(orderNo)) {
			return null;
		}
		return "takeaway:dish:stock:consume:done:" + orderNo;
	}

}
