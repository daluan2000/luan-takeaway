package com.pig4cloud.pig.takeaway.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

import com.pig4cloud.pig.takeaway.common.entity.WmAddress;
import com.pig4cloud.pig.takeaway.common.entity.WmCustomerUserExt;

/**
 * 客户传输对象
 *
 * @author pig
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "客户传输对象")
public class WmCustomerDTO extends WmCustomerUserExt {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "扩展信息是否不存在")
	private Boolean noExist;

	@Schema(description = "默认地址")
	private WmAddress defAddress;

}
