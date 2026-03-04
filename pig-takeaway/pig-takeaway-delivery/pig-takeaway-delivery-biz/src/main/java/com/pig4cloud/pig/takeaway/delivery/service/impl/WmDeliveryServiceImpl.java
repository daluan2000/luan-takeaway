package com.pig4cloud.pig.takeaway.delivery.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.dto.CreateDeliveryOrderRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryOrder;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryUserExt;
import com.pig4cloud.pig.takeaway.order.api.OrderApi;
import com.pig4cloud.pig.takeaway.delivery.mapper.WmDeliveryOrderMapper;
import com.pig4cloud.pig.takeaway.delivery.mapper.WmDeliveryUserExtMapper;
import com.pig4cloud.pig.takeaway.delivery.service.WmDeliveryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class WmDeliveryServiceImpl implements WmDeliveryService {

	private final WmDeliveryUserExtMapper wmDeliveryUserExtMapper;

	private final WmDeliveryOrderMapper wmDeliveryOrderMapper;

	private final OrderApi orderApi;

	@Override
	public boolean registerRider(WmDeliveryUserExt rider) {
		if (rider.getOnlineStatus() == null) {
			rider.setOnlineStatus(TakeawayStatusConstants.Delivery.ONLINE_OFF);
		}
		if (rider.getEmploymentStatus() == null) {
			rider.setEmploymentStatus(TakeawayStatusConstants.Delivery.EMPLOYMENT_ON);
		}
		if (rider.getId() == null) {
			return wmDeliveryUserExtMapper.insert(rider) > 0;
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

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean acceptOrder(Long orderId, Long deliveryUserId) {
		WmDeliveryOrder deliveryOrder = wmDeliveryOrderMapper
			.selectOne(Wrappers.<WmDeliveryOrder>lambdaQuery().eq(WmDeliveryOrder::getOrderId, orderId));
		if (deliveryOrder == null) {
			throw new IllegalArgumentException("配送单不存在");
		}
		if (!TakeawayStatusConstants.Delivery.WAIT_ACCEPT.equals(deliveryOrder.getDeliveryStatus())) {
			throw new IllegalStateException("配送单非待接单状态");
		}

		R<Boolean> resp = orderApi.deliveryStart(orderId, deliveryUserId);
		Boolean started = unwrap(resp, "订单更新配送中失败");
		if (!Boolean.TRUE.equals(started)) {
			throw new IllegalStateException("订单更新配送中失败");
		}

		deliveryOrder.setDeliveryUserId(deliveryUserId);
		deliveryOrder.setDeliveryStatus(TakeawayStatusConstants.Delivery.DELIVERING);
		deliveryOrder.setAcceptTime(LocalDateTime.now());
		deliveryOrder.setPickupTime(LocalDateTime.now());
		return wmDeliveryOrderMapper.updateById(deliveryOrder) > 0;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean completeOrder(Long orderId, Long deliveryUserId) {
		WmDeliveryOrder deliveryOrder = wmDeliveryOrderMapper
			.selectOne(Wrappers.<WmDeliveryOrder>lambdaQuery().eq(WmDeliveryOrder::getOrderId, orderId));
		if (deliveryOrder == null) {
			throw new IllegalArgumentException("配送单不存在");
		}
		if (!TakeawayStatusConstants.Delivery.DELIVERING.equals(deliveryOrder.getDeliveryStatus())) {
			throw new IllegalStateException("配送单非配送中状态");
		}
		if (deliveryOrder.getDeliveryUserId() == null || !deliveryOrder.getDeliveryUserId().equals(deliveryUserId)) {
			throw new IllegalStateException("仅接单骑手可完成配送");
		}

		R<Boolean> finishResp = orderApi.finish(orderId);
		Boolean finished = unwrap(finishResp, "订单完成失败");
		if (!Boolean.TRUE.equals(finished)) {
			throw new IllegalStateException("订单完成失败");
		}

		deliveryOrder.setDeliveryStatus(TakeawayStatusConstants.Delivery.ARRIVED);
		deliveryOrder.setDeliveredTime(LocalDateTime.now());
		return wmDeliveryOrderMapper.updateById(deliveryOrder) > 0;
	}

	private <T> T unwrap(R<T> response, String errorMsg) {
		if (response == null || response.getCode() != 0) {
			throw new IllegalStateException(errorMsg);
		}
		return response.getData();
	}

}
