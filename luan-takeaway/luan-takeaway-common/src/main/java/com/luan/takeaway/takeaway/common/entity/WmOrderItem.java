package com.luan.takeaway.takeaway.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("wm_order_item")
@EqualsAndHashCode(callSuper = true)
public class WmOrderItem extends BaseTakeawayEntity {

	private Long orderId;

	private Long dishId;

	private String dishName;

	private BigDecimal dishPrice;

	private Integer quantity;

	private BigDecimal itemAmount;

}
