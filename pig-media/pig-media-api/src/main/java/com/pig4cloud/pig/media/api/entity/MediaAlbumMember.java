package com.pig4cloud.pig.media.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * 相册成员
 */
@Data
@TableName("media_album_member")
@FieldNameConstants
@Schema(description = "相册成员")
@EqualsAndHashCode(callSuper = true)
public class MediaAlbumMember extends Model<MediaAlbumMember> {

	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	private Long albumId;

	private Long userId;

	private String role;

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
