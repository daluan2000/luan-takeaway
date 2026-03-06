package com.pig4cloud.pig.takeaway.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.security.service.PigUser;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.dto.CreateDeliveryOrderRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryOrder;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryUserExt;
import com.pig4cloud.pig.takeaway.common.mapper.WmDeliveryOrderMapper;
import com.pig4cloud.pig.takeaway.common.mapper.WmDeliveryUserExtMapper;
import com.pig4cloud.pig.takeaway.user.dto.WmDeliveryDTO;
import com.pig4cloud.pig.takeaway.user.service.WmDeliveryService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class WmDeliveryServiceImpl implements WmDeliveryService {

	private final WmDeliveryUserExtMapper wmDeliveryUserExtMapper;

	private final WmDeliveryOrderMapper wmDeliveryOrderMapper;

	@Override
	public WmDeliveryDTO createRider(WmDeliveryDTO riderDTO) {
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}
		Long userId = currentUser.getId();

		Long existCount = wmDeliveryUserExtMapper.selectCount(
				Wrappers.<WmDeliveryUserExt>lambdaQuery().eq(WmDeliveryUserExt::getUserId, userId));
		if (existCount != null && existCount > 0) {
			throw new IllegalStateException("当前用户已存在骑手信息");
		}

		WmDeliveryUserExt rider = new WmDeliveryUserExt();
		Objects.requireNonNull(riderDTO, "骑手参数不能为空");
		BeanUtils.copyProperties(riderDTO, rider);
		rider.setId(null);
		rider.setUserId(userId);
		if (rider.getOnlineStatus() == null) {
			rider.setOnlineStatus(TakeawayStatusConstants.Delivery.ONLINE_OFF);
		}
		if (rider.getEmploymentStatus() == null) {
			rider.setEmploymentStatus(TakeawayStatusConstants.Delivery.EMPLOYMENT_ON);
		}

		if (wmDeliveryUserExtMapper.insert(rider) <= 0) {
			throw new IllegalStateException("新增骑手信息失败");
		}

		WmDeliveryDTO result = new WmDeliveryDTO();
		BeanUtils.copyProperties(rider, result);
		return result;
	}

	@Override
	public WmDeliveryDTO current() {
		PigUser currentUser = SecurityUtils.getUser();
		if (currentUser == null || currentUser.getId() == null) {
			throw new IllegalStateException("当前登录用户不存在");
		}
		WmDeliveryUserExt delivery = wmDeliveryUserExtMapper
			.selectOne(Wrappers.<WmDeliveryUserExt>lambdaQuery().eq(WmDeliveryUserExt::getUserId, currentUser.getId()));
		WmDeliveryDTO result = new WmDeliveryDTO();
		if (delivery == null) {
			result.setNoExist(Boolean.TRUE);
			return result;
		}
		BeanUtils.copyProperties(delivery, result);
		result.setNoExist(Boolean.FALSE);
		return result;
	}

	@Override
	public boolean updateRider(WmDeliveryDTO riderDTO) {
		if (riderDTO.getId() == null) {
			throw new IllegalArgumentException("骑手ID不能为空");
		}
		WmDeliveryUserExt rider = new WmDeliveryUserExt();
		BeanUtils.copyProperties(riderDTO, rider);
		if (rider.getOnlineStatus() == null) {
			rider.setOnlineStatus(TakeawayStatusConstants.Delivery.ONLINE_OFF);
		}
		if (rider.getEmploymentStatus() == null) {
			rider.setEmploymentStatus(TakeawayStatusConstants.Delivery.EMPLOYMENT_ON);
		}
		return wmDeliveryUserExtMapper.updateById(rider) > 0;
	}

	@Override
	public boolean createDeliveryOrder(CreateDeliveryOrderRequest request) {
		WmDeliveryOrder exists = wmDeliveryOrderMapper
			.selectOne(Wrappers.<WmDeliveryOrder>lambdaQuery().eq(WmDeliveryOrder::getOrderId, request.getOrderId()));
		if (exists != null) {
			return true;
		}
		WmDeliveryOrder deliveryOrder = new WmDeliveryOrder();
		deliveryOrder.setOrderId(request.getOrderId());
		deliveryOrder.setOrderNo(request.getOrderNo());
		deliveryOrder.setMerchantUserId(request.getMerchantUserId());
		deliveryOrder.setDeliveryStatus(TakeawayStatusConstants.Delivery.WAIT_ACCEPT);
		return wmDeliveryOrderMapper.insert(deliveryOrder) > 0;
	}

	@Override
	public Page<WmDeliveryOrder> pageOrders(Page<WmDeliveryOrder> page, Long deliveryUserId, String status) {
		return wmDeliveryOrderMapper.selectPage(page,
				Wrappers.<WmDeliveryOrder>lambdaQuery()
					.eq(deliveryUserId != null, WmDeliveryOrder::getDeliveryUserId, deliveryUserId)
					.eq(status != null && !status.isBlank(), WmDeliveryOrder::getDeliveryStatus, status)
					.orderByDesc(WmDeliveryOrder::getCreateTime));
	}

}
