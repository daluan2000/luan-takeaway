package com.pig4cloud.pig.takeaway.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.takeaway.common.api.RemoteTakeawayUserRoleService;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayRoleConstants;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.dto.RoleApplyRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmCustomerUserExt;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryUserExt;
import com.pig4cloud.pig.takeaway.common.entity.WmMerchantUserExt;
import com.pig4cloud.pig.takeaway.common.mapper.WmCustomerUserExtMapper;
import com.pig4cloud.pig.takeaway.common.mapper.WmDeliveryUserExtMapper;
import com.pig4cloud.pig.takeaway.common.mapper.WmMerchantUserExtMapper;
import com.pig4cloud.pig.takeaway.order.service.WmRoleApplyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class WmRoleApplyServiceImpl implements WmRoleApplyService {

	private final WmMerchantUserExtMapper wmMerchantUserExtMapper;

	private final WmDeliveryUserExtMapper wmDeliveryUserExtMapper;

	private final WmCustomerUserExtMapper wmCustomerUserExtMapper;

	private final RemoteTakeawayUserRoleService remoteTakeawayUserRoleService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean apply(RoleApplyRequest request) {
		if (request == null || StrUtil.isBlank(request.getRoleCode())) {
			throw new IllegalArgumentException("角色编码不能为空");
		}

		Long userId = SecurityUtils.getUser().getId();
		String username = SecurityUtils.getUser().getUsername();
		String roleCode = request.getRoleCode().trim();

		switch (roleCode) {
			case TakeawayRoleConstants.ROLE_MERCHANT:
				upsertMerchant(userId, username, request);
				break;
			case TakeawayRoleConstants.ROLE_DELIVERY:
				upsertDelivery(userId, username, request);
				break;
			case TakeawayRoleConstants.ROLE_CUSTOMER:
				upsertCustomer(userId, username, request);
				break;
			default:
				throw new IllegalArgumentException("不支持的角色编码: " + roleCode);
		}

		R<Boolean> response = remoteTakeawayUserRoleService.switchRole(userId, roleCode);
		if (response == null || response.getCode() != 0 || !Boolean.TRUE.equals(response.getData())) {
			throw new IllegalStateException("用户角色切换失败");
		}
		return true;
	}

	private void upsertMerchant(Long userId, String username, RoleApplyRequest request) {
		WmMerchantUserExt current = wmMerchantUserExtMapper
			.selectOne(Wrappers.<WmMerchantUserExt>lambdaQuery().eq(WmMerchantUserExt::getUserId, userId));

		WmMerchantUserExt merchant = new WmMerchantUserExt();
		merchant.setUserId(userId);
		merchant.setMerchantName(request.getMerchantName());
		merchant.setContactName(request.getContactName());
		merchant.setStoreAddressId(request.getStoreAddressId());
		merchant.setBusinessStatus(StrUtil.blankToDefault(request.getBusinessStatus(),
				TakeawayStatusConstants.Merchant.BUSINESS_OPEN));
		merchant.setAuditStatus(StrUtil.blankToDefault(request.getMerchantAuditStatus(),
				TakeawayStatusConstants.Merchant.AUDIT_PENDING));

		if (current == null) {
			merchant.setCreateBy(username);
			wmMerchantUserExtMapper.insert(merchant);
			return;
		}

		merchant.setId(current.getId());
		merchant.setUpdateBy(username);
		wmMerchantUserExtMapper.updateById(merchant);
	}

	private void upsertDelivery(Long userId, String username, RoleApplyRequest request) {
		WmDeliveryUserExt current = wmDeliveryUserExtMapper
			.selectOne(Wrappers.<WmDeliveryUserExt>lambdaQuery().eq(WmDeliveryUserExt::getUserId, userId));

		WmDeliveryUserExt delivery = new WmDeliveryUserExt();
		delivery.setUserId(userId);
		delivery.setRealName(request.getRealName());
		delivery.setDeliveryScopeKm(request.getDeliveryScopeKm());
		delivery.setOnlineStatus(StrUtil.blankToDefault(request.getOnlineStatus(),
				TakeawayStatusConstants.Delivery.ONLINE_OFF));
		delivery.setEmploymentStatus(StrUtil.blankToDefault(request.getEmploymentStatus(),
				TakeawayStatusConstants.Delivery.EMPLOYMENT_ON));

		if (current == null) {
			delivery.setCreateBy(username);
			wmDeliveryUserExtMapper.insert(delivery);
			return;
		}

		delivery.setId(current.getId());
		delivery.setUpdateBy(username);
		wmDeliveryUserExtMapper.updateById(delivery);
	}

	private void upsertCustomer(Long userId, String username, RoleApplyRequest request) {
		WmCustomerUserExt current = wmCustomerUserExtMapper
			.selectOne(Wrappers.<WmCustomerUserExt>lambdaQuery().eq(WmCustomerUserExt::getUserId, userId));

		WmCustomerUserExt customer = new WmCustomerUserExt();
		customer.setUserId(userId);
		customer.setRealName(request.getRealName());
		customer.setDefaultAddressId(request.getDefaultAddressId());

		if (current == null) {
			customer.setCreateBy(username);
			wmCustomerUserExtMapper.insert(customer);
			return;
		}

		customer.setId(current.getId());
		customer.setUpdateBy(username);
		wmCustomerUserExtMapper.updateById(customer);
	}

}
