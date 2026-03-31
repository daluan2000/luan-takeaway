package com.luan.takeaway.takeaway.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.admin.api.util.ParamResolver;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.security.service.PigUser;
import com.luan.takeaway.common.security.util.SecurityUtils;
import com.luan.takeaway.takeaway.common.call.DishServiceCallFacade;
import com.luan.takeaway.takeaway.common.constant.TakeawayStatusConstants;
import com.luan.takeaway.takeaway.common.entity.WmAddress;
import com.luan.takeaway.takeaway.common.entity.WmMerchantUserExt;
import com.luan.takeaway.takeaway.common.mapper.WmAddressMapper;
import com.luan.takeaway.takeaway.common.mapper.WmMerchantUserExtMapper;
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

import java.math.BigDecimal;
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

	private static final String PARAM_MERCHANT_NEARBY_DISTANCE_KM =
			"TAKEAWAY_MERCHANT_NEARBY_DISTANCE_KM";

	private static final String PARAM_MERCHANT_NEARBY_LAT_LNG_RANGE = "TAKEAWAY_MERCHANT_NEARBY_LAT_LNG_RANGE";

	private static final String MERCHANT_CACHE_VERSION_KEY = "takeaway:merchant:cache:version";

	private final WmMerchantUserExtMapper wmMerchantUserExtMapper;

	private final WmAddressMapper wmAddressMapper;

	private final ObjectMapper objectMapper;

	private final MerchantAuditResultMqPublisher merchantAuditResultMqPublisher;

	private final StringRedisTemplate stringRedisTemplate;

	private final DishServiceCallFacade dishCall;

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
			bumpMerchantCacheVersion();
		}
		return saved;
	}

	private void scheduleAutoApprove(Long merchantId) {
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

			bumpMerchantCacheVersion();

			WmMerchantUserExt merchant = wmMerchantUserExtMapper.selectById(merchantId);
			if (merchant == null || merchant.getUserId() == null) {
				return;
			}

			MerchantAuditResultWsMessage wsMessage = MerchantAuditResultWsMessage.approved(merchantId,
					merchant.getUserId());
			try {
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
		if (merchantAuditResultMqPublisher.publish(merchantId, userId, messageText)) {
			return;
		}
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
		// 缓存由 @SmartCache 注解处理，用户ID从 SecurityUtils 获取
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}
		return loadCurrentByUserId(currentUser.getId());
	}

	@Override
	public Page<WmMerchantDTO> page(Page<WmMerchantDTO> page, Long userId, String auditStatus, String businessStatus,
			boolean includeDishList) {
		// 缓存由 @SmartCache 注解处理
		return loadMerchantPageFromDb(page, userId, auditStatus, businessStatus, includeDishList);
	}

	@Override
	public List<WmMerchantDTO> listByRegion(String province, String city, String district, boolean includeDishList) {
		// 缓存由 @SmartCache 注解处理
		return loadMerchantListByRegionFromDb(province, city, district, includeDishList);
	}

	@Override
	public List<WmMerchantDTO> listByNearby(BigDecimal longitude, BigDecimal latitude, boolean includeDishList) {
		// 缓存由 @SmartCache 注解处理
		if (longitude == null || latitude == null) {
			return Collections.emptyList();
		}
		double[] latLngRange = calculateLatLngRange(longitude.doubleValue(), latitude.doubleValue());
		return loadMerchantListByNearbyFromDb(longitude, latitude, latLngRange, includeDishList);
	}

	/**
	 * 根据经纬度距离上限计算经纬度范围
	 */
	private double[] calculateLatLngRange(double longitude, double latitude) {
		Double distanceKm = ParamResolver.getDouble(PARAM_MERCHANT_NEARBY_DISTANCE_KM, 5.0);
		if (distanceKm == null || distanceKm <= 0) {
			distanceKm = 5.0;
		}

		double latDelta = distanceKm / 111.0;
		double avgLatRad = Math.toRadians(latitude);
		double lngDelta = distanceKm / (111.0 * Math.cos(avgLatRad));

		if (lngDelta > 180) {
			lngDelta = 180;
		}

		return new double[] {
				latitude - latDelta,
				latitude + latDelta,
				longitude - lngDelta,
				longitude + lngDelta
		};
	}

	private List<WmMerchantDTO> loadMerchantListByNearbyFromDb(BigDecimal longitude, BigDecimal latitude,
			double[] latLngRange, boolean includeDishList) {
		double minLat = latLngRange[0];
		double maxLat = latLngRange[1];
		double minLng = latLngRange[2];
		double maxLng = latLngRange[3];

		List<WmAddress> addresses = wmAddressMapper.selectList(Wrappers.<WmAddress>lambdaQuery()
			.ge(WmAddress::getLatitude, minLat)
			.le(WmAddress::getLatitude, maxLat)
			.ge(WmAddress::getLongitude, minLng)
			.le(WmAddress::getLongitude, maxLng));

		if (addresses.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> addressIds = addresses.stream()
			.map(WmAddress::getId)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		if (addressIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<WmMerchantUserExt> merchants = wmMerchantUserExtMapper.selectList(Wrappers.<WmMerchantUserExt>lambdaQuery()
			.in(WmMerchantUserExt::getStoreAddressId, addressIds)
			.orderByDesc(WmMerchantUserExt::getCreateTime));

		List<WmMerchantDTO> result = toDtoListWithAddress(merchants);

		final double userLng = longitude.doubleValue();
		final double userLat = latitude.doubleValue();
		result.sort((m1, m2) -> {
			double dist1 = calculateDistance(userLat, userLng, m1);
			double dist2 = calculateDistance(userLat, userLng, m2);
			return Double.compare(dist1, dist2);
		});

		if (includeDishList) {
			fillDishList(result);
		}
		return result;
	}

	private double calculateDistance(double userLat, double userLng, WmMerchantDTO merchant) {
		WmAddress address = merchant.getAddress();
		if (address == null || address.getLatitude() == null || address.getLongitude() == null) {
			return Double.MAX_VALUE;
		}

		double storeLat = address.getLatitude().doubleValue();
		double storeLng = address.getLongitude().doubleValue();

		double earthRadiusKm = 6371.0;
		double dLat = Math.toRadians(storeLat - userLat);
		double dLng = Math.toRadians(storeLng - userLng);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(storeLat))
				* Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return earthRadiusKm * c;
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
				R<Page<com.luan.takeaway.takeaway.common.entity.WmDish>> response = dishCall.servicePageDish(1, 50,
						null, merchant.getUserId(), TakeawayStatusConstants.Dish.SALE_ON);
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

	private void bumpMerchantCacheVersion() {
		try {
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
