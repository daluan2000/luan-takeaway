package com.luan.takeaway.takeaway.common.dto;

import com.luan.takeaway.takeaway.common.entity.WmOrder;
import com.luan.takeaway.takeaway.common.entity.WmOrderItem;
import com.luan.takeaway.takeaway.common.entity.WmAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单传输对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单传输对象")
public class OrderDTO extends WmOrder {

	@Schema(description = "商家名称")
	private String merchantName;

	@Schema(description = "骑手名称")
	private String deliveryRiderName;

	@Schema(description = "客户姓名")
	private String customerName;

	@Schema(description = "客户地址")
	private WmAddress customerAddress;

	@Schema(description = "商家地址")
	private WmAddress merchantAddress;

	@Schema(description = "下单菜品项，下单请求入参")
	private List<DishPurchaseItemDTO> items;

	@Schema(description = "订单明细")
	private List<WmOrderItem> orderItems;

	@Schema(description = "订单自动取消截止时间戳(毫秒)")
	private Long autoCancelDeadlineTs;

}
