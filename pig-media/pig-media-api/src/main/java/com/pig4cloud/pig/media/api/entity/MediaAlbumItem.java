package com.pig4cloud.pig.media.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * 相册图片关联
 */
@Data
@TableName("media_album_item")
@FieldNameConstants
@Schema(description = "相册图片关联")
@EqualsAndHashCode(callSuper = true)
public class MediaAlbumItem extends Model<MediaAlbumItem> {

	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	private Long albumId;

	private Long fileId;

	private Integer sortNo;

	@TableField(fill = FieldFill.INSERT)
	private String createBy;

	@TableField(fill = FieldFill.UPDATE)
	private String updateBy;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;

	@TableLogic
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;

}
