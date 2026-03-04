package com.pig4cloud.pig.takeaway.merchant.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.entity.WmMerchantUserExt;
import com.pig4cloud.pig.takeaway.order.api.OrderApi;
import com.pig4cloud.pig.takeaway.merchant.mapper.WmMerchantUserExtMapper;
import com.pig4cloud.pig.takeaway.merchant.service.WmMerchantService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WmMerchantServiceImpl implements WmMerchantService {

	private final WmMerchantUserExtMapper wmMerchantUserExtMapper;

	private final OrderApi orderApi;

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
	public boolean updateMerchant(WmMerchantUserExt merchant) {
		return wmMerchantUserExtMapper.updateById(merchant) > 0;
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
