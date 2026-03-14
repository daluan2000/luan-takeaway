package com.luan.takeaway.takeaway.dish.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luan.takeaway.admin.api.util.ParamResolver;
import com.luan.takeaway.takeaway.common.cache.RedisSafeCacheService;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Slf4j
@Service
@AllArgsConstructor
public class WmDishServiceImpl extends ServiceImpl<WmDishMapper, WmDish> implements WmDishService {

	private static final String PARAM_DISH_DB_LOCK_WAIT_TIMEOUT_SECONDS = "TAKEAWAY_DISH_DB_LOCK_WAIT_TIMEOUT_SECONDS";

	private static final String DISH_STOCK_CACHE_KEY_PREFIX = "takeaway:dish:stock:";

	// 菜品个体缓存 key：用于按 merchantId + dishId 读取单个菜品信息。
	private static final String DISH_ITEM_CACHE_KEY_PREFIX = "takeaway:dish:item:";

	// 【防击穿】菜品个体锁 key：热点菜品缓存过期时，只让一个线程回源。
	private static final String DISH_ITEM_LOCK_KEY_PREFIX = "takeaway:dish:item:lock:";

	// 菜品分页缓存 key：缓存 pageByQuery 查询结果。
	private static final String DISH_LIST_CACHE_KEY_PREFIX = "takeaway:dish:list:";

	// 【防击穿】菜品分页锁 key：同一查询条件缓存失效时，避免并发打 DB。
	private static final String DISH_LIST_LOCK_KEY_PREFIX = "takeaway:dish:list:lock:";

	// 菜品缓存版本号：写操作后递增，实现“逻辑失效”（老 key 自动淘汰）。
	private static final String DISH_CACHE_VERSION_KEY = "takeaway:dish:cache:version";

	// 菜品缓存策略（统一交给 RedisSafeCacheService 生效）：
	// - 【防雪崩】基础 TTL 30 分钟 + 抖动 10 分钟
	// - 【防穿透】空值缓存 2 分钟
	// - 【防击穿】锁 10 秒 + 重试 3 次
	private static final RedisSafeCacheService.CachePolicy DISH_CACHE_POLICY = RedisSafeCacheService.CachePolicy
		.of(30 * 60, 10 * 60, 2 * 60, 10, 3, 50);

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

	private final RedisSafeCacheService redisSafeCacheService;

