package com.pig4cloud.pig.media.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "预签名参数")
public class FilePresignDTO {

	@NotBlank(message = "文件名不能为空")
	private String fileName;

	private String contentType;

}
