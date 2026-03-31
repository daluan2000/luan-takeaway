"""
菜品数据模块 - 基于知识库的智能菜品生成系统

提供真实、合理的菜品数据：
- 从 dish_knowledge_base.json 加载知识库
- 按菜系权重随机选择
- 根据配料、做法、口味生成真实描述
- 基于成本和档次计算合理价格
"""

from __future__ import annotations

import json
import random
from dataclasses import dataclass, field
from decimal import Decimal
from pathlib import Path
from typing import Literal

# 知识库数据（全局缓存）
_KNOWLEDGE_BASE: dict | None = None
_KB_PATH = Path(__file__).parent / "dish_knowledge_base.json"


def _load_knowledge_base() -> dict:
    """加载菜品知识库（延迟加载，全局缓存）"""
    global _KNOWLEDGE_BASE
    if _KNOWLEDGE_BASE is None:
        with open(_KB_PATH, encoding="utf-8") as f:
            _KNOWLEDGE_BASE = json.load(f)
    return _KNOWLEDGE_BASE


# ---------------------------------------------------------------------------
# 知识库数据结构
# ---------------------------------------------------------------------------

@dataclass
class DishKnowledge:
    """菜品知识条目"""
    name: str
    description_templates: list[str]
    ingredients: list[str]
    cooking_methods: list[str]
    flavors: list[str]
    base_price: float
    price_factors: dict[str, float]


@dataclass
class CuisineInfo:
    """菜系信息"""
    name_cn: str
    name_en: str
    characteristics: list[str]
    typical_ingredients: list[str]
    price_tier: str
    popularity_weight: float
    price_range: dict[str, float]


@dataclass
class PriceTier:
    """价格档次"""
    name: str
    min_multiplier: float
    max_multiplier: float


# ---------------------------------------------------------------------------
# 知识库访问接口
# ---------------------------------------------------------------------------

def get_all_cuisines() -> dict[str, CuisineInfo]:
    """获取所有菜系信息"""
    kb = _load_knowledge_base()
    result = {}
    for cuisine_key, cuisine_data in kb["cuisines"].items():
        result[cuisine_key] = CuisineInfo(
            name_cn=cuisine_data["name_cn"],
            name_en=cuisine_data["name_en"],
            characteristics=cuisine_data["characteristics"],
            typical_ingredients=cuisine_data["typical_ingredients"],
            price_tier=cuisine_data["price_tier"],
            popularity_weight=cuisine_data["popularity_weight"],
            price_range=cuisine_data["price_range"],
        )
    return result


def get_cuisine_dishes(cuisine_key: str) -> list[DishKnowledge]:
    """获取指定菜系的所有菜品"""
    kb = _load_knowledge_base()
    dishes_data = kb["dishes"].get(cuisine_key, [])
    return [
        DishKnowledge(
            name=d["name"],
            description_templates=d["description_templates"],
            ingredients=d["ingredients"],
            cooking_methods=d["cooking_methods"],
            flavors=d["flavors"],
            base_price=d["base_price"],
            price_factors=d.get("price_factors", {}),
        )
        for d in dishes_data
    ]


def get_all_dishes() -> dict[str, list[DishKnowledge]]:
    """获取所有菜品（按菜系分组）"""
    kb = _load_knowledge_base()
    result = {}
    for cuisine_key in kb["dishes"]:
        result[cuisine_key] = get_cuisine_dishes(cuisine_key)
    return result


def get_price_tiers() -> dict[str, PriceTier]:
    """获取价格档次信息"""
    kb = _load_knowledge_base()
    return {
        key: PriceTier(
            name=data["name"],
            min_multiplier=data["min_multiplier"],
            max_multiplier=data["max_multiplier"],
        )
        for key, data in kb["price_tiers"].items()
    }


def get_naming_patterns() -> dict:
    """获取菜品命名模式"""
    kb = _load_knowledge_base()
    return kb.get("dish_naming_patterns", {})


# ---------------------------------------------------------------------------
# 菜品分类与价格区间（兼容旧接口）
# ---------------------------------------------------------------------------

@dataclass
class DishCategory:
    """菜品分类（兼容旧接口）"""
    name: str
    weight: float
    price_min: Decimal
    price_max: Decimal
    examples: list[str] = field(default_factory=list)


