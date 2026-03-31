package com.luan.takeaway.takeaway.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.cache.SmartCache;
import com.luan.takeaway.common.core.cache.SmartCacheEvict;
import com.luan.takeaway.takeaway.user.dto.WmMerchantDTO;

import java.math.BigDecimal;
import java.util.List;

public interface WmMerchantService {

	/**
	 * 创建商家（触发缓存清除）。
	 */
	@SmartCacheEvict(name = "merchant:page", allEntries = true)
	@SmartCacheEvict(name = "merchant:list", allEntries = true)
	@SmartCacheEvict(name = "merchant:nearby", allEntries = true)
	WmMerchantDTO createMerchant(WmMerchantDTO merchantDTO);

	/**
	 * 申请商家（触发缓存清除）。
	 */
	@SmartCacheEvict(name = "merchant:page", allEntries = true)
	@SmartCacheEvict(name = "merchant:list", allEntries = true)
	@SmartCacheEvict(name = "merchant:nearby", allEntries = true)
	boolean apply(WmMerchantDTO merchant);

	/**
	 * 审核商家（触发缓存清除）。
	 */
	@SmartCacheEvict(name = "merchant:page", allEntries = true)
	@SmartCacheEvict(name = "merchant:list", allEntries = true)
	@SmartCacheEvict(name = "merchant:nearby", allEntries = true)
	boolean audit(Long id, String auditStatus);

	/**
	 * 更新商家（触发缓存清除）。
	 */
	@SmartCacheEvict(name = "merchant:page", allEntries = true)
	@SmartCacheEvict(name = "merchant:list", allEntries = true)
	@SmartCacheEvict(name = "merchant:nearby", allEntries = true)
	boolean updateMerchant(WmMerchantDTO merchantDTO);

	/**
	 * 获取当前用户商家（带缓存）。
	 * 注意：用户ID从 SecurityUtils 获取，热点检测通过 merchantId 判断。
	 */
	@SmartCache(
		name = "merchant:current",
		key = "'current_user'",
		hotKeyType = com.luan.takeaway.common.core.cache.HotKeyType.SHOP,
		hotKeyIdExpression = "'global'",
		baseTtlSeconds = 300,
		hotTtlSeconds = 1800,
		nullTtlSeconds = 120,
		lockTtlSeconds = 10,
		retryTimes = 3,
		retrySleepMillis = 50
	)
	WmMerchantDTO current();

	/**
	 * 分页查询商家（带缓存）。
	 */
	@SmartCache(
		name = "merchant:page",
		key = "#page.current + ':' + #page.size + ':' + (#userId == null ? '_' : #userId) + ':' + (#auditStatus == null ? '_' : #auditStatus) + ':' + (#businessStatus == null ? '_' : #businessStatus) + ':' + #includeDishList",
		hotKeyType = com.luan.takeaway.common.core.cache.HotKeyType.SHOP,
		hotKeyIdExpression = "#userId == null ? '0' : #userId.toString()",
		baseTtlSeconds = 1200,
		hotTtlSeconds = 1200,
		nullTtlSeconds = 120,
		lockTtlSeconds = 10,
		retryTimes = 3,
		retrySleepMillis = 50
	)
	Page<WmMerchantDTO> page(Page<WmMerchantDTO> page, Long userId, String auditStatus, String businessStatus,
			boolean includeDishList);

	/**
	 * 按地域查询商家列表（带缓存）。
	 */
	@SmartCache(
		name = "merchant:list",
		key = "(#province == null || #province.isEmpty()) ? 'all' : (#province + ':' + (#city == null ? '_' : #city) + ':' + (#district == null ? '_' : #district)) + ':' + #includeDishList",
		hotKeyType = com.luan.takeaway.common.core.cache.HotKeyType.SHOP,
		hotKeyIdExpression = "#province == null || #province.isEmpty() ? 'all' : #province",
		baseTtlSeconds = 1200,
		hotTtlSeconds = 1200,
		nullTtlSeconds = 120,
		lockTtlSeconds = 10,
		retryTimes = 3,
		retrySleepMillis = 50
	)
	List<WmMerchantDTO> listByRegion(String province, String city, String district, boolean includeDishList);

	/**
	 * 根据经纬度查询附近商家列表（带缓存）。
	 * - 【防穿透】空值缓存 2 分钟
	 * - 【防击穿】互斥锁 10 秒
	 */
	@SmartCache(
		name = "merchant:nearby",
		key = "#longitude + ':' + #latitude + ':' + #includeDishList",
		hotKeyType = com.luan.takeaway.common.core.cache.HotKeyType.SHOP,
		hotKeyIdExpression = "#latitude.toString()",
		baseTtlSeconds = 600,
		hotTtlSeconds = 600,
		nullTtlSeconds = 120,
		lockTtlSeconds = 10,
		retryTimes = 3,
		retrySleepMillis = 50
	)
	List<WmMerchantDTO> listByNearby(BigDecimal longitude, BigDecimal latitude, boolean includeDishList);

	/**
	 * 更新营业状态（触发缓存清除）。
	 */
	@SmartCacheEvict(name = "merchant:current", key = "#id")
	@SmartCacheEvict(name = "merchant:page", allEntries = true)
	@SmartCacheEvict(name = "merchant:list", allEntries = true)
	@SmartCacheEvict(name = "merchant:nearby", allEntries = true)
	boolean updateBusinessStatus(Long id, String businessStatus);

}
