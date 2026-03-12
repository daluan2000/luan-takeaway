package com.luan.takeaway.takeaway.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luan.takeaway.takeaway.common.dto.OrderDTO;
import com.luan.takeaway.takeaway.common.entity.WmOrder;

public interface WmOrderService extends IService<WmOrder> {

	OrderDTO createOrder(OrderDTO request);

	OrderDTO detail(Long orderId);

	Page<OrderDTO> queryPage(Page<OrderDTO> page, Long customerUserId, Long merchantUserId, Long deliveryUserId,
			String status);

	boolean markPaid(Long orderId);

	boolean merchantAccept(Long orderId);

	boolean deliveryStart(Long orderId, Long deliveryUserId);

	boolean finish(Long orderId);

	boolean cancel(Long orderId);

	/**
	 * 自动取消待支付订单。
	 *
	 * 该方法用于延时消息消费者调用，内部应使用“状态条件更新”保证幂等：
	 * 仅当订单仍处于待支付状态时才会更新为已取消。
	 * @param orderId 订单ID
	 * @return true 表示本次确实发生了状态变更；false 表示订单已支付或已非待支付状态
	 */
	boolean autoCancelIfUnpaid(Long orderId);

}
