package com.pig4cloud.pig.media.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "相册增加图片参数")
public class AlbumItemAddDTO {

	@NotEmpty(message = "图片ID列表不能为空")
	private List<Long> fileIds;

}
