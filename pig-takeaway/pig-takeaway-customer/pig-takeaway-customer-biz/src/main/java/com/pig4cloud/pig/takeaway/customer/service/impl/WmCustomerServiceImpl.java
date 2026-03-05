package com.pig4cloud.pig.takeaway.customer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.common.security.service.PigUser;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.takeaway.common.entity.WmCustomerUserExt;
import com.pig4cloud.pig.takeaway.common.mapper.WmCustomerUserExtMapper;
import com.pig4cloud.pig.takeaway.customer.dto.WmCustomerDTO;
import com.pig4cloud.pig.takeaway.customer.service.WmCustomerService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class WmCustomerServiceImpl implements WmCustomerService {

	private final WmCustomerUserExtMapper wmCustomerUserExtMapper;

	@Override
	public WmCustomerDTO createCustomer(WmCustomerDTO customerDTO) {
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}
		Long userId = currentUser.getId();

		Long existCount = wmCustomerUserExtMapper.selectCount(
				Wrappers.<WmCustomerUserExt>lambdaQuery().eq(WmCustomerUserExt::getUserId, userId));
		if (existCount != null && existCount > 0) {
			throw new IllegalStateException("当前用户已存在客户信息");
		}

		WmCustomerUserExt customer = new WmCustomerUserExt();
		Objects.requireNonNull(customerDTO, "客户参数不能为空");
		BeanUtils.copyProperties(customerDTO, customer);
		customer.setId(null);
		customer.setUserId(userId);

		if (wmCustomerUserExtMapper.insert(customer) <= 0) {
			throw new IllegalStateException("新增客户信息失败");
		}

		WmCustomerDTO result = new WmCustomerDTO();
		BeanUtils.copyProperties(customer, result);
		return result;
	}

	@Override
	public boolean updateCustomer(WmCustomerDTO customerDTO) {
		Objects.requireNonNull(customerDTO, "客户参数不能为空");
		if (customerDTO.getId() == null) {
			throw new IllegalArgumentException("客户ID不能为空");
		}

		WmCustomerUserExt customer = new WmCustomerUserExt();
		BeanUtils.copyProperties(customerDTO, customer);
		return wmCustomerUserExtMapper.updateById(customer) > 0;
	}

}
