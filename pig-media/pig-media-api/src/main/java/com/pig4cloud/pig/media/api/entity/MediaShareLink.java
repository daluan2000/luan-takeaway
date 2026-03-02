package com.pig4cloud.pig.media.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * 相册分享链接
 */
@Data
@TableName("media_share_link")
@FieldNameConstants
@Schema(description = "相册分享链接")
@EqualsAndHashCode(callSuper = true)
public class MediaShareLink extends Model<MediaShareLink> {

	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	private String shareToken;

	private Long ownerId;

	private Long albumId;

	private String code;

	private LocalDateTime expireAt;

	private Integer maxViewCount;

	private Integer currentViewCount;

	private String status;

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
