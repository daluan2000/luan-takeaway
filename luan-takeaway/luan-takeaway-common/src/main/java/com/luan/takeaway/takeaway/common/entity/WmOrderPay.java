package com.luan.takeaway.takeaway.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wm_order_pay")
@EqualsAndHashCode(callSuper = true)
public class WmOrderPay extends BaseTakeawayEntity {

	private Long orderId;

	private String orderNo;

	private String payNo;

	private BigDecimal payAmount;

	private String payStatus;

	private String payChannel;

	private LocalDateTime payTime;

	private String failReason;

}
