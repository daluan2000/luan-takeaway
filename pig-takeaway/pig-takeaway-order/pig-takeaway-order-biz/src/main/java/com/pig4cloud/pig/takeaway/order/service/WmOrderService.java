package com.pig4cloud.pig.takeaway.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.takeaway.common.dto.OrderDTO;
import com.pig4cloud.pig.takeaway.common.entity.WmOrder;

public interface WmOrderService extends IService<WmOrder> {

	OrderDTO createOrder(OrderDTO request);

	OrderDTO detail(Long orderId);

	Page<OrderDTO> queryPage(Page<WmOrder> page, Long customerUserId, Long merchantUserId, Long deliveryUserId,
			String status);

	boolean markPaid(Long orderId);

	boolean merchantAccept(Long orderId);

	boolean deliveryStart(Long orderId, Long deliveryUserId);

	boolean finish(Long orderId);

	boolean cancel(Long orderId);

}
