package com.luan.takeaway.takeaway.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("wm_dish")
@EqualsAndHashCode(callSuper = true)
public class WmDish extends BaseTakeawayEntity {

	private Long merchantUserId;

	private String dishImage;

	private String dishName;

	private String dishDesc;

	private BigDecimal price;

	private Integer stock;

	private String saleStatus;

}
