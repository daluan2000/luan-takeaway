package com.pig4cloud.pig.takeaway.common.vo;

import com.pig4cloud.pig.takeaway.common.entity.WmOrder;
import com.pig4cloud.pig.takeaway.common.entity.WmOrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 订单详情对象
 *
 * @author pig
 */
@Data
@Schema(description = "订单详情对象")
public class OrderDetailVO {

	@Schema(description = "订单主信息")
	private WmOrder order;

	@Schema(description = "订单明细")
	private List<WmOrderItem> items;

}
