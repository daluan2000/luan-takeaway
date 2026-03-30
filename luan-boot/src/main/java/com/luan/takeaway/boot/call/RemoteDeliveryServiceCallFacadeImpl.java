package com.luan.takeaway.boot.call;

import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.call.DeliveryServiceCallFacade;
import com.luan.takeaway.takeaway.common.dto.CreateDeliveryOrderRequest;
import com.luan.takeaway.takeaway.delivery.api.RemoteDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 配送服务 Feign 调用实现（微服务模式）
 *
 * @author luan
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteDeliveryServiceCallFacadeImpl implements DeliveryServiceCallFacade {

	private final RemoteDeliveryService deliveryService;

	@Override
	public R<Boolean> createDeliveryOrder(CreateDeliveryOrderRequest request) {
		return deliveryService.createDeliveryOrder(request);
	}

}
