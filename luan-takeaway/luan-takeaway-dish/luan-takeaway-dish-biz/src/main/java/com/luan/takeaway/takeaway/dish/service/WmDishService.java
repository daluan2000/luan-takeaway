package com.luan.takeaway.takeaway.dish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.cache.SmartCache;
import com.luan.takeaway.common.core.cache.HotKeyType;
import com.luan.takeaway.takeaway.common.constant.TakeawayStatusConstants;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.dto.HybridDishCandidateDTO;
import com.luan.takeaway.takeaway.common.dto.HybridDishSearchRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import com.luan.takeaway.takeaway.dish.dto.BatchDishRequest;
import com.luan.takeaway.takeaway.dish.dto.BatchDishResult;

import java.util.List;
import java.util.Map;

public interface WmDishService extends IService<WmDish> {

	/**
	 * 分页查询菜品（带缓存）。
	 * - 【防穿透】空值缓存 2 分钟
	 * - 【防击穿】互斥锁 10 秒
	 * - 【防雪崩】TTL 抖动
	 */
	@SmartCache(
		name = "dish:list",
		key = "#page.current + ':' + #page.size + ':' + (#query == null ? '_' : #query.merchantUserId) + ':' + (#query == null || #query.dishName == null ? '_' : #query.dishName) + ':' + (#query == null || #query.saleStatus == null ? '_' : #query.saleStatus)",
		hotKeyType = HotKeyType.DISH,
		hotKeyIdExpression = "#query == null || #query.merchantUserId == null ? '0' : #query.merchantUserId.toString()",
		baseTtlSeconds = 1800,
		hotTtlSeconds = 1800,
		nullTtlSeconds = 120,
		lockTtlSeconds = 10,
		retryTimes = 3,
		retrySleepMillis = 50
	)
	Page<WmDish> pageByQuery(Page<WmDish> page, WmDish query);

	/**
	 * 单个菜品查询（带缓存，热点自适应 TTL）。
	 * 批量查询 listByMerchantAndIds 内部调用此方法。
	 *
	 * <p>【热点自适应】：热点菜品自动延长 TTL，减少回源压力。
	 * - 热点判定：60秒内访问 >= 100次
	 * - 热点 TTL：30分钟
	 * - 普通 TTL：5分钟
	 */
	@SmartCache(
		name = "dish:item",
		key = "#merchantUserId + ':' + #dishId",
		hotKeyType = HotKeyType.DISH,
		hotKeyIdExpression = "#dishId.toString()",
		baseTtlSeconds = 300,
		hotTtlSeconds = 1800,
		nullTtlSeconds = 120,
		lockTtlSeconds = 10,
		retryTimes = 3,
		retrySleepMillis = 50
	)
	WmDish getByMerchantAndId(Long merchantUserId, Long dishId);

	/**
	 * 批量查询菜品（内部调用带注解的单查方法）。
	 */
	List<WmDish> listByMerchantAndIds(Long merchantUserId, List<Long> ids);

	/**
	 * 查询菜品知识文档（带缓存）。
	 */
	@SmartCache(
		name = "dish:knowledge",
		key = "#dishId",
		baseTtlSeconds = 1800,
		hotTtlSeconds = 3600,
		nullTtlSeconds = 120,
		lockTtlSeconds = 10,
		retryTimes = 3,
		retrySleepMillis = 50
	)
	DishKnowledgeDoc getKnowledgeDoc(Long dishId);

	/**
	 * 保存菜品（触发缓存清除）。
	 */
	boolean save(WmDish entity);

	/**
	 * 更新菜品（触发缓存清除）。
	 */
	boolean updateById(WmDish entity);

	/**
	 * 删除菜品（触发缓存清除）。
	 */
	boolean removeById(java.io.Serializable id);

	/**
	 * 扣减库存。
	 */
	boolean deductStock(DeductStockRequest request);

	/**
	 * 异步扣减库存到数据库。
	 */
	boolean asyncDeductStockToDb(Map<Long, Integer> buyCountMap, Long merchantUserId);

	/**
	 * 构建库存消费完成 key。
	 */
	String buildConsumeDoneKey(String orderNo);

	/**
	 * upsert 知识文档。
	 */
	boolean upsertKnowledgeDoc(Long dishId, DishKnowledgeDoc doc);

	/**
	 * 混合搜索候选菜品。
	 */
	List<HybridDishCandidateDTO> searchHybridCandidates(HybridDishSearchRequest request);

	/**
	 * 同步生成知识文档。
	 */
	DishKnowledgeDoc generateKnowledgeDocSync(WmDish dish);

	/**
	 * 批量导入菜品（管理员模式）
	 *
	 * <p>功能说明：管理员批量导入菜品，支持指定商家。
	 * 每个菜品可以单独指定商家（dish.merchantUserId），也可以使用请求级别的 merchantUserId。
	 *
	 * @param request 批量导入请求
	 * @return 批量导入结果
	 * @see BatchDishRequest
	 * @see BatchDishResult
	 */
	BatchDishResult batchImport(BatchDishRequest request);

	/**
	 * 批量导入菜品（商家模式）
	 *
	 * <p>功能说明：商家批量导入自己的菜品，自动使用当前登录商家的 userId。
	 * 请求中的 merchantUserId 会被忽略，统一使用当前登录用户的ID。
	 *
	 * @param request 批量导入请求
	 * @return 批量导入结果
	 * @see BatchDishRequest
	 * @see BatchDishResult
	 */
	BatchDishResult merchantBatchImport(BatchDishRequest request);

}
