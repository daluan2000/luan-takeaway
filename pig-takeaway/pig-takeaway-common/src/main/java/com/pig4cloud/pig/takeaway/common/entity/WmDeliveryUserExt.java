package com.pig4cloud.pig.takeaway.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("wm_delivery_user_ext")
@EqualsAndHashCode(callSuper = true)
public class WmDeliveryUserExt extends BaseTakeawayEntity {

	private Long userId;

	private String realName;

	private BigDecimal deliveryScopeKm;

	private String onlineStatus;

	private String employmentStatus;

}
