package com.pig4cloud.pig.takeaway.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.service.PigUser;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.entity.WmMerchantUserExt;
import com.pig4cloud.pig.takeaway.common.mapper.WmMerchantUserExtMapper;
import com.pig4cloud.pig.takeaway.order.api.OrderApi;
import com.pig4cloud.pig.takeaway.user.dto.WmMerchantDTO;
import com.pig4cloud.pig.takeaway.user.service.WmMerchantService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class WmMerchantServiceImpl implements WmMerchantService {

	private final WmMerchantUserExtMapper wmMerchantUserExtMapper;

	private final OrderApi orderApi;

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
		return result;
	}

	@Override
	public boolean apply(WmMerchantUserExt merchant) {
		merchant.setAuditStatus(TakeawayStatusConstants.Merchant.AUDIT_PENDING);
		if (merchant.getBusinessStatus() == null) {
			merchant.setBusinessStatus(TakeawayStatusConstants.Merchant.BUSINESS_OPEN);
		}
		return wmMerchantUserExtMapper.insert(merchant) > 0;
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
		return wmMerchantUserExtMapper.updateById(merchant) > 0;
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
		return result;
	}

	@Override
	public Page<WmMerchantUserExt> page(Page<WmMerchantUserExt> page, Long userId, String auditStatus, String businessStatus) {
		return wmMerchantUserExtMapper.selectPage(page,
				Wrappers.<WmMerchantUserExt>lambdaQuery()
					.eq(userId != null, WmMerchantUserExt::getUserId, userId)
					.eq(auditStatus != null && !auditStatus.isBlank(), WmMerchantUserExt::getAuditStatus, auditStatus)
					.eq(businessStatus != null && !businessStatus.isBlank(), WmMerchantUserExt::getBusinessStatus,
							businessStatus)
					.orderByDesc(WmMerchantUserExt::getCreateTime));
	}

	@Override
	public boolean updateBusinessStatus(Long id, String businessStatus) {
		WmMerchantUserExt merchant = new WmMerchantUserExt();
		merchant.setId(id);
		merchant.setBusinessStatus(businessStatus);
		return wmMerchantUserExtMapper.updateById(merchant) > 0;
	}

	@Override
	public boolean acceptOrder(Long orderId) {
		R<Boolean> response = orderApi.merchantAccept(orderId);
		if (response == null || response.getCode() != 0) {
			throw new IllegalStateException("商家接单失败");
		}
		return Boolean.TRUE.equals(response.getData());
	}

}
