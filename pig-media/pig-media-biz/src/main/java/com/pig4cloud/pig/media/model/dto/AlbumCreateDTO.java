package com.pig4cloud.pig.media.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建相册参数")
public class AlbumCreateDTO {

	@NotBlank(message = "相册名称不能为空")
	private String name;

	private String description;

	private Long coverFileId;

	private String visibleScope;

}
