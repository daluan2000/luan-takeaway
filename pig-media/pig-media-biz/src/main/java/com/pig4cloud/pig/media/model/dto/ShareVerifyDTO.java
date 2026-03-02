package com.pig4cloud.pig.media.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分享提取码验证参数")
public class ShareVerifyDTO {

	private String code;

}
