package com.luan.takeaway.boot.call;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.call.OrderServiceCallFacade;
import com.luan.takeaway.takeaway.common.dto.OrderDTO;
import com.luan.takeaway.takeaway.common.entity.WmOrder;
import com.luan.takeaway.takeaway.order.api.RemoteOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单服务 Feign 调用实现（微服务模式）
 *
 * @author luan
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteOrderServiceCallFacadeImpl implements OrderServiceCallFacade {

	private final RemoteOrderService orderService;

	@Override
	public R<WmOrder> getById(Long orderId) {
		return orderService.getById(orderId);
	}

	@Override
	public R<Boolean> markPaid(Long orderId) {
		return orderService.markPaid(orderId);
	}

	@Override
	public R<Boolean> merchantAccept(Long orderId) {
		return orderService.merchantAccept(orderId);
	}

	@Override
	public R<Boolean> deliveryStart(Long orderId, Long deliveryUserId) {
		return orderService.deliveryStart(orderId, deliveryUserId);
	}

	@Override
	public R<Boolean> finish(Long orderId) {
		return orderService.finish(orderId);
	}

	@Override
	public R<Page<OrderDTO>> page(long current, long size, Long customerUserId, Long merchantUserId,
			Long deliveryUserId, String status) {
		return orderService.page(current, size, customerUserId, merchantUserId, deliveryUserId, status);
	}

}
