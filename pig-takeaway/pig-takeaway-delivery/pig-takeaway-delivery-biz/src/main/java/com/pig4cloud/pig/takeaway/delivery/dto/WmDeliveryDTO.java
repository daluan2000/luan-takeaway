package com.pig4cloud.pig.takeaway.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

import com.pig4cloud.pig.takeaway.common.entity.WmDeliveryUserExt;

/**
 * 骑手传输对象
 *
 * @author pig
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "骑手传输对象")
public class WmDeliveryDTO extends WmDeliveryUserExt {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "扩展信息是否不存在")
	private Boolean noExist;

	// 保留，后续可扩展数据传输对象字段

}
