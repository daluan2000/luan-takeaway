package com.pig4cloud.pig.takeaway.order.api;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.common.constant.TakeawayServiceNameConstants;
import com.pig4cloud.pig.takeaway.common.entity.WmOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 订单服务 Feign 接口
 *
 * @author pig
 */
@FeignClient(contextId = "orderApi", value = TakeawayServiceNameConstants.TAKEAWAY_ORDER_SERVICE)
public interface OrderApi {

	/**
	 * 按ID查询订单
	 * @param orderId 订单ID
	 * @return 订单信息
	 */
	@GetMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}")
	R<WmOrder> getById(@PathVariable("orderId") Long orderId);

	/**
	 * 标记订单已支付
	 * @param orderId 订单ID
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/pay-success")
	R<Boolean> markPaid(@PathVariable("orderId") Long orderId);

	/**
	 * 商家接单
	 * @param orderId 订单ID
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/merchant-accept")
	R<Boolean> merchantAccept(@PathVariable("orderId") Long orderId);

	/**
	 * 开始配送
	 * @param orderId 订单ID
	 * @param deliveryUserId 配送员用户ID
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/delivery-start/{deliveryUserId}")
	R<Boolean> deliveryStart(@PathVariable("orderId") Long orderId,
			@PathVariable("deliveryUserId") Long deliveryUserId);

	/**
	 * 完成订单
	 * @param orderId 订单ID
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.ORDER_PATH + "/service/{orderId}/finish")
	R<Boolean> finish(@PathVariable("orderId") Long orderId);

}
