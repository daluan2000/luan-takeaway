package com.pig4cloud.pig.takeaway.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.takeaway.common.dto.CreateOrderRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmOrder;
import com.pig4cloud.pig.takeaway.common.vo.OrderCreateResultVO;
import com.pig4cloud.pig.takeaway.common.vo.OrderDetailVO;

public interface WmOrderService extends IService<WmOrder> {

	OrderCreateResultVO createOrder(CreateOrderRequest request);

	OrderDetailVO detail(Long orderId);

	Page<WmOrder> queryPage(Page<WmOrder> page, Long customerUserId, Long merchantUserId, Long deliveryUserId, String status);

	boolean markPaid(Long orderId);

	boolean merchantAccept(Long orderId);

	boolean deliveryStart(Long orderId, Long deliveryUserId);

	boolean finish(Long orderId);

	boolean cancel(Long orderId);

}
