package com.luan.takeaway.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推荐菜品项。
 * <p>
 * 该对象是 AI 模块输出给调用方的最小推荐单元，
 * 同时承载“可展示信息 + 打分信息 + 推荐理由”。
 */
@Data
@Schema(description = "推荐菜品项")
public class RecommendationItem {

	/** 菜品主键 ID（长整型雪花 ID）。 */
	private Long dishId;

	/** 所属商家用户 ID，用于前端跳转商家或做二次筛选。 */
	private Long merchantUserId;

	private String dishName;

	private String dishDesc;

	private BigDecimal price;

	/** 标签是给用户看的简洁特征，例如“清淡/不辣/预算内”。 */
	private List<String> tags;

	/** 人类可读推荐理由，优先由 LLM 生成，失败则规则兜底。 */
	private String reason;

	/** 综合分，用于调试和排序观察。 */
	private Double score;

}