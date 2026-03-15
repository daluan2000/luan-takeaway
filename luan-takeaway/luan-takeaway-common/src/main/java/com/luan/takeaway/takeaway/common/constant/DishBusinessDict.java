
package com.luan.takeaway.takeaway.common.constant;

import java.util.List;

/*
用于：
数据库存储
API 参数
SQL 查询
Tool Calling 结构过滤
*/
public class DishBusinessDict {

    // =========================================================
    // 菜品分类 category
    // =========================================================
    public static final List<String> CATEGORY = List.of(
            "rice",
            "noodle",
            "porridge",
            "hotpot",
            "drink",
            "snack",
            "dessert"
    );

    // =========================================================
    // 餐段 mealTime
    // =========================================================
    public static final List<String> MEAL_TIME = List.of(
            "breakfast",
            "lunch",
            "dinner",
            "midnight"
    );

    // =========================================================
    // 分量 portionSize
    // =========================================================
    public static final List<String> PORTION_SIZE = List.of(
            "small",
            "medium",
            "large"
    );

    // =========================================================
    // 辣度等级 spicyLevel
    // =========================================================
    public static final List<Integer> SPICY_LEVEL = List.of(
            0, 1, 2, 3, 4, 5
    );
}

