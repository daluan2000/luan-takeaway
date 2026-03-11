package com.luan.takeaway.takeaway.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("wm_customer_user_ext")
@EqualsAndHashCode(callSuper = true)
public class WmCustomerUserExt extends BaseTakeawayEntity {

	private Long userId;

	private String realName;

	private Long defaultAddressId;

}
