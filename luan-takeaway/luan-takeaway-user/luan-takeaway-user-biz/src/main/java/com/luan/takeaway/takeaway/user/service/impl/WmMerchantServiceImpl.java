package com.luan.takeaway.takeaway.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.admin.api.dto.WsPushMessageDTO;
import com.luan.takeaway.admin.api.feign.RemoteWsPushService;
import com.luan.takeaway.takeaway.user.dto.ws.MerchantAuditResultWsMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.common.core.constant.CommonConstants;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.security.service.PigUser;
import com.luan.takeaway.common.security.util.SecurityUtils;
import com.luan.takeaway.takeaway.common.constant.TakeawayStatusConstants;
import com.luan.takeaway.takeaway.common.entity.WmAddress;
import com.luan.takeaway.takeaway.common.entity.WmMerchantUserExt;
import com.luan.takeaway.takeaway.common.mapper.WmAddressMapper;
import com.luan.takeaway.takeaway.common.mapper.WmMerchantUserExtMapper;
import com.luan.takeaway.takeaway.user.dto.WmMerchantDTO;
import com.luan.takeaway.takeaway.user.message.MerchantAuditResultMqPublisher;
import com.luan.takeaway.takeaway.user.service.WmMerchantService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

	private final WmMerchantUserExtMapper wmMerchantUserExtMapper;

	private final WmAddressMapper wmAddressMapper;

	private final RemoteWsPushService remoteWsPushService;

	private final ObjectMapper objectMapper;

	private final MerchantAuditResultMqPublisher merchantAuditResultMqPublisher;

	@Override
	public WmMerchantDTO createMerchant(WmMerchantDTO merchantDTO) {
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}
		Long userId = currentUser.getId();

		Long existCount = wmMerchantUserExtMapper.selectCount(
				Wrappers.<WmMerchantUserExt>lambdaQuery().eq(WmMerchantUserExt::getUserId, userId));
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
		}
		return saved;
	}

	private void scheduleAutoApprove(Long merchantId) {
		int delaySeconds = ThreadLocalRandom.current().nextInt(3, 11);
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

			WmMerchantUserExt merchant = wmMerchantUserExtMapper.selectById(merchantId);
			if (merchant == null || merchant.getUserId() == null) {
				return;
			}

			MerchantAuditResultWsMessage wsMessage = MerchantAuditResultWsMessage.approved(merchantId,
					merchant.getUserId());
			try {
				String messageText = objectMapper.writeValueAsString(wsMessage);
				pushAuditResultWithRetry(merchantId, merchant.getUserId(), messageText);
			}
			catch (JsonProcessingException e) {
				log.error("商家审核结果消息序列化失败, merchantId={}", merchantId, e);
			}
			catch (Exception e) {
				log.error("商家审核结果消息推送异常, merchantId={}, userId={}", merchantId, merchant.getUserId(), e);
			}
		});
	}

	private void pushAuditResultWithRetry(Long merchantId, Long userId, String messageText) {
		if (merchantAuditResultMqPublisher.publish(merchantId, userId, messageText)) {
			return;
		}

		log.info("商家审核结果MQ未发送成功，回退为直连推送, merchantId={}, userId={}", merchantId, userId);

		WsPushMessageDTO pushMessageDTO = new WsPushMessageDTO().setMessageText(messageText)
			.setSessionKeys(Collections.singletonList(String.valueOf(userId)));

		try {
			R<Boolean> response = remoteWsPushService.push(pushMessageDTO);
			if (isPushSuccess(response)) {
				log.info("商家审核结果消息推送成功, merchantId={}, userId={}", merchantId, userId);
				return;
			}
			log.warn("商家审核结果消息推送失败, merchantId={}, userId={}, response={}", merchantId, userId, response);
		}
		catch (Exception e) {
			log.warn("商家审核结果消息推送异常, merchantId={}, userId={}, message={}", merchantId, userId, e.getMessage());
		}
	}

	private boolean isPushSuccess(R<Boolean> response) {
		return response != null && response.getCode() == CommonConstants.SUCCESS
				&& Boolean.TRUE.equals(response.getData());
	}

	@Override
	public boolean audit(Long id, String auditStatus) {
		WmMerchantUserExt merchant = new WmMerchantUserExt();
		merchant.setId(id);
		merchant.setAuditStatus(auditStatus);
		return wmMerchantUserExtMapper.updateById(merchant) > 0;
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
		}
		return updated;
	}

	@Override
	public WmMerchantDTO current() {
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}
		WmMerchantUserExt merchant = wmMerchantUserExtMapper
			.selectOne(Wrappers.<WmMerchantUserExt>lambdaQuery().eq(WmMerchantUserExt::getUserId, currentUser.getId()));
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

	@Override
	public Page<WmMerchantDTO> page(Page<WmMerchantDTO> page, Long userId, String auditStatus, String businessStatus) {
		Page<WmMerchantUserExt> entityPage = wmMerchantUserExtMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()),
				Wrappers.<WmMerchantUserExt>lambdaQuery()
					.eq(userId != null, WmMerchantUserExt::getUserId, userId)
					.eq(auditStatus != null && !auditStatus.isBlank(), WmMerchantUserExt::getAuditStatus, auditStatus)
					.eq(businessStatus != null && !businessStatus.isBlank(), WmMerchantUserExt::getBusinessStatus,
							businessStatus)
					.orderByDesc(WmMerchantUserExt::getCreateTime));

		List<WmMerchantDTO> dtoList = toDtoListWithAddress(entityPage.getRecords());
		Page<WmMerchantDTO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
		result.setRecords(dtoList);
		return result;
	}

	@Override
	public List<WmMerchantDTO> listByRegion(String province, String city, String district) {
		if (!StringUtils.hasText(province)) {
			List<WmMerchantUserExt> merchants = wmMerchantUserExtMapper.selectList(
					Wrappers.<WmMerchantUserExt>lambdaQuery().orderByDesc(WmMerchantUserExt::getCreateTime));
			return toDtoListWithAddress(merchants);
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
		return toDtoListWithAddress(merchants);
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
		return wmMerchantUserExtMapper.updateById(merchant) > 0;
	}

}
