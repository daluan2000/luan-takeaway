package com.luan.takeaway.takeaway.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.takeaway.common.dto.CreateDeliveryOrderRequest;
import com.luan.takeaway.takeaway.common.entity.WmDeliveryOrder;
import com.luan.takeaway.takeaway.user.dto.WmDeliveryDTO;

public interface WmDeliveryService {

	WmDeliveryDTO createRider(WmDeliveryDTO riderDTO);

	WmDeliveryDTO current();

	boolean updateRider(WmDeliveryDTO riderDTO);

	boolean createDeliveryOrder(CreateDeliveryOrderRequest request);

	Page<WmDeliveryOrder> pageOrders(Page<WmDeliveryOrder> page, Long deliveryUserId, String status);

}
