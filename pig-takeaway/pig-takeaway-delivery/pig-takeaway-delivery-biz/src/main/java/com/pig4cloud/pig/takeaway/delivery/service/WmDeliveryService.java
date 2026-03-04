package com.pig4cloud.pig.takeaway.delivery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.takeaway.common.dto.CreateDeliveryOrderRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryOrder;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryUserExt;

public interface WmDeliveryService {

	boolean registerRider(WmDeliveryUserExt rider);

	boolean createDeliveryOrder(CreateDeliveryOrderRequest request);

	Page<WmDeliveryOrder> pageOrders(Page<WmDeliveryOrder> page, Long deliveryUserId, String status);

	boolean acceptOrder(Long orderId, Long deliveryUserId);

	boolean completeOrder(Long orderId, Long deliveryUserId);

}
