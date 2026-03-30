package com.luan.takeaway.boot.call;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.call.OrderServiceCallFacade;
import com.luan.takeaway.takeaway.common.dto.OrderDTO;
import com.luan.takeaway.takeaway.common.entity.WmOrder;
import com.luan.takeaway.takeaway.order.service.WmOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 订单服务本地调用实现（单体模式）
 * <p>
 * 直接注入本地 Service，无网络开销
 *
 * @author luan
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class LocalOrderServiceCallFacadeImpl implements OrderServiceCallFacade {

	private final WmOrderService orderService;

	@Override
	public R<WmOrder> getById(Long orderId) {
		try {
			WmOrder result = orderService.getById(orderId);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地查询订单失败", e);
			return R.failed(e.getMessage());
		}
	}

	@Override
	public R<Boolean> markPaid(Long orderId) {
		try {
			boolean result = orderService.markPaid(orderId);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地标记订单已支付失败", e);
			return R.failed(e.getMessage());
		}
	}

	@Override
	public R<Boolean> merchantAccept(Long orderId) {
		try {
			boolean result = orderService.merchantAccept(orderId);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地商家接单失败", e);
			return R.failed(e.getMessage());
		}
	}

	@Override
	public R<Boolean> deliveryStart(Long orderId, Long deliveryUserId) {
		try {
			boolean result = orderService.deliveryStart(orderId, deliveryUserId);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地开始配送失败", e);
			return R.failed(e.getMessage());
		}
	}

	@Override
	public R<Boolean> finish(Long orderId) {
		try {
			boolean result = orderService.finish(orderId);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地完成订单失败", e);
			return R.failed(e.getMessage());
		}
	}

	@Override
	public R<Page<OrderDTO>> page(long current, long size, Long customerUserId, Long merchantUserId,
			Long deliveryUserId, String status) {
		try {
			Page<OrderDTO> page = new Page<>(current, size);
			Page<OrderDTO> result = orderService.queryPage(page, customerUserId, merchantUserId, deliveryUserId, status);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地分页查询订单失败", e);
			return R.failed(e.getMessage());
		}
	}

}
