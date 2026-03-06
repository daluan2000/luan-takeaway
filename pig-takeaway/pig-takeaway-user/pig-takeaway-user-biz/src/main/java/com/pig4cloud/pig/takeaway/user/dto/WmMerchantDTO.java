package com.pig4cloud.pig.takeaway.user.dto;

import com.pig4cloud.pig.takeaway.common.entity.WmAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

import com.pig4cloud.pig.takeaway.common.entity.WmMerchantUserExt;

/**
 * 商家传输对象
 *
 * @author pig
 */
@Data
@Schema(description = "商家传输对象")
@EqualsAndHashCode(callSuper = true)
public class WmMerchantDTO extends WmMerchantUserExt {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "扩展信息是否不存在")
	private Boolean noExist;

	@Schema(description = "店铺地址信息")
	private WmAddress address;

	// 保留，后续可扩展传输字段

}
