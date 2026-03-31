package com.luan.takeaway.takeaway.dish.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luan.takeaway.admin.api.util.ParamResolver;
import com.luan.takeaway.ai.api.feign.RemoteAiAssistantService;
import com.luan.takeaway.common.core.cache.SmartCacheEvict;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.constant.TakeawayStatusConstants;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeGenerateEvent;
import com.luan.takeaway.takeaway.common.dto.DishPurchaseItemDTO;
import com.luan.takeaway.takeaway.common.dto.HybridDishCandidateDTO;
import com.luan.takeaway.takeaway.common.dto.HybridDishSearchRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import com.luan.takeaway.takeaway.common.entity.WmDishKnowledgeDoc;
import com.luan.takeaway.takeaway.common.mapper.WmDishMapper;
import com.luan.takeaway.takeaway.common.mapper.WmDishKnowledgeDocMapper;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class WmDishServiceImpl extends ServiceImpl<WmDishMapper, WmDish> implements WmDishService {

	private static final String PARAM_DISH_DB_LOCK_WAIT_TIMEOUT_SECONDS = "TAKEAWAY_DISH_DB_LOCK_WAIT_TIMEOUT_SECONDS";

	private static final String DISH_STOCK_CACHE_KEY_PREFIX = "takeaway:dish:stock:";

	private static final long REDIS_DEDUCT_SUCCESS = 1L;

	private static final long REDIS_DEDUCT_NOT_ENOUGH = 0L;

	private static final long REDIS_DEDUCT_KEY_MISSING = -2L;

	private static final DefaultRedisScript<Long> REDIS_STOCK_DEDUCT_SCRIPT;

	static {
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

	private final WmDishKnowledgeDocMapper wmDishKnowledgeDocMapper;

	private final RemoteAiAssistantService remoteAiAssistantService;

	@Override
	public Page<WmDish> pageByQuery(Page<WmDish> page, WmDish query) {
		// 缓存由 @SmartCache 注解处理
		if (query == null) {
			query = new WmDish();
		}
		return page(page, Wrappers.<WmDish>lambdaQuery()
			.eq(query.getMerchantUserId() != null, WmDish::getMerchantUserId, query.getMerchantUserId())
			.like(query.getDishName() != null && !query.getDishName().isBlank(), WmDish::getDishName, query.getDishName())
			.eq(query.getSaleStatus() != null && !query.getSaleStatus().isBlank(), WmDish::getSaleStatus, query.getSaleStatus())
			.orderByDesc(WmDish::getCreateTime));
	}

	@Override
	public WmDish getByMerchantAndId(Long merchantUserId, Long dishId) {
		// 缓存由 @SmartCache 注解处理
		return getOne(Wrappers.<WmDish>lambdaQuery()
			.eq(WmDish::getMerchantUserId, merchantUserId)
			.eq(WmDish::getId, dishId)
			.last("LIMIT 1"), false);
	}

	@Override
	public List<WmDish> listByMerchantAndIds(Long merchantUserId, List<Long> ids) {
		// 批量查询：内部调用带注解的单查方法
		if (merchantUserId == null || ids == null || ids.isEmpty()) {
			return List.of();
		}
		List<Long> uniqueIds = new ArrayList<>(new LinkedHashSet<>(ids));
		List<WmDish> result = new ArrayList<>(uniqueIds.size());
		for (Long dishId : uniqueIds) {
			WmDish dish = getByMerchantAndId(merchantUserId, dishId);
			if (dish != null) {
				result.add(dish);
			}
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@SmartCacheEvict(name = "dish:item", key = "#entity.merchantUserId + ':' + #entity.id")
	@SmartCacheEvict(name = "dish:list", allEntries = true)
	public boolean save(WmDish entity) {
		boolean saved = super.save(entity);
		// 缓存清除由 @SmartCacheEvict 注解处理
		return saved;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@SmartCacheEvict(name = "dish:item", key = "#entity.merchantUserId + ':' + #entity.id")
	@SmartCacheEvict(name = "dish:list", allEntries = true)
	public boolean updateById(WmDish entity) {
		return super.updateById(entity);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@SmartCacheEvict(name = "dish:item", key = "#id")
	@SmartCacheEvict(name = "dish:list", allEntries = true)
	public boolean removeById(java.io.Serializable id) {
		if (id == null) {
			return false;
		}
		return super.removeById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deductStock(DeductStockRequest request) {
		List<DishPurchaseItemDTO> items = request.getItems();
		if (items == null || items.isEmpty()) {
			return true;
		}
		if (request.getMerchantUserId() == null) {
			throw new IllegalArgumentException("商家ID不能为空");
		}

		// 合并同一 dishId 的购买数量
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

		// 初始化 Redis 库存缓存
		List<String> redisStockKeys = buildDishStockKeys(request.getMerchantUserId(), dishIds);
		ensureRedisStockCache(redisStockKeys, dishMap);

		// 执行 Lua 原子扣减
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

		// 投递 MQ 事件异步落库
		publishStockDeductEvent(request, buyCountMap);
		return true;
	}

	private List<String> buildDishStockKeys(Long merchantUserId, List<Long> dishIds) {
		List<String> keys = new ArrayList<>(dishIds.size());
		for (Long dishId : dishIds) {
			keys.add(DISH_STOCK_CACHE_KEY_PREFIX + merchantUserId + ":" + dishId);
		}
		return keys;
	}

	private void ensureRedisStockCache(List<String> stockKeys, Map<Long, WmDish> dishMap) {
		for (String stockKey : stockKeys) {
			Long dishId = parseDishIdFromStockKey(stockKey);
			WmDish dish = dishMap.get(dishId);
			if (dish == null) {
				continue;
			}
			Integer stock = dish.getStock() == null ? 0 : dish.getStock();
			stringRedisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(stock));
		}
	}

	private Long executeRedisStockDeduct(List<String> stockKeys, Map<Long, Integer> buyCountMap) {
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
		int idx = stockKey.lastIndexOf(":");
		if (idx < 0 || idx + 1 >= stockKey.length()) {
			throw new IllegalStateException("库存key非法: " + stockKey);
		}
		return Long.parseLong(stockKey.substring(idx + 1));
	}

	private void publishStockDeductEvent(DeductStockRequest request, Map<Long, Integer> buyCountMap) {
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
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@SmartCacheEvict(name = "dish:item", key = "#merchantUserId + ':' + #buyCountMap.keySet()")
	@SmartCacheEvict(name = "dish:list", allEntries = true)
	public boolean asyncDeductStockToDb(Map<Long, Integer> buyCountMap, Long merchantUserId) {
		if (merchantUserId == null || buyCountMap == null || buyCountMap.isEmpty()) {
			return true;
		}

		jdbcTemplate.execute("SET innodb_lock_wait_timeout = " + getDbLockWaitTimeoutSeconds());
		int updatedRows = executeBatchDeductSql(buyCountMap, merchantUserId);
		if (updatedRows != buyCountMap.size()) {
			log.error("异步落库库存扣减失败, merchantUserId={}, requestItems={}, updatedRows={}", merchantUserId, buyCountMap,
					updatedRows);
			throw new IllegalStateException("异步库存落库失败");
		}
		return true;
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
	public String buildConsumeDoneKey(String orderNo) {
		if (!StringUtils.hasText(orderNo)) {
			return null;
		}
		return "takeaway:dish:stock:consume:done:" + orderNo;
	}

	@Override
	public DishKnowledgeDoc getKnowledgeDoc(Long dishId) {
		// 缓存由 @SmartCache 注解处理
		if (dishId == null) {
			return null;
		}
		WmDishKnowledgeDoc entity = wmDishKnowledgeDocMapper.selectOne(Wrappers.<WmDishKnowledgeDoc>lambdaQuery()
			.eq(WmDishKnowledgeDoc::getDishId, dishId)
			.last("LIMIT 1"));
		if (entity == null) {
			return null;
		}
		return toDoc(entity);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@SmartCacheEvict(name = "dish:knowledge", key = "#dishId")
	public boolean upsertKnowledgeDoc(Long dishId, DishKnowledgeDoc doc) {
		if (dishId == null || doc == null) {
			throw new IllegalArgumentException("菜品知识文档参数不能为空");
		}
		WmDish dish = getById(dishId);
		if (dish == null) {
			throw new IllegalArgumentException("菜品不存在");
		}
		WmDishKnowledgeDoc existing = wmDishKnowledgeDocMapper.selectOne(Wrappers.<WmDishKnowledgeDoc>lambdaQuery()
			.eq(WmDishKnowledgeDoc::getDishId, dishId)
			.last("LIMIT 1"));
		WmDishKnowledgeDoc entity = toEntity(dishId, doc);
		if (existing == null) {
			wmDishKnowledgeDocMapper.insert(entity);
		}
		else {
			entity.setId(existing.getId());
			wmDishKnowledgeDocMapper.updateById(entity);
		}
		return true;
	}

	@Override
	public List<HybridDishCandidateDTO> searchHybridCandidates(HybridDishSearchRequest request) {
		HybridDishSearchRequest search = request == null ? new HybridDishSearchRequest() : request;

		List<WmDish> dishes;
		List<Long> semanticIds = search.getSemanticCandidateIds();

		if (semanticIds != null && !semanticIds.isEmpty()) {
			dishes = listByMerchantAndIds(search.getMerchantUserId(), semanticIds);
			if (dishes.isEmpty()) {
				return List.of();
			}
			if (search.getPriceMax() != null) {
				dishes = dishes.stream()
					.filter(d -> d.getPrice() == null || d.getPrice().compareTo(search.getPriceMax()) <= 0)
					.collect(Collectors.toList());
			}
		}
		else {
			int size = search.getLimit() == null || search.getLimit() <= 0
					? 120 : Math.min(search.getLimit(), 300);
			dishes = page(new Page<>(1, size), Wrappers.<WmDish>lambdaQuery()
				.eq(search.getMerchantUserId() != null, WmDish::getMerchantUserId, search.getMerchantUserId())
				.eq(WmDish::getSaleStatus, TakeawayStatusConstants.Dish.SALE_ON)
				.le(search.getPriceMax() != null, WmDish::getPrice, search.getPriceMax())
				.orderByDesc(WmDish::getCreateTime)).getRecords();

			if (dishes.isEmpty()) {
				return List.of();
			}
		}

		Map<Long, WmDishKnowledgeDoc> knowledgeMap = wmDishKnowledgeDocMapper
			.selectList(Wrappers.<WmDishKnowledgeDoc>lambdaQuery()
				.in(WmDishKnowledgeDoc::getDishId,
					dishes.stream().map(WmDish::getId).filter(Objects::nonNull).collect(Collectors.toSet())))
			.stream()
			.collect(Collectors.toMap(WmDishKnowledgeDoc::getDishId, v -> v, (a, b) -> a));

		List<HybridDishCandidateDTO> result = new ArrayList<>();
		for (WmDish dish : dishes) {
			WmDishKnowledgeDoc knowledge = knowledgeMap.get(dish.getId());
			if (!matchStructuredFilter(search, knowledge)) {
				continue;
			}
			HybridDishCandidateDTO candidate = new HybridDishCandidateDTO();
			candidate.setDishId(dish.getId());
			candidate.setMerchantUserId(dish.getMerchantUserId());
			candidate.setDishName(dish.getDishName());
			candidate.setDishDesc(dish.getDishDesc());
			candidate.setDishImage(dish.getDishImage());
			candidate.setPrice(dish.getPrice());
			candidate.setStock(dish.getStock());
			candidate.setSaleStatus(dish.getSaleStatus());
			candidate.setKnowledgeDoc(knowledge == null ? null : toDoc(knowledge));
			result.add(candidate);
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@SmartCacheEvict(name = "dish:knowledge", key = "#dish.id")
	public DishKnowledgeDoc generateKnowledgeDocSync(WmDish dish) {
		if (dish == null || dish.getId() == null) {
			throw new IllegalArgumentException("菜品不存在");
		}
		DishKnowledgeGenerateEvent event = new DishKnowledgeGenerateEvent();
		event.setDishId(dish.getId());
		event.setMerchantUserId(dish.getMerchantUserId());
		event.setDishName(dish.getDishName());
		event.setDishDesc(dish.getDishDesc());
		event.setPrice(dish.getPrice());

		R<DishKnowledgeDoc> response = remoteAiAssistantService.generateKnowledgeDoc(event);
		DishKnowledgeDoc doc = response == null ? null : response.getData();
		if (doc == null) {
			throw new IllegalStateException("LLM 同步生成知识文档失败");
		}
		upsertKnowledgeDoc(dish.getId(), doc);
		return getKnowledgeDoc(dish.getId());
	}

	private boolean matchStructuredFilter(HybridDishSearchRequest request, WmDishKnowledgeDoc doc) {
		if (doc == null) {
			return !hasStrictStructuredConstraints(request);
		}
		if (request.getCategory() != null && !request.getCategory().equalsIgnoreCase(emptyToNull(doc.getCategory()))) {
			return false;
		}
		if (request.getSpicy() != null && !request.getSpicy().equals(doc.getSpicy())) {
			return false;
		}
		if (request.getSpicyLevel() != null && !request.getSpicyLevel().equals(doc.getSpicyLevel())) {
			return false;
		}
		if (request.getLightTaste() != null && !request.getLightTaste().equals(doc.getLightTaste())) {
			return false;
		}
		if (request.getOily() != null && !request.getOily().equals(doc.getOily())) {
			return false;
		}
		if (request.getSoupBased() != null && !request.getSoupBased().equals(doc.getSoupBased())) {
			return false;
		}
		if (request.getVegetarian() != null && !request.getVegetarian().equals(doc.getVegetarian())) {
			return false;
		}
		if (request.getCaloriesMin() != null && (doc.getCalories() == null || doc.getCalories() < request.getCaloriesMin())) {
			return false;
		}
		if (request.getCaloriesMax() != null && (doc.getCalories() == null || doc.getCalories() > request.getCaloriesMax())) {
			return false;
		}
		if (request.getProteinMin() != null && (doc.getProtein() == null || doc.getProtein() < request.getProteinMin())) {
			return false;
		}
		if (request.getProteinMax() != null && (doc.getProtein() == null || doc.getProtein() > request.getProteinMax())) {
			return false;
		}
		if (request.getFatMin() != null && (doc.getFat() == null || doc.getFat() < request.getFatMin())) {
			return false;
		}
		if (request.getFatMax() != null && (doc.getFat() == null || doc.getFat() > request.getFatMax())) {
			return false;
		}
		if (request.getCarbohydrateMin() != null
				&& (doc.getCarbohydrate() == null || doc.getCarbohydrate() < request.getCarbohydrateMin())) {
			return false;
		}
		if (request.getCarbohydrateMax() != null
				&& (doc.getCarbohydrate() == null || doc.getCarbohydrate() > request.getCarbohydrateMax())) {
			return false;
		}
		if (request.getPortionSize() != null && !request.getPortionSize().equalsIgnoreCase(emptyToNull(doc.getPortionSize()))) {
			return false;
		}
		if (request.getMealTime() != null && !request.getMealTime().isEmpty()) {
			List<String> mealTimes = parseStringList(doc.getMealTimeJson());
			if (!hasIntersection(request.getMealTime(), mealTimes)) {
				return false;
			}
		}
		return true;
	}

	private DishKnowledgeDoc toDoc(WmDishKnowledgeDoc entity) {
		DishKnowledgeDoc doc = new DishKnowledgeDoc();
		doc.setDishId(entity.getDishId());
		doc.setCategory(entity.getCategory());
		doc.setSpicy(entity.getSpicy());
		doc.setSpicyLevel(entity.getSpicyLevel());
		doc.setLightTaste(entity.getLightTaste());
		doc.setOily(entity.getOily());
		doc.setSoupBased(entity.getSoupBased());
		doc.setVegetarian(entity.getVegetarian());
		doc.setCalories(entity.getCalories());
		doc.setProtein(entity.getProtein());
		doc.setFat(entity.getFat());
		doc.setCarbohydrate(entity.getCarbohydrate());
		doc.setMealTime(parseStringList(entity.getMealTimeJson()));
		doc.setPortionSize(entity.getPortionSize());
		doc.setTags(parseStringList(entity.getTags()));
		doc.setSuitableScenes(parseStringList(entity.getSuitableScenes()));
		doc.setAvoidScenes(parseStringList(entity.getAvoidScenes()));
		doc.setSuitablePeople(parseStringList(entity.getSuitablePeople()));
		doc.setEmbeddingText(entity.getEmbeddingText());
		doc.setFlavorDescription(entity.getFlavorDescription());
		doc.setLlmSummary(entity.getLlmSummary());
		doc.setRecommendationReason(entity.getRecommendationReason());
		return doc;
	}

	private WmDishKnowledgeDoc toEntity(Long dishId, DishKnowledgeDoc doc) {
		WmDishKnowledgeDoc entity = new WmDishKnowledgeDoc();
		entity.setDishId(dishId);
		entity.setCategory(doc.getCategory());
		entity.setSpicy(doc.getSpicy());
		entity.setSpicyLevel(doc.getSpicyLevel());
		entity.setLightTaste(doc.getLightTaste());
		entity.setOily(doc.getOily());
		entity.setSoupBased(doc.getSoupBased());
		entity.setVegetarian(doc.getVegetarian());
		entity.setCalories(doc.getCalories());
		entity.setProtein(doc.getProtein());
		entity.setFat(doc.getFat());
		entity.setCarbohydrate(doc.getCarbohydrate());
		entity.setMealTimeJson(writeJsonList(doc.getMealTime()));
		entity.setPortionSize(doc.getPortionSize());
		entity.setTags(writeJsonList(doc.getTags()));
		entity.setSuitableScenes(writeJsonList(doc.getSuitableScenes()));
		entity.setAvoidScenes(writeJsonList(doc.getAvoidScenes()));
		entity.setSuitablePeople(writeJsonList(doc.getSuitablePeople()));
		entity.setEmbeddingText(doc.getEmbeddingText());
		entity.setFlavorDescription(doc.getFlavorDescription());
		entity.setLlmSummary(doc.getLlmSummary());
		entity.setRecommendationReason(doc.getRecommendationReason());
		return entity;
	}

	private List<String> parseStringList(String json) {
		if (!StringUtils.hasText(json)) {
			return List.of();
		}
		try {
			List<String> parsed = objectMapper.readValue(json, new TypeReference<List<String>>() {});
			return parsed == null ? List.of() : parsed.stream().filter(StringUtils::hasText).collect(Collectors.toList());
		}
		catch (Exception ex) {
			return List.of();
		}
	}

	private String writeJsonList(Collection<String> values) {
		if (values == null || values.isEmpty()) {
			return "[]";
		}
		try {
			return objectMapper.writeValueAsString(values);
		}
		catch (Exception ex) {
			return "[]";
		}
	}

	private boolean hasIntersection(List<String> source, List<String> target) {
		if (source == null || source.isEmpty() || target == null || target.isEmpty()) {
			return false;
		}
		Set<String> targetSet = target.stream()
			.filter(StringUtils::hasText)
			.map(String::trim)
			.collect(Collectors.toSet());
		for (String value : source) {
			if (StringUtils.hasText(value) && targetSet.contains(value.trim())) {
				return true;
			}
		}
		return false;
	}

	private String emptyToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private boolean hasStrictStructuredConstraints(HybridDishSearchRequest request) {
		return request.getSpicy() != null
				|| request.getSpicyLevel() != null
				|| request.getLightTaste() != null
				|| request.getOily() != null
				|| request.getSoupBased() != null
				|| request.getVegetarian() != null
				|| request.getCaloriesMin() != null
				|| request.getCaloriesMax() != null
				|| request.getProteinMin() != null
				|| request.getProteinMax() != null
				|| request.getFatMin() != null
				|| request.getFatMax() != null
				|| request.getCarbohydrateMin() != null
				|| request.getCarbohydrateMax() != null
				|| request.getPortionSize() != null;
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
