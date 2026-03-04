package com.pig4cloud.pig.takeaway.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("wm_merchant_user_ext")
@EqualsAndHashCode(callSuper = true)
public class WmMerchantUserExt extends BaseTakeawayEntity {

	private Long userId;

	private String merchantName;

	private String contactName;

	private Long storeAddressId;

	private String businessStatus;

	private String auditStatus;

}
