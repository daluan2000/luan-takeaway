package com.pig4cloud.pig.media.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "相册图片排序项")
public class AlbumItemSortDTO {

	@NotNull(message = "关联ID不能为空")
	private Long id;

	@NotNull(message = "排序值不能为空")
	private Integer sortNo;

}
