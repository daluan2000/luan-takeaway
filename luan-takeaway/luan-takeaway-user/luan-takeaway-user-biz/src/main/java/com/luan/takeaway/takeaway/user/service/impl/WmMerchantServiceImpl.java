package com.luan.takeaway.takeaway.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.admin.api.util.ParamResolver;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.security.service.PigUser;
import com.luan.takeaway.common.security.util.SecurityUtils;
import com.luan.takeaway.takeaway.common.cache.RedisSafeCacheService;
import com.luan.takeaway.takeaway.common.constant.TakeawayStatusConstants;
import com.luan.takeaway.takeaway.common.entity.WmAddress;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import com.luan.takeaway.takeaway.common.entity.WmMerchantUserExt;
import com.luan.takeaway.takeaway.common.mapper.WmAddressMapper;
import com.luan.takeaway.takeaway.common.mapper.WmMerchantUserExtMapper;
import com.luan.takeaway.takeaway.dish.api.RemoteDishService;
import com.luan.takeaway.takeaway.user.dto.WmMerchantDTO;
import com.luan.takeaway.takeaway.user.dto.ws.MerchantAuditResultWsMessage;
import com.luan.takeaway.takeaway.user.message.MerchantAuditResultMqPublisher;
import com.luan.takeaway.takeaway.user.service.WmMerchantService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class WmMerchantServiceImpl implements WmMerchantService {

	private static final String PARAM_MERCHANT_AUTO_AUDIT_DELAY_MIN_SECONDS =
			"TAKEAWAY_MERCHANT_AUTO_AUDIT_DELAY_MIN_SECONDS";

	private static final String PARAM_MERCHANT_AUTO_AUDIT_DELAY_MAX_SECONDS =
			"TAKEAWAY_MERCHANT_AUTO_AUDIT_DELAY_MAX_SECONDS";

	// 商户缓存版本号，写操作后递增，用于让老的查询缓存“逻辑过期”。
	private static final String MERCHANT_CACHE_VERSION_KEY = "takeaway:merchant:cache:version";

	// 当前登录用户的商户信息缓存 key 前缀。
	private static final String MERCHANT_CURRENT_CACHE_KEY_PREFIX = "takeaway:merchant:current:";

	// 【防击穿】当前用户商户信息锁 key 前缀。
	private static final String MERCHANT_CURRENT_LOCK_KEY_PREFIX = "takeaway:merchant:current:lock:";

	// 地域列表查询缓存 key 前缀。
	private static final String MERCHANT_LIST_CACHE_KEY_PREFIX = "takeaway:merchant:list:";

	// 【防击穿】地域列表查询锁 key 前缀。
	private static final String MERCHANT_LIST_LOCK_KEY_PREFIX = "takeaway:merchant:list:lock:";

	// 分页查询缓存 key 前缀。
	private static final String MERCHANT_PAGE_CACHE_KEY_PREFIX = "takeaway:merchant:page:";

	// 【防击穿】分页查询锁 key 前缀。
	private static final String MERCHANT_PAGE_LOCK_KEY_PREFIX = "takeaway:merchant:page:lock:";

	// 商户缓存统一策略（这套策略会在个体/分页/列表查询里复用）：
	// - 【防雪崩】基础 TTL 20 分钟 + 抖动 5 分钟
	// - 【防穿透】空值缓存 2 分钟
	// - 【防击穿】锁 10 秒 + 重试 3 次
	private static final RedisSafeCacheService.CachePolicy MERCHANT_CACHE_POLICY = RedisSafeCacheService.CachePolicy
		.of(20 * 60, 5 * 60, 2 * 60, 10, 3, 50);

	private final WmMerchantUserExtMapper wmMerchantUserExtMapper;

	private final WmAddressMapper wmAddressMapper;

	private final ObjectMapper objectMapper;

	private final MerchantAuditResultMqPublisher merchantAuditResultMqPublisher;

	private final StringRedisTemplate stringRedisTemplate;

	private final RedisSafeCacheService redisSafeCacheService;

	private final RemoteDishService dishApi;

	@Override
	public WmMerchantDTO createMerchant(WmMerchantDTO merchantDTO) {
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}
		Long userId = currentUser.getId();

		Long existCount = wmMerchantUserExtMapper
			.selectCount(Wrappers.<WmMerchantUserExt>lambdaQuery().eq(WmMerchantUserExt::getUserId, userId));
		if (existCount != null && existCount > 0) {
			throw new IllegalStateException("当前用户已存在商家信息");
		}

		WmMerchantUserExt merchant = new WmMerchantUserExt();
		Objects.requireNonNull(merchantDTO, "商家参数不能为空");
		BeanUtils.copyProperties(merchantDTO, merchant);
		merchant.setId(null);
		merchant.setUserId(userId);
		if (merchant.getAuditStatus() == null) {
			merchant.setAuditStatus(TakeawayStatusConstants.Merchant.AUDIT_PENDING);
		}
		if (merchant.getBusinessStatus() == null) {
			merchant.setBusinessStatus(TakeawayStatusConstants.Merchant.BUSINESS_OPEN);
		}

		if (wmMerchantUserExtMapper.insert(merchant) <= 0) {
			throw new IllegalStateException("新增商家信息失败");
		}

		WmMerchantDTO result = new WmMerchantDTO();
		BeanUtils.copyProperties(merchant, result);
		fillAddress(result);
		bumpMerchantCacheVersion();
		return result;
	}

	@Override
	public boolean apply(WmMerchantDTO merchantDTO) {
		Objects.requireNonNull(merchantDTO, "商家参数不能为空");
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}
		WmMerchantUserExt merchant = new WmMerchantUserExt();
		BeanUtils.copyProperties(merchantDTO, merchant);
		merchant.setUserId(currentUser.getId());
		merchant.setId(null);
		merchant.setAuditStatus(TakeawayStatusConstants.Merchant.AUDIT_PENDING);
		if (merchant.getBusinessStatus() == null) {
			merchant.setBusinessStatus(TakeawayStatusConstants.Merchant.BUSINESS_OPEN);
		}

		boolean saved = wmMerchantUserExtMapper.insert(merchant) > 0;
		if (saved && merchant.getId() != null) {
			scheduleAutoApprove(merchant.getId());
			// 新增商户后提升缓存版本，避免旧列表继续返回过期数据。
			bumpMerchantCacheVersion();
		}
		return saved;
	}

	private void scheduleAutoApprove(Long merchantId) {
		// 审核通过这里使用异步延时，主要是模拟真实平台“审核中 -> 审核结果”的时间差。
		// 这样前端更容易覆盖待审核态，也能提前暴露通知链路（MQ/WS）在异步场景下的问题。
		int[] delayRange = getAutoAuditDelayRangeSeconds();
		int delaySeconds = ThreadLocalRandom.current().nextInt(delayRange[0], delayRange[1] + 1);
		CompletableFuture.runAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(delaySeconds);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}

			int updatedRows = wmMerchantUserExtMapper.update(null,
					Wrappers.<WmMerchantUserExt>lambdaUpdate()
						.set(WmMerchantUserExt::getAuditStatus, TakeawayStatusConstants.Merchant.AUDIT_APPROVED)
						.eq(WmMerchantUserExt::getId, merchantId)
						.eq(WmMerchantUserExt::getAuditStatus, TakeawayStatusConstants.Merchant.AUDIT_PENDING));

			if (updatedRows <= 0) {
				return;
			}

			// 自动审核通过后同样会改变查询结果，必须刷新版本。
			bumpMerchantCacheVersion();

			WmMerchantUserExt merchant = wmMerchantUserExtMapper.selectById(merchantId);
			if (merchant == null || merchant.getUserId() == null) {
				return;
			}

			MerchantAuditResultWsMessage wsMessage = MerchantAuditResultWsMessage.approved(merchantId,
					merchant.getUserId());
			try {
				// 业务消息先序列化为字符串，再作为 MQ 的 messageText。
				// upms 侧消费后会继续按统一的 WsPushMessageDTO 协议下发给指定会话。
				String messageText = objectMapper.writeValueAsString(wsMessage);
				pushAuditResultByMq(merchantId, merchant.getUserId(), messageText);
			}
			catch (JsonProcessingException e) {
				log.error("商家审核结果消息序列化失败, merchantId={}", merchantId, e);
			}
			catch (Exception e) {
				log.error("商家审核结果消息推送异常, merchantId={}, userId={}", merchantId, merchant.getUserId(), e);
			}
		});
	}

	private void pushAuditResultByMq(Long merchantId, Long userId, String messageText) {
		// 这里明确只走 MQ：业务服务只负责“发布事件”，不直接承担 websocket 推送职责。
		// 这样可以把推送能力收敛到 upms，避免多个服务各自维护 ws 连接和推送细节。
		if (merchantAuditResultMqPublisher.publish(merchantId, userId, messageText)) {
			return;
		}
		// 当前策略是不再走 Feign 直推兜底，发送失败只记录日志，方便后续做统一重试/补偿。
		log.warn("商家审核结果MQ发送失败, merchantId={}, userId={}", merchantId, userId);
	}

	@Override
	public boolean audit(Long id, String auditStatus) {
		WmMerchantUserExt merchant = new WmMerchantUserExt();
		merchant.setId(id);
		merchant.setAuditStatus(auditStatus);
		boolean updated = wmMerchantUserExtMapper.updateById(merchant) > 0;
		if (updated) {
			bumpMerchantCacheVersion();
		}
		return updated;
	}

	@Override
	public boolean updateMerchant(WmMerchantDTO merchantDTO) {
		if (merchantDTO.getId() == null) {
			throw new IllegalArgumentException("商家ID不能为空");
		}
		WmMerchantUserExt merchant = new WmMerchantUserExt();
		BeanUtils.copyProperties(merchantDTO, merchant);
		merchant.setAuditStatus(TakeawayStatusConstants.Merchant.AUDIT_PENDING);
		boolean updated = wmMerchantUserExtMapper.updateById(merchant) > 0;
		if (updated) {
			scheduleAutoApprove(merchantDTO.getId());
			bumpMerchantCacheVersion();
		}
		return updated;
	}

	@Override
	public WmMerchantDTO current() {
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}

		long version = getMerchantCacheVersion();
		String cacheKey = MERCHANT_CURRENT_CACHE_KEY_PREFIX + version + ":" + currentUser.getId();
		String lockKey = MERCHANT_CURRENT_LOCK_KEY_PREFIX + version + ":" + currentUser.getId();

		// 当前用户商户个体查询：直接复用统一组件，三件事都一起兜住
		// 【防穿透】空值缓存、【防击穿】互斥锁、【防雪崩】随机 TTL。
		return redisSafeCacheService.queryWithProtect(cacheKey, lockKey, WmMerchantDTO.class,
				() -> loadCurrentByUserId(currentUser.getId()), MERCHANT_CACHE_POLICY);
	}

	@Override
	public Page<WmMerchantDTO> page(Page<WmMerchantDTO> page, Long userId, String auditStatus, String businessStatus,
			boolean includeDishList) {
		long version = getMerchantCacheVersion();
		String safeUserId = userId == null ? "_" : String.valueOf(userId);
		String safeAuditStatus = StringUtils.hasText(auditStatus) ? auditStatus.trim() : "_";
		String safeBusinessStatus = StringUtils.hasText(businessStatus) ? businessStatus.trim() : "_";
		String safeIncludeDishList = includeDishList ? "1" : "0";

		String cacheKey = MERCHANT_PAGE_CACHE_KEY_PREFIX + version + ":" + page.getCurrent() + ":" + page.getSize() + ":"
				+ safeUserId + ":" + safeAuditStatus + ":" + safeBusinessStatus + ":" + safeIncludeDishList;
		String lockKey = MERCHANT_PAGE_LOCK_KEY_PREFIX + cacheKey;

		JavaType pageType = objectMapper.getTypeFactory().constructParametricType(Page.class, WmMerchantDTO.class);
		// 分页查询同样走统一缓存保护，避免同条件请求高并发时把 DB 挤爆。
		return redisSafeCacheService.queryWithProtect(cacheKey, lockKey, pageType,
				() -> loadMerchantPageFromDb(page, userId, auditStatus, businessStatus, includeDishList),
				MERCHANT_CACHE_POLICY);
	}

	@Override
	public List<WmMerchantDTO> listByRegion(String province, String city, String district, boolean includeDishList) {
		long version = getMerchantCacheVersion();
		String safeProvince = StringUtils.hasText(province) ? province.trim() : "_";
		String safeCity = StringUtils.hasText(city) ? city.trim() : "_";
		String safeDistrict = StringUtils.hasText(district) ? district.trim() : "_";
		String safeIncludeDishList = includeDishList ? "1" : "0";

		String cacheKey = MERCHANT_LIST_CACHE_KEY_PREFIX + version + ":" + safeProvince + ":" + safeCity + ":"
				+ safeDistrict + ":" + safeIncludeDishList;
		String lockKey = MERCHANT_LIST_LOCK_KEY_PREFIX + cacheKey;

		JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, WmMerchantDTO.class);
		// 地域列表查询也复用统一组件，和个体/分页一套规则。
		return redisSafeCacheService.queryWithProtect(cacheKey, lockKey, listType,
				() -> loadMerchantListByRegionFromDb(province, city, district, includeDishList), MERCHANT_CACHE_POLICY);
	}

	private List<WmMerchantDTO> toDtoListWithAddress(List<WmMerchantUserExt> merchants) {
		if (merchants == null || merchants.isEmpty()) {
			return Collections.emptyList();
		}
		List<Long> addressIds = merchants.stream()
			.map(WmMerchantUserExt::getStoreAddressId)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());

		Map<Long, WmAddress> addressMap = new HashMap<>();
		if (!addressIds.isEmpty()) {
			addressMap = wmAddressMapper
				.selectList(Wrappers.<WmAddress>lambdaQuery().in(WmAddress::getId, addressIds))
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(WmAddress::getId, item -> item, (left, right) -> left, HashMap::new));
		}

		Map<Long, WmAddress> finalAddressMap = addressMap;
		List<WmMerchantDTO> result = new java.util.ArrayList<>();
		for (WmMerchantUserExt merchant : merchants) {
			if (merchant == null) {
				continue;
			}
			WmMerchantDTO dto = new WmMerchantDTO();
			BeanUtils.copyProperties(merchant, dto);
			if (merchant.getStoreAddressId() != null) {
				dto.setAddress(finalAddressMap.get(merchant.getStoreAddressId()));
			}
			result.add(dto);
		}
		return result;
	}

	private void fillAddress(WmMerchantDTO dto) {
		if (dto == null || dto.getStoreAddressId() == null) {
			return;
		}
		dto.setAddress(wmAddressMapper.selectById(dto.getStoreAddressId()));
	}

	@Override
	public boolean updateBusinessStatus(Long id, String businessStatus) {
		WmMerchantUserExt merchant = new WmMerchantUserExt();
		merchant.setId(id);
		merchant.setBusinessStatus(businessStatus);
		boolean updated = wmMerchantUserExtMapper.updateById(merchant) > 0;
		if (updated) {
			bumpMerchantCacheVersion();
		}
		return updated;
	}

	private WmMerchantDTO loadCurrentByUserId(Long userId) {
		WmMerchantUserExt merchant = wmMerchantUserExtMapper
			.selectOne(Wrappers.<WmMerchantUserExt>lambdaQuery().eq(WmMerchantUserExt::getUserId, userId));
		WmMerchantDTO result = new WmMerchantDTO();
		if (merchant == null) {
			result.setNoExist(Boolean.TRUE);
			return result;
		}
		BeanUtils.copyProperties(merchant, result);
		result.setNoExist(Boolean.FALSE);
		fillAddress(result);
		return result;
	}

	private Page<WmMerchantDTO> loadMerchantPageFromDb(Page<WmMerchantDTO> page, Long userId, String auditStatus,
			String businessStatus, boolean includeDishList) {
		Page<WmMerchantUserExt> entityPage = wmMerchantUserExtMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()),
				Wrappers.<WmMerchantUserExt>lambdaQuery()
					.eq(userId != null, WmMerchantUserExt::getUserId, userId)
					.eq(auditStatus != null && !auditStatus.isBlank(), WmMerchantUserExt::getAuditStatus, auditStatus)
					.eq(businessStatus != null && !businessStatus.isBlank(), WmMerchantUserExt::getBusinessStatus,
							businessStatus)
					.orderByDesc(WmMerchantUserExt::getCreateTime));

		List<WmMerchantDTO> dtoList = toDtoListWithAddress(entityPage.getRecords());
		if (includeDishList) {
			fillDishList(dtoList);
		}
		Page<WmMerchantDTO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
		result.setRecords(dtoList);
		return result;
	}

	private List<WmMerchantDTO> loadMerchantListByRegionFromDb(String province, String city, String district,
			boolean includeDishList) {
		List<WmMerchantDTO> result;
		if (!StringUtils.hasText(province)) {
			List<WmMerchantUserExt> merchants = wmMerchantUserExtMapper
				.selectList(Wrappers.<WmMerchantUserExt>lambdaQuery().orderByDesc(WmMerchantUserExt::getCreateTime));
			result = toDtoListWithAddress(merchants);
			if (includeDishList) {
				fillDishList(result);
			}
			return result;
		}

		List<WmAddress> addresses = wmAddressMapper.selectList(Wrappers.<WmAddress>lambdaQuery()
			.eq(WmAddress::getProvince, province)
			.eq(StringUtils.hasText(city), WmAddress::getCity, city)
			.eq(StringUtils.hasText(city) && StringUtils.hasText(district), WmAddress::getDistrict, district));

		if (addresses.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> addressIds = addresses.stream().map(WmAddress::getId).filter(Objects::nonNull).collect(Collectors.toList());
		if (addressIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<WmMerchantUserExt> merchants = wmMerchantUserExtMapper.selectList(Wrappers.<WmMerchantUserExt>lambdaQuery()
			.in(WmMerchantUserExt::getStoreAddressId, addressIds)
			.orderByDesc(WmMerchantUserExt::getCreateTime));
		result = toDtoListWithAddress(merchants);
		if (includeDishList) {
			fillDishList(result);
		}
		return result;
	}

	private void fillDishList(List<WmMerchantDTO> merchants) {
		if (merchants == null || merchants.isEmpty()) {
			return;
		}
		for (WmMerchantDTO merchant : merchants) {
			if (merchant == null || merchant.getUserId() == null) {
				continue;
			}
			try {
				R<Page<WmDish>> response = dishApi.page(1, 50, null, merchant.getUserId(),
						TakeawayStatusConstants.Dish.SALE_ON);
				if (response != null && response.getData() != null && response.getData().getRecords() != null) {
					merchant.setDishList(response.getData().getRecords());
				}
				else {
					merchant.setDishList(Collections.emptyList());
				}
			}
			catch (Exception ex) {
				log.warn("加载商家菜品列表失败, merchantUserId={}", merchant.getUserId(), ex);
				merchant.setDishList(Collections.emptyList());
			}
		}
	}

	private long getMerchantCacheVersion() {
		// 版本号缺失/异常时退化为 0，确保服务可用。
		String value = stringRedisTemplate.opsForValue().get(MERCHANT_CACHE_VERSION_KEY);
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

	private void bumpMerchantCacheVersion() {
		try {
			// 原子自增，配合“版本入 key”实现无扫描失效。
			stringRedisTemplate.opsForValue().increment(MERCHANT_CACHE_VERSION_KEY);
		}
		catch (Exception ex) {
			log.warn("更新商户缓存版本号失败", ex);
		}
	}

	private int[] getAutoAuditDelayRangeSeconds() {
		Long minVal = ParamResolver.getLong(PARAM_MERCHANT_AUTO_AUDIT_DELAY_MIN_SECONDS, 3L);
		Long maxVal = ParamResolver.getLong(PARAM_MERCHANT_AUTO_AUDIT_DELAY_MAX_SECONDS, 10L);

		int min = minVal == null ? 3 : minVal.intValue();
		int max = maxVal == null ? 10 : maxVal.intValue();

		if (min <= 0) {
			min = 3;
		}
		if (max <= 0) {
			max = 10;
		}
		if (max < min) {
			max = min;
		}
		if (max > 300) {
			max = 300;
		}
		return new int[] { min, max };
	}

}
