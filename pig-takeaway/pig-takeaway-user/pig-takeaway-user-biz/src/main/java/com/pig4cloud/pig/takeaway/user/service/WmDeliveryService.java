package com.pig4cloud.pig.takeaway.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.takeaway.common.dto.CreateDeliveryOrderRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryOrder;
import com.pig4cloud.pig.takeaway.user.dto.WmDeliveryDTO;

public interface WmDeliveryService {

	WmDeliveryDTO createRider(WmDeliveryDTO riderDTO);

	WmDeliveryDTO current();

	boolean updateRider(WmDeliveryDTO riderDTO);

	boolean createDeliveryOrder(CreateDeliveryOrderRequest request);

	Page<WmDeliveryOrder> pageOrders(Page<WmDeliveryOrder> page, Long deliveryUserId, String status);

}
