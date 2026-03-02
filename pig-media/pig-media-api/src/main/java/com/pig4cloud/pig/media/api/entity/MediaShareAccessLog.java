package com.pig4cloud.pig.media.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * 分享访问日志
 */
@Data
@TableName("media_share_access_log")
@FieldNameConstants
@Schema(description = "分享访问日志")
@EqualsAndHashCode(callSuper = true)
public class MediaShareAccessLog extends Model<MediaShareAccessLog> {

	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	private Long shareId;

	private String visitorIp;

	private String userAgent;

	private LocalDateTime accessTime;

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
