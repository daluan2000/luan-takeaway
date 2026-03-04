package com.pig4cloud.pig.takeaway.order.api;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.feign.annotation.NoToken;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayServiceNameConstants;
import com.pig4cloud.pig.takeaway.common.entity.WmOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(contextId = "orderApi", value = TakeawayServiceNameConstants.TAKEAWAY_ORDER_SERVICE)
public interface OrderApi {

	@NoToken
	@GetMapping(TakeawayApiConstants.INTERNAL_ORDER_PATH + "/{orderId}")
	R<WmOrder> getById(@PathVariable("orderId") Long orderId);

	@NoToken
	@PostMapping(TakeawayApiConstants.INTERNAL_ORDER_PATH + "/{orderId}/pay-success")
	R<Boolean> markPaid(@PathVariable("orderId") Long orderId);

	@NoToken
	@PostMapping(TakeawayApiConstants.INTERNAL_ORDER_PATH + "/{orderId}/merchant-accept")
	R<Boolean> merchantAccept(@PathVariable("orderId") Long orderId);

	@NoToken
	@PostMapping(TakeawayApiConstants.INTERNAL_ORDER_PATH + "/{orderId}/delivery-start/{deliveryUserId}")
	R<Boolean> deliveryStart(@PathVariable("orderId") Long orderId,
			@PathVariable("deliveryUserId") Long deliveryUserId);

	@NoToken
	@PostMapping(TakeawayApiConstants.INTERNAL_ORDER_PATH + "/{orderId}/finish")
	R<Boolean> finish(@PathVariable("orderId") Long orderId);

}