def _build_compat_categories() -> list[DishCategory]:
    """构建兼容旧接口的分类列表"""
    cuisines = get_all_cuisines()
    categories = []
    for cuisine_key, cuisine_info in cuisines.items():
        dishes = get_cuisine_dishes(cuisine_key)
        examples = [d.name for d in dishes[:10]]  # 取前10个作为示例
        categories.append(DishCategory(
            name=cuisine_info.name_cn,
            weight=cuisine_info.popularity_weight,
            price_min=Decimal(str(cuisine_info.price_range["min"])),
            price_max=Decimal(str(cuisine_info.price_range["max"])),
            examples=examples,
        ))
    return categories


DISH_CATEGORIES: list[DishCategory] = []
_TOTAL_WEIGHT = 0.0


def _ensure_categories() -> None:
    """确保分类列表已初始化"""
    global DISH_CATEGORIES, _TOTAL_WEIGHT
    if not DISH_CATEGORIES:
        DISH_CATEGORIES = _build_compat_categories()
        _TOTAL_WEIGHT = sum(c.weight for c in DISH_CATEGORIES)


def _pick_category() -> tuple[str, CuisineInfo, DishCategory]:
    """按权重随机选取一个分类，返回(菜系key, 菜系信息, 分类对象)"""
    _ensure_categories()
    r = random.random() * _TOTAL_WEIGHT
    cumulative = 0.0
    cuisines = get_all_cuisines()
    for cat in DISH_CATEGORIES:
        cumulative += cat.weight
        if r <= cumulative:
            # 找到对应的菜系key
            for key, info in cuisines.items():
                if info.name_cn == cat.name:
                    return key, info, cat
    return list(cuisines.items())[0][0], list(cuisines.values())[0], DISH_CATEGORIES[0]


# ---------------------------------------------------------------------------
# 智能菜品模板
# ---------------------------------------------------------------------------

