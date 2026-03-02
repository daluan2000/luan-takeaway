package com.pig4cloud.pig.media.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * 相册实体
 */
@Data
@TableName("media_album")
@FieldNameConstants
@Schema(description = "相册")
@EqualsAndHashCode(callSuper = true)
public class MediaAlbum extends Model<MediaAlbum> {

	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	private Long ownerId;

	private String name;

	private String description;

	private Long coverFileId;

	private String visibleScope;

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
