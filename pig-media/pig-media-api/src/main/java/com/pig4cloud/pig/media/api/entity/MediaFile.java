package com.pig4cloud.pig.media.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * 图片元数据
 */
@Data
@TableName("media_file")
@FieldNameConstants
@Schema(description = "图片元数据")
@EqualsAndHashCode(callSuper = true)
public class MediaFile extends Model<MediaFile> {

	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private Long id;

	@Schema(description = "所属用户ID")
	private Long ownerId;

	@Schema(description = "存储桶")
	private String bucketName;

	@Schema(description = "对象键")
	private String objectKey;

	@Schema(description = "原始文件名")
	private String originName;

	@Schema(description = "内容类型")
	private String contentType;

	@Schema(description = "文件大小")
	private Long fileSize;

	@Schema(description = "文件md5")
	private String md5;

	@Schema(description = "宽")
	private Integer width;

	@Schema(description = "高")
	private Integer height;

	@Schema(description = "状态")
	private String status;

	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建者")
	private String createBy;

	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "更新者")
	private String updateBy;

	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "更新时间")
	private LocalDateTime updateTime;

	@TableLogic
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "删除标记")
	private String delFlag;

}