@dataclass
class DishTemplate:
    """智能菜品数据模板"""
    dish_name: str
    dish_desc: str
    price: Decimal
    stock: int
    cuisine: str = ""  # 菜系
    ingredients: list[str] = field(default_factory=list)  # 配料
    cooking_method: str = ""  # 做法
    flavor: str = ""  # 口味
    sale_status: Literal["0", "1"] = "1"

    @staticmethod
    def _generate_name(
        dish_knowledge: DishKnowledge,
        cuisine_key: str,
        naming_patterns: dict,
    ) -> str:
        """根据知识库和命名模式生成菜品名称"""
        base_name = dish_knowledge.name
        
        # 决定是否添加前缀/后缀
        should_prefix = random.random() < 0.3
        should_suffix = random.random() < 0.2
        should_portion = random.random() < 0.15
        
        prefixes = naming_patterns.get("prefixes", {}).get(cuisine_key, [])
        suffixes = naming_patterns.get("suffixes", {}).get(cuisine_key, [])
        portion_names = naming_patterns.get("portion_names", {})
        
        name_parts = []
        
        if should_prefix and prefixes:
            prefix = random.choice(prefixes)
            name_parts.append(prefix)
        
        name_parts.append(base_name)
        
        # 随机选择一个价格因素作为变体
        if dish_knowledge.price_factors and random.random() < 0.4:
            variant = random.choice(list(dish_knowledge.price_factors.keys()))
            # 只在价格增幅大于0且不是"半份"类时添加
            factor_val = dish_knowledge.price_factors.get(variant, 0)
            if factor_val > 0 or variant in ["半只", "例牌", "小份"]:
                name_parts.append(variant)
        
        if should_suffix and suffixes:
            suffix = random.choice(suffixes)
            name_parts.append(suffix)
        
        if should_portion and portion_names:
            portion_size = random.choice(["small", "medium", "large"])
            portion_list = portion_names.get(portion_size, [])
            if portion_list:
                name_parts.append(random.choice(portion_list))
        
        return "".join(name_parts)

    @staticmethod
    def _generate_description(
        dish_knowledge: DishKnowledge,
        cuisine_info: CuisineInfo,
    ) -> str:
        """从知识库的描述模板中随机选择一个"""
        if dish_knowledge.description_templates:
            return random.choice(dish_knowledge.description_templates)
        
        # 备用生成逻辑
        ingredients_str = "、".join(dish_knowledge.ingredients[:3])
        flavor_str = "、".join(dish_knowledge.flavors)
        method_str = "、".join(dish_knowledge.cooking_methods)
        
        return f"{cuisine_info.name_cn}，选用{ingredients_str}，采用{method_str}工艺，口味{flavor_str}，正宗地道"

    @staticmethod
    def _calculate_price(
        dish_knowledge: DishKnowledge,
        cuisine_info: CuisineInfo,
        price_tiers: dict[str, PriceTier],
    ) -> Decimal:
        """根据知识库和档次计算价格"""
        base_price = dish_knowledge.base_price
        tier_info = price_tiers.get(cuisine_info.price_tier)
        
        if tier_info:
            multiplier = random.uniform(tier_info.min_multiplier, tier_info.max_multiplier)
            price = base_price * multiplier
        else:
            # 根据菜系价格范围调整
            price_range = cuisine_info.price_range
            price_min = price_range["min"]
            price_max = price_range["max"]
            
            # 以base_price为中心，在合理范围内波动
            if base_price < price_min:
                base_price = price_min
            elif base_price > price_max * 0.8:
                base_price = price_max * 0.8
            
            price_range_span = price_max - price_min
            if price_range_span > 0:
                offset = random.uniform(-price_range_span * 0.15, price_range_span * 0.15)
                price = base_price + offset
            else:
                price = base_price
        
        # 随机应用价格因素
        if dish_knowledge.price_factors and random.random() < 0.5:
            factors = list(dish_knowledge.price_factors.items())
            # 随机选择0-2个价格因素
            num_factors = random.randint(0, min(2, len(factors)))
            if num_factors > 0:
                selected = random.sample(factors, num_factors)
                for _, factor_val in selected:
                    price += factor_val
        
        # 确保价格在合理范围内
        final_price = max(price, cuisine_info.price_range["min"] * 0.7)
        final_price = min(final_price, cuisine_info.price_range["max"] * 1.5)
        
        return Decimal(str(round(final_price, 2)))

    @staticmethod
    def from_knowledge(
        cuisine_key: str | None = None,
        dish_knowledge: DishKnowledge | None = None,
    ) -> DishTemplate:
        """
        从知识库创建智能菜品模板。
        如果指定 cuisine_key，则在该菜系内随机选择菜品。
        如果指定 dish_knowledge，则直接使用该菜品。
        """
        kb = _load_knowledge_base()
        cuisines = get_all_cuisines()
        price_tiers = get_price_tiers()
        naming_patterns = get_naming_patterns()
        
        # 选择菜系和菜品
        if dish_knowledge is None:
            if cuisine_key is None:
                cuisine_key, _ = random.choice(list(cuisines.items()))
            elif cuisine_key not in cuisines:
                cuisine_key = random.choice(list(cuisines.keys()))
            
            cuisine_dishes = get_cuisine_dishes(cuisine_key)
            if not cuisine_dishes:
                cuisine_key = random.choice(list(cuisines.keys()))
                cuisine_dishes = get_cuisine_dishes(cuisine_key)
            
            dish_knowledge = random.choice(cuisine_dishes)
        
        if cuisine_key is None:
            cuisine_key = random.choice(list(cuisines.keys()))
        
        cuisine_info = cuisines[cuisine_key]
        
        # 生成各字段
        dish_name = DishTemplate._generate_name(dish_knowledge, cuisine_key, naming_patterns)
        dish_desc = DishTemplate._generate_description(dish_knowledge, cuisine_info)
        price = DishTemplate._calculate_price(dish_knowledge, cuisine_info, price_tiers)
        stock = random.randint(20, 500)
        
        # 80% 上架，20% 下架
        sale_status: Literal["0", "1"] = random.choices(["1", "0"], weights=[0.8, 0.2])[0]
        
        return DishTemplate(
            dish_name=dish_name,
            dish_desc=dish_desc,
            price=price,
            stock=stock,
            cuisine=cuisine_info.name_cn,
            ingredients=dish_knowledge.ingredients,
            cooking_method="、".join(dish_knowledge.cooking_methods),
            flavor="、".join(dish_knowledge.flavors),
            sale_status=sale_status,
        )

    @staticmethod
    def random() -> DishTemplate:
        """从预设分类中随机生成一个菜品模板（兼容旧接口）"""
        cuisine_key, cuisine_info, category = _pick_category()
        dishes = get_cuisine_dishes(cuisine_key)
        if dishes:
            dish_knowledge = random.choice(dishes)
        else:
            # 备用：使用旧方式生成
            name = random.choice(category.examples) if category.examples else "未知菜品"
            modifiers = ["微辣", "中辣", "特辣", "微麻", "招牌", "经典", "豪华", "精选", ""]
            modifier = random.choice(modifiers)
            display_name = f"{modifier}{name}" if modifier else name
            
            price = round(random.uniform(float(category.price_min), float(category.price_max)), 2)
            price = Decimal(str(price)).quantize(Decimal("0.01"))
            
            return DishTemplate(
                dish_name=display_name,
                dish_desc=f"{category.name}，{name}，味道正宗，欢迎品尝",
                price=price,
                stock=random.randint(30, 500),
                cuisine=category.name,
                sale_status=random.choices(["1", "0"], weights=[0.8, 0.2])[0],
            )
        
        return DishTemplate.from_knowledge(cuisine_key, dish_knowledge)


