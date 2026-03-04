package com.pig4cloud.pig.takeaway.delivery.api;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.feign.annotation.NoToken;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayServiceNameConstants;
import com.pig4cloud.pig.takeaway.common.dto.CreateDeliveryOrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "deliveryApi", value = TakeawayServiceNameConstants.TAKEAWAY_DELIVERY_SERVICE)
public interface DeliveryApi {

	@NoToken
	@PostMapping(TakeawayApiConstants.INTERNAL_DELIVERY_PATH + "/create")
	R<Boolean> createDeliveryOrder(@RequestBody CreateDeliveryOrderRequest request);

}
