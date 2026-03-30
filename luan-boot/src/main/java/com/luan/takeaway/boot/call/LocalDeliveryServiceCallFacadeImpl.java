package com.luan.takeaway.boot.call;

import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.call.DeliveryServiceCallFacade;
import com.luan.takeaway.takeaway.common.dto.CreateDeliveryOrderRequest;
import com.luan.takeaway.takeaway.user.service.WmDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 配送服务本地调用实现（单体模式）
 * <p>
 * 直接注入本地 Service，无网络开销
 *
 * @author luan
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class LocalDeliveryServiceCallFacadeImpl implements DeliveryServiceCallFacade {

	private final WmDeliveryService deliveryService;

	@Override
	public R<Boolean> createDeliveryOrder(CreateDeliveryOrderRequest request) {
		try {
			boolean result = deliveryService.createDeliveryOrder(request);
			return R.ok(result);
		}
		catch (Exception e) {
			log.error("本地创建配送单失败", e);
			return R.failed(e.getMessage());
		}
	}

}