	@Override
	public Page<WmDish> pageByQuery(Page<WmDish> page, WmDish query) {
		// 这里直接复用通用缓存组件：
		// - 【防穿透】不存在的数据会走空值缓存
		// - 【防击穿】同一个查询条件只会有一个线程回源
		// - 【防雪崩】缓存 TTL 会自动加随机抖动
		if (query == null) {
			query = new WmDish();
		}

		long version = getDishCacheVersion();
		String cacheKey = buildDishListCacheKey(page, query, version);
		String lockKey = buildDishListLockKey(page, query, version);
		JavaType pageType = objectMapper.getTypeFactory().constructParametricType(Page.class, WmDish.class);

		WmDish finalQuery = query;
		return redisSafeCacheService.queryWithProtect(cacheKey, lockKey, pageType,
				// 仅在缓存未命中且当前线程拿到锁时才会执行数据库查询。
				() -> page(page, Wrappers.<WmDish>lambdaQuery()
					.eq(finalQuery.getMerchantUserId() != null, WmDish::getMerchantUserId, finalQuery.getMerchantUserId())
					.like(finalQuery.getDishName() != null && !finalQuery.getDishName().isBlank(), WmDish::getDishName,
							finalQuery.getDishName())
					.eq(finalQuery.getSaleStatus() != null && !finalQuery.getSaleStatus().isBlank(), WmDish::getSaleStatus,
							finalQuery.getSaleStatus())
					.orderByDesc(WmDish::getCreateTime)),
				DISH_CACHE_POLICY);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean save(WmDish entity) {
		boolean saved = super.save(entity);
		if (saved && entity != null) {
			// 写后处理：
			// 1) 删掉受影响的个体缓存
			// 2) 版本号 +1，让老分页缓存自然失效
			evictDishCaches(entity.getMerchantUserId(), entity.getId(), true);
			bumpDishCacheVersion();
		}
		return saved;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateById(WmDish entity) {
		WmDish oldDish = entity == null || entity.getId() == null ? null : getById(entity.getId());
		boolean updated = super.updateById(entity);
		if (updated && entity != null && entity.getId() != null) {
			Long merchantUserId = entity.getMerchantUserId();
			if (merchantUserId == null && oldDish != null) {
				merchantUserId = oldDish.getMerchantUserId();
			}
			// 同步删缓存，避免改完菜品后还读到旧值。
			evictDishCaches(merchantUserId, entity.getId(), true);
			bumpDishCacheVersion();
		}
		return updated;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeById(java.io.Serializable id) {
		if (id == null) {
			return false;
		}
		Long dishId;
		try {
			dishId = Long.valueOf(String.valueOf(id));
		}
		catch (NumberFormatException ex) {
			return super.removeById(id);
		}

		WmDish oldDish = getById(dishId);
		boolean removed = super.removeById(id);
		if (removed && oldDish != null) {
			// 删除后把个体缓存删掉，同时升级列表版本。
			evictDishCaches(oldDish.getMerchantUserId(), dishId, true);
			bumpDishCacheVersion();
		}
		return removed;
	}

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
		jdbcTemplate.execute("SET innodb_lock_wait_timeout = " + getDbLockWaitTimeoutSeconds());
		int updatedRows = executeBatchDeductSql(buyCountMap, merchantUserId);
		if (updatedRows != buyCountMap.size()) {
			log.error("异步落库库存扣减失败, merchantUserId={}, requestItems={}, updatedRows={}", merchantUserId, buyCountMap,
					updatedRows);
			throw new IllegalStateException("异步库存落库失败");
		}

		evictDishItemCacheBatch(merchantUserId, buyCountMap.keySet());
		// 库存落库后提升版本，保证分页缓存中的库存相关字段不陈旧。
		bumpDishCacheVersion();
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
		if (merchantUserId == null || ids == null || ids.isEmpty()) {
			return List.of();
		}

		List<Long> uniqueIds = new ArrayList<>(new LinkedHashSet<>(ids));
		if (uniqueIds.isEmpty()) {
			return List.of();
		}

		// 个体查询也是同一套缓存保护，和分页保持一致：
		// 【防穿透】空值缓存、【防击穿】互斥锁、【防雪崩】随机 TTL。
		List<WmDish> finalResult = new ArrayList<>(uniqueIds.size());
		for (Long dishId : uniqueIds) {
			String cacheKey = buildDishItemCacheKey(merchantUserId, dishId);
			String lockKey = buildDishItemLockKey(merchantUserId, dishId);
			WmDish dish = redisSafeCacheService.queryWithProtect(cacheKey, lockKey, WmDish.class,
					() -> getDishFromDb(merchantUserId, dishId), DISH_CACHE_POLICY);
			if (dish != null) {
				finalResult.add(dish);
			}
		}
		return finalResult;
	}

	@Override
	public String buildConsumeDoneKey(String orderNo) {
		if (!StringUtils.hasText(orderNo)) {
			return null;
		}
		return "takeaway:dish:stock:consume:done:" + orderNo;
	}

	private WmDish getDishFromDb(Long merchantUserId, Long dishId) {
		// 按商家隔离查询，避免跨商家串数据。
		return getOne(Wrappers.<WmDish>lambdaQuery()
			.eq(WmDish::getMerchantUserId, merchantUserId)
			.eq(WmDish::getId, dishId)
			.last("LIMIT 1"), false);
	}

	private String buildDishItemCacheKey(Long merchantUserId, Long dishId) {
		return DISH_ITEM_CACHE_KEY_PREFIX + merchantUserId + ":" + dishId;
	}

	private String buildDishItemLockKey(Long merchantUserId, Long dishId) {
		return DISH_ITEM_LOCK_KEY_PREFIX + merchantUserId + ":" + dishId;
	}

	private String buildDishListCacheKey(Page<WmDish> page, WmDish query, long version) {
		// 把查询维度和版本号拼入 key，保证不同条件缓存互不污染。
		String dishName = StringUtils.hasText(query.getDishName()) ? query.getDishName().trim() : "_";
		String saleStatus = StringUtils.hasText(query.getSaleStatus()) ? query.getSaleStatus().trim() : "_";
		String merchantId = query.getMerchantUserId() == null ? "_" : String.valueOf(query.getMerchantUserId());
		return DISH_LIST_CACHE_KEY_PREFIX + version + ":" + page.getCurrent() + ":" + page.getSize() + ":" + merchantId
				+ ":" + dishName + ":" + saleStatus;
	}

	private String buildDishListLockKey(Page<WmDish> page, WmDish query, long version) {
		return DISH_LIST_LOCK_KEY_PREFIX + buildDishListCacheKey(page, query, version);
	}

	private void evictDishItemCacheBatch(Long merchantUserId, Set<Long> dishIds) {
		if (merchantUserId == null || dishIds == null || dishIds.isEmpty()) {
			return;
		}
		// 批量删除减少网络往返，适合 MQ 异步落库场景。
		List<String> keys = new ArrayList<>(dishIds.size());
		for (Long dishId : dishIds) {
			keys.add(buildDishItemCacheKey(merchantUserId, dishId));
			keys.add(buildDishItemLockKey(merchantUserId, dishId));
		}
		stringRedisTemplate.delete(keys);
	}

	private void evictDishCaches(Long merchantUserId, Long dishId, boolean removeStockCache) {
		if (merchantUserId == null || dishId == null) {
			return;
		}
		// 写后删缓存（而非更新缓存）策略：实现简单且一致性更稳健。
		List<String> keys = new ArrayList<>(3);
		keys.add(buildDishItemCacheKey(merchantUserId, dishId));
		keys.add(buildDishItemLockKey(merchantUserId, dishId));
		if (removeStockCache) {
			keys.add(DISH_STOCK_CACHE_KEY_PREFIX + merchantUserId + ":" + dishId);
		}
		stringRedisTemplate.delete(keys);
	}

	private long getDishCacheVersion() {
		// 版本号不存在时按 0 处理，保证首次启动可用。
		String value = stringRedisTemplate.opsForValue().get(DISH_CACHE_VERSION_KEY);
		if (!StringUtils.hasText(value)) {
			return 0L;
		}
		try {
			return Long.parseLong(value);
		}
		catch (NumberFormatException ex) {
			return 0L;
		}
	}

	private void bumpDishCacheVersion() {
		try {
			// 原子递增版本号，让旧列表缓存自然失效，无需扫描删除。
			stringRedisTemplate.opsForValue().increment(DISH_CACHE_VERSION_KEY);
		}
		catch (Exception ex) {
			log.warn("更新菜品缓存版本号失败", ex);
		}
	}

	private int getDbLockWaitTimeoutSeconds() {
		Long resolved = ParamResolver.getLong(PARAM_DISH_DB_LOCK_WAIT_TIMEOUT_SECONDS, 3L);
		if (resolved == null || resolved <= 0L) {
			return 3;
		}
		if (resolved > 120L) {
			return 120;
		}
		return resolved.intValue();
	}

}
