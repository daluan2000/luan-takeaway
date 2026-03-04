package com.pig4cloud.pig.takeaway.pay.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayStatusConstants;
import com.pig4cloud.pig.takeaway.common.dto.CreateDeliveryOrderRequest;
import com.pig4cloud.pig.takeaway.common.dto.PayRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmOrder;
import com.pig4cloud.pig.takeaway.common.entity.WmOrderPay;
import com.pig4cloud.pig.takeaway.delivery.api.DeliveryApi;
import com.pig4cloud.pig.takeaway.order.api.OrderApi;
import com.pig4cloud.pig.takeaway.pay.mapper.WmOrderPayMapper;
import com.pig4cloud.pig.takeaway.pay.service.WmOrderPayService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@AllArgsConstructor
public class WmOrderPayServiceImpl extends ServiceImpl<WmOrderPayMapper, WmOrderPay> implements WmOrderPayService {

	private final OrderApi orderApi;

	private final DeliveryApi deliveryApi;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean mockPay(PayRequest request) {
		R<WmOrder> orderResp = orderApi.getById(request.getOrderId());
		WmOrder order = unwrap(orderResp, "订单不存在");
		if (order == null) {
			throw new IllegalArgumentException("订单不存在");
		}
		if (!TakeawayStatusConstants.Order.WAIT_PAY.equals(order.getOrderStatus())) {
			throw new IllegalStateException("当前订单状态不可支付");
		}

		WmOrderPay pay = new WmOrderPay();
		pay.setOrderId(order.getId());
		pay.setOrderNo(order.getOrderNo());
		pay.setPayNo("PAY" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000));
		pay.setPayAmount(order.getPayAmount());
		pay.setPayStatus(TakeawayStatusConstants.Pay.SUCCESS);
		pay.setPayChannel(request.getPayChannel() == null ? TakeawayStatusConstants.Pay.CHANNEL_MOCK : request.getPayChannel());
		pay.setPayTime(LocalDateTime.now());
		save(pay);

		R<Boolean> paySuccessResp = orderApi.markPaid(order.getId());
		Boolean marked = unwrap(paySuccessResp, "更新订单支付状态失败");
		if (!Boolean.TRUE.equals(marked)) {
			throw new IllegalStateException("更新订单支付状态失败");
		}

		CreateDeliveryOrderRequest createRequest = new CreateDeliveryOrderRequest();
		createRequest.setOrderId(order.getId());
		createRequest.setOrderNo(order.getOrderNo());
		createRequest.setMerchantUserId(order.getMerchantUserId());
		R<Boolean> deliveryResp = deliveryApi.createDeliveryOrder(createRequest);
		Boolean deliveryCreated = unwrap(deliveryResp, "创建配送单失败");
		if (!Boolean.TRUE.equals(deliveryCreated)) {
			throw new IllegalStateException("创建配送单失败");
		}

		return true;
	}

	@Override
	public Page<WmOrderPay> pageByOrderId(Page<WmOrderPay> page, Long orderId) {
		return page(page,
				Wrappers.<WmOrderPay>lambdaQuery().eq(orderId != null, WmOrderPay::getOrderId, orderId).orderByDesc(WmOrderPay::getCreateTime));
	}

	private <T> T unwrap(R<T> response, String errorMsg) {
		if (response == null || response.getCode() != 0) {
			throw new IllegalStateException(errorMsg);
		}
		return response.getData();
	}

}
