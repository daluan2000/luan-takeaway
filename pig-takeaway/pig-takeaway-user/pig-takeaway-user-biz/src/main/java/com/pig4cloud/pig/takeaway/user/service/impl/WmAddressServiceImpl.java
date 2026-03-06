package com.pig4cloud.pig.takeaway.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.common.security.service.PigUser;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.takeaway.common.entity.WmAddress;
import com.pig4cloud.pig.takeaway.common.mapper.WmAddressMapper;
import com.pig4cloud.pig.takeaway.user.service.WmAddressService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class WmAddressServiceImpl implements WmAddressService {

	private final WmAddressMapper wmAddressMapper;

	@Override
	public WmAddress createAddress(WmAddress address) {
		Objects.requireNonNull(address, "地址参数不能为空");
		Long userId = currentUserId();

		WmAddress target = new WmAddress();
		BeanUtils.copyProperties(address, target);
		target.setId(null);
		target.setUserId(userId);

		if (wmAddressMapper.insert(target) <= 0) {
			throw new IllegalStateException("新增地址失败");
		}
		return target;
	}

	@Override
	public boolean updateAddress(WmAddress address) {
		Objects.requireNonNull(address, "地址参数不能为空");
		if (address.getId() == null) {
			throw new IllegalArgumentException("地址ID不能为空");
		}

		WmAddress exist = getOwnedById(address.getId(), currentUserId());

		WmAddress target = new WmAddress();
		BeanUtils.copyProperties(address, target);
		target.setUserId(exist.getUserId());
		return wmAddressMapper.updateById(target) > 0;
	}

	@Override
	public boolean deleteAddress(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("地址ID不能为空");
		}
		getOwnedById(id, currentUserId());
		return wmAddressMapper.deleteById(id) > 0;
	}

	@Override
	public WmAddress getAddress(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("地址ID不能为空");
		}
		return getOwnedById(id, currentUserId());
	}

	@Override
	public List<WmAddress> listCurrentUserAddresses() {
		Long userId = currentUserId();
		return wmAddressMapper.selectList(Wrappers.<WmAddress>lambdaQuery()
			.eq(WmAddress::getUserId, userId)
			.orderByDesc(WmAddress::getCreateTime));
	}

	private WmAddress getOwnedById(Long id, Long userId) {
		WmAddress address = wmAddressMapper.selectById(id);
		if (address == null) {
			throw new IllegalStateException("地址不存在");
		}
		if (!userId.equals(address.getUserId())) {
			throw new IllegalStateException("无权操作该地址");
		}
		return address;
	}

	private Long currentUserId() {
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}
		return currentUser.getId();
	}

}
