package com.pig4cloud.pig.takeaway.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("wm_delivery_order")
@EqualsAndHashCode(callSuper = true)
public class WmDeliveryOrder extends BaseTakeawayEntity {

	private Long orderId;

	private String orderNo;

	private Long merchantUserId;

	private Long deliveryUserId;

	private String deliveryStatus;

	private LocalDateTime acceptTime;

	private LocalDateTime pickupTime;

	private LocalDateTime deliveredTime;

	private LocalDateTime cancelTime;

}
