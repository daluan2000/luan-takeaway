package com.pig4cloud.pig.media.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建分享参数")
public class ShareCreateDTO {

	@NotNull(message = "相册ID不能为空")
	private Long albumId;

	private String code;

	private Integer expireHours;

	private Integer maxViewCount;

}
