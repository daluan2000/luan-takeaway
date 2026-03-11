package com.luan.takeaway.common.mybatis.base;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;


/*
是的，自动填充的前提是：实体最终可访问到这些属性（本类或父类）且有对应填充注解；没有属性就不会填。

在你当前结构下，很多实体已经 extends Model<T>，受 Java 单继承限制，不能再直接 extends BaseEntity。

所以 BaseEntity 在现状里更像“规范模板/复用候选”，而不是“自动填充开关”。

再补一句：BaseEntity 不只是文档作用，理论上还能统一字段定义；只是目前被继承体系卡住了。

如果要真正用起来，常见做法是建一个 BaseModelEntity<T> extends Model<T>，把审计字段放进去，让业务实体继承它。
*/

/**
 * 基础实体抽象类，包含通用实体字段
 *
 * @author lengleng
 * @date 2025/05/31
 */
@Getter
@Setter
public class BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 创建者
	 */
	@Schema(description = "创建人")
	@TableField(fill = FieldFill.INSERT)
	private String createBy;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 更新者
	 */
	@Schema(description = "更新人")
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateBy;

	/**
	 * 更新时间
	 */
	@Schema(description = "更新时间")
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;

}
