package com.pig4cloud.pig.takeaway.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 骑手传输对象
 *
 * @author pig
 */
@Data
@Schema(description = "骑手传输对象")
public class WmDeliveryDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "主键ID")
	private Long id;

	@Schema(description = "用户ID")
	private Long userId;

	@Schema(description = "真实姓名")
	private String realName;

	@Schema(description = "配送范围(公里)")
	private BigDecimal deliveryScopeKm;

	@Schema(description = "在线状态")
	private String onlineStatus;

	@Schema(description = "在职状态")
	private String employmentStatus;

}
