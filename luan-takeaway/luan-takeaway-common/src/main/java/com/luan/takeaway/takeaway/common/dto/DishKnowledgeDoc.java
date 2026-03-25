package com.luan.takeaway.takeaway.common.dto;

import java.util.List;

import lombok.Data;

@Data
public class DishKnowledgeDoc {

    /**
     * 文档主键（关联菜品ID）
     */
    private Long dishId;

    // =========================================================
    // 一、结构化筛选字段（可用于 SQL / API 参数 / Tool Calling）
    // 说明：这些字段值应尽量来自业务字典，适合做精确过滤或范围过滤
    // =========================================================

    /**
     * 主类别（建议来自 DishBusinessDict.CATEGORY）
     */
    private String category;

    /**
     * 是否辣
     */
    private Boolean spicy;

    /**
     * 辣度等级（建议来自 DishBusinessDict.SPICY_LEVEL）
     */
    private Integer spicyLevel;

    /**
     * 是否清淡
     */
    private Boolean lightTaste;

    /**
     * 是否油腻
     */
    private Boolean oily;

    /**
     * 是否汤类
     */
    private Boolean soupBased;

    /**
     * 是否素食
     */
    private Boolean vegetarian;

    /**
     * 热量 kcal（可做数值范围筛选）
     */
    private Integer calories;

    /**
     * 蛋白质 g（可做数值范围筛选）
     */
    private Integer protein;

    /**
     * 脂肪 g（可做数值范围筛选）
     */
    private Integer fat;

    /**
     * 碳水 g（可做数值范围筛选）
     */
    private Integer carbohydrate;

    /**
     * 适用餐段（建议来自 DishBusinessDict.MEAL_TIME）
     */
    private List<String> mealTime;

    /**
     * 分量（建议来自 DishBusinessDict.PORTION_SIZE）
     */
    private String portionSize;

    // =========================================================
    // 二、语义字段（用于语义理解 / RAG 检索 / embedding 标准化）
    // 说明：通常来自语义词典，支持“意图相近”而非严格等值的召回
    // =========================================================

    /**
     * 标签（建议来自 DishSemanticDict.TAGS）
     */
    private List<String> tags;

    /**
     * 推荐场景（建议来自 DishSemanticDict.SUITABLE_SCENES）
     */
    private List<String> suitableScenes;

    /**
     * 不推荐场景（建议来自 DishSemanticDict.AVOID_SCENES）
     */
    private List<String> avoidScenes;

    /**
     * 适合人群（建议来自 DishSemanticDict.SUITABLE_PEOPLE）
     */
    private List<String> suitablePeople;

    /**
     * embedding 输入文本（程序拼接）
     * 推荐拼接来源：category + tags + suitableScenes + llmSummary
     */
    private String embeddingText;

    // =========================================================
    // 三、自由生成描述字段（LLM 解释性文本，不做结构化过滤）
    // 说明：用于推荐解释、文案展示，可读性优先
    // =========================================================

    /**
     * 风味描述（LLM自由生成）
     * 示例：
     * 酸甜微辣、温和清香、浓郁鲜香
     * 不参与过滤，只用于推荐解释
     */
    private String flavorDescription;

    /**
     * 菜品知识摘要（LLM自由生成）
     * 示例：
     * 番茄鸡蛋面属于清淡汤面，油脂较低，适合胃不适用户。
     */
    private String llmSummary;

    /**
     * 推荐理由（LLM自由生成）
     * 示例：
     * 适合作为晚餐轻食，负担较小。
     */
    private String recommendationReason;
}







