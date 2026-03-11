package com.luan.takeaway.takeaway.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.luan.takeaway.common.security.service.PigUser;
import com.luan.takeaway.common.security.util.SecurityUtils;
import com.luan.takeaway.takeaway.common.entity.WmCustomerUserExt;
import com.luan.takeaway.takeaway.common.mapper.WmCustomerUserExtMapper;
import com.luan.takeaway.takeaway.user.dto.WmCustomerDTO;
import com.luan.takeaway.takeaway.user.service.WmCustomerService;
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

	@Override
	public WmCustomerDTO current() {
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}
		WmCustomerUserExt customer = wmCustomerUserExtMapper
			.selectOne(Wrappers.<WmCustomerUserExt>lambdaQuery().eq(WmCustomerUserExt::getUserId, currentUser.getId()));
		WmCustomerDTO result = new WmCustomerDTO();
		if (customer == null) {
			result.setNoExist(Boolean.TRUE);
			return result;
		}
		BeanUtils.copyProperties(customer, result);
		result.setNoExist(Boolean.FALSE);
		return result;
	}

}
