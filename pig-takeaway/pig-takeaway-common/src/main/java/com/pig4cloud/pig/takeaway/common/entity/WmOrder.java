package com.pig4cloud.pig.takeaway.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wm_order")
@EqualsAndHashCode(callSuper = true)
public class WmOrder extends BaseTakeawayEntity {

	private String orderNo;

	private Long customerUserId;

	private Long merchantUserId;

	private Long deliveryUserId;

	private Long deliveryAddressId;

	private BigDecimal totalAmount;

	private BigDecimal payAmount;

	private String orderStatus;

	private String remark;

	private LocalDateTime payTime;

	private LocalDateTime acceptTime;

	private LocalDateTime deliveryStartTime;

	private LocalDateTime finishTime;

	private LocalDateTime cancelTime;

}
