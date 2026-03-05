package com.pig4cloud.pig.takeaway.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 客户传输对象
 *
 * @author pig
 */
@Data
@Schema(description = "客户传输对象")
public class WmCustomerDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "主键ID")
	private Long id;

	@Schema(description = "用户ID")
	private Long userId;

	@Schema(description = "真实姓名")
	private String realName;

	@Schema(description = "默认地址ID")
	private Long defaultAddressId;

}
