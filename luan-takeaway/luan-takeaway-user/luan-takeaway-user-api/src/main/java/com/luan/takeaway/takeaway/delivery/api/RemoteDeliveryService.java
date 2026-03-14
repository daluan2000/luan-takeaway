package com.luan.takeaway.takeaway.delivery.api;

import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.api.TakeawayApiConstants;
import com.luan.takeaway.takeaway.common.constant.TakeawayServiceNameConstants;
import com.luan.takeaway.takeaway.common.dto.CreateDeliveryOrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 配送服务 Feign 接口
 *
 * @author pig
 */
@FeignClient(contextId = "remoteDeliveryService", value = TakeawayServiceNameConstants.TAKEAWAY_USER_SERVICE)
public interface RemoteDeliveryService {

	/**
	 * 创建配送单
	 * @param request 创建配送单请求
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.DELIVERY_PATH + "/service/create")
	R<Boolean> createDeliveryOrder(@RequestBody CreateDeliveryOrderRequest request);

}
