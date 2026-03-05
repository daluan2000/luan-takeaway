package com.pig4cloud.pig.takeaway.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商家传输对象
 *
 * @author pig
 */
@Data
@Schema(description = "商家传输对象")
public class WmMerchantDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "主键ID")
	private Long id;

	@Schema(description = "用户ID")
	private Long userId;

	@Schema(description = "商家名称")
	private String merchantName;

	@Schema(description = "联系人")
	private String contactName;

	@Schema(description = "门店地址ID")
	private Long storeAddressId;

	@Schema(description = "营业状态")
	private String businessStatus;

	@Schema(description = "审核状态")
	private String auditStatus;

}
