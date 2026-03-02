package com.pig4cloud.pig.media.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "直传确认参数")
public class UploadConfirmDTO {

	@NotBlank(message = "对象键不能为空")
	private String objectKey;

	@NotBlank(message = "原始文件名不能为空")
	private String originName;

	private String contentType;

	@NotNull(message = "文件大小不能为空")
	private Long fileSize;

	private String md5;

}