# ---------------------------------------------------------------------------
# 批量生成
# ---------------------------------------------------------------------------

def generate_dishes(count: int) -> list[DishTemplate]:
    """生成 N 个菜品模板（每个随机选取分类）"""
    return [DishTemplate.random() for _ in range(count)]


def generate_dishes_by_cuisine(count: int, cuisine_key: str) -> list[DishTemplate]:
    """生成 N 个指定菜系的菜品"""
    return [DishTemplate.from_knowledge(cuisine_key=cuisine_key) for _ in range(count)]


# ---------------------------------------------------------------------------
# 商家专属菜品库（每个商家有固定的基础菜品集合，避免重复）
# ---------------------------------------------------------------------------

# 每个商家固定有的招牌菜类型
_SIGNATURE_CUISINES = [
    ("川菜", 2),  # 2道川菜
    ("湘菜", 1),  # 1道湘菜
    ("粤菜", 1),  # 1道粤菜
    ("家常菜", 1),  # 1道家常菜
]


def _get_signature_dishes_for_merchant(merchant_index: int) -> list[tuple[str, DishKnowledge]]:
    """为商家获取招牌菜品"""
    cuisines = get_all_cuisines()
    dishes_by_cuisine = get_all_dishes()
    signature_dishes = []
    
    for cuisine_key, count in _SIGNATURE_CUISINES:
        cuisine_dishes = dishes_by_cuisine.get(cuisine_key, [])
        if cuisine_dishes:
            # 选择不同的菜品（避免重复）
            available = [d for d in cuisine_dishes 
                         if d not in [sd[1] for sd in signature_dishes]]
            if len(available) >= count:
                selected = random.sample(available, count)
                signature_dishes.extend([(cuisine_key, d) for d in selected])
            elif available:
                signature_dishes.append((cuisine_key, random.choice(available)))
    
    return signature_dishes


