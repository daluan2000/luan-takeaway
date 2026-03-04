package com.pig4cloud.pig.takeaway.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("wm_address")
@EqualsAndHashCode(callSuper = true)
public class WmAddress extends BaseTakeawayEntity {

	private Long userId;

	private String province;

	private String city;

	private String district;

	private String detailAddress;

	private BigDecimal longitude;

	private BigDecimal latitude;

}
