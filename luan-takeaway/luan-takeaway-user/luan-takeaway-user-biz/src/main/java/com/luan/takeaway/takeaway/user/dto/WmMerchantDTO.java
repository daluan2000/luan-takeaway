package com.luan.takeaway.takeaway.user.dto;

import com.luan.takeaway.takeaway.common.entity.WmAddress;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

import com.luan.takeaway.takeaway.common.entity.WmMerchantUserExt;

import java.util.List;

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

	@Schema(description = "店铺菜品列表，仅在 includeDishList=true 时填充")
	private List<WmDish> dishList;

	// 保留，后续可扩展传输字段

}