def generate_merchant_dishes(merchant_index: int, dish_count: int | tuple[int, int] = 20) -> list[DishTemplate]:
    """
    为指定商家生成菜品列表。
    包含：
    - 5道固定招牌菜（来自不同菜系）
    - 剩余道数为随机菜品
    
    Args:
        merchant_index: 商家索引（用于生成唯一的菜品名称变体）
        dish_count: 菜品总数，默认20道。接受单个整数或 (min, max) 元组（元组时随机选取）。
    """
    templates: list[DishTemplate] = []
    
    # 解析菜品数量（支持范围随机）
    if isinstance(dish_count, tuple):
        min_count, max_count = dish_count
        actual_count = random.randint(min_count, max_count)
    else:
        actual_count = dish_count
    
    # 获取招牌菜（固定5道）
    signature_dishes = _get_signature_dishes_for_merchant(merchant_index)
    for i, (cuisine_key, dish_knowledge) in enumerate(signature_dishes):
        # 招牌菜有特殊命名
        prefix = random.choice(["招牌", "秘制", "特色", "推荐", ""])
        base_name = dish_knowledge.name
        if prefix:
            dish_name = f"{prefix}{base_name}"
        else:
            dish_name = f"{base_name}"
        
        # 招牌菜价格稍高
        cuisine_info = get_all_cuisines()[cuisine_key]
        price_tiers = get_price_tiers()
        base_price = DishTemplate._calculate_price(dish_knowledge, cuisine_info, price_tiers)
        # 招牌菜加价10-20%
        price = Decimal(str(round(float(base_price) * random.uniform(1.1, 1.2), 2)))
        
        # 重新生成描述
        desc = DishTemplate._generate_description(dish_knowledge, cuisine_info)
        
        templates.append(DishTemplate(
            dish_name=dish_name,
            dish_desc=desc,
            price=price,
            stock=random.randint(50, 200),
            cuisine=cuisine_info.name_cn,
            ingredients=dish_knowledge.ingredients,
            cooking_method="、".join(dish_knowledge.cooking_methods),
            flavor="、".join(dish_knowledge.flavors),
            sale_status="1",  # 招牌菜默认上架
        ))
    
    # 计算剩余需要生成的菜品数量
    signature_count = len(signature_dishes)
    extra_count = actual_count - signature_count
    # 补充随机菜品
    templates.extend(generate_dishes(extra_count))
    
    return templates


# ---------------------------------------------------------------------------
# 知识库统计信息
# ---------------------------------------------------------------------------

def get_knowledge_stats() -> dict:
    """获取知识库统计信息"""
    cuisines = get_all_cuisines()
    dishes = get_all_dishes()
    
    stats = {
        "total_cuisines": len(cuisines),
        "total_dishes": sum(len(d) for d in dishes.values()),
        "cuisine_breakdown": {},
    }
    
    for cuisine_key, cuisine_dishes in dishes.items():
        cuisine_info = cuisines.get(cuisine_key)
        if cuisine_info:
            stats["cuisine_breakdown"][cuisine_info.name_cn] = {
                "count": len(cuisine_dishes),
                "avg_base_price": sum(d.base_price for d in cuisine_dishes) / len(cuisine_dishes) if cuisine_dishes else 0,
                "price_range": cuisine_info.price_range,
            }
    
    return stats


# ---------------------------------------------------------------------------
# 便捷函数
# ---------------------------------------------------------------------------

def print_knowledge_stats() -> None:
    """打印知识库统计信息（用于调试）"""
    stats = get_knowledge_stats()
    print(f"\n{'='*60}")
    print(f"  菜品知识库统计")
    print(f"{'='*60}")
    print(f"  菜系数量  : {stats['total_cuisines']}")
    print(f"  菜品总数  : {stats['total_dishes']}")
    print(f"{'='*60}")
    print(f"  各菜系菜品分布:")
    for cuisine_name, info in stats["cuisine_breakdown"].items():
        print(f"    {cuisine_name}: {info['count']} 道, "
              f"均价 {info['avg_base_price']:.1f}元, "
              f"范围 {info['price_range']['min']}-{info['price_range']['max']}元")
    print(f"{'='*60}\n")


# ---------------------------------------------------------------------------
# 测试入口
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    print("=== 智能菜品生成系统测试 ===\n")
    
    # 打印统计信息
    print_knowledge_stats()
    
    # 测试生成单个菜品
    print("=== 生成 10 个随机菜品 ===")
    for i, template in enumerate(generate_dishes(10), 1):
        print(f"\n{i}. {template.dish_name}")
        print(f"   菜系: {template.cuisine}")
        print(f"   价格: ¥{template.price}")
        print(f"   描述: {template.dish_desc}")
        print(f"   配料: {', '.join(template.ingredients[:3])}")
        print(f"   做法: {template.cooking_method}")
        print(f"   口味: {template.flavor}")
    
    print("\n=== 生成商家招牌菜示例 ===")
    merchant_dishes = generate_merchant_dishes(0)
    for i, template in enumerate(merchant_dishes[:5], 1):
        print(f"\n{i}. {template.dish_name}")
        print(f"   价格: ¥{template.price}")
        print(f"   描述: {template.dish_desc}")
