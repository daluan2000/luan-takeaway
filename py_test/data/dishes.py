from __future__ import annotations

import random
from dataclasses import dataclass, field
from decimal import Decimal
from typing import Literal


# ---------------------------------------------------------------------------
# 菜品分类与价格区间
# ---------------------------------------------------------------------------

@dataclass
class DishCategory:
    """菜品分类。"""
    name: str
    weight: float  # 在随机选中时的权重
    price_min: Decimal
    price_max: Decimal
    examples: list[str] = field(default_factory=list)


DISH_CATEGORIES: list[DishCategory] = [
    DishCategory(
        name="川菜",
        weight=0.15,
        price_min=Decimal("18.00"),
        price_max=Decimal("88.00"),
        examples=[
            "麻婆豆腐", "宫保鸡丁", "水煮鱼", "回锅肉", "鱼香肉丝",
            "酸菜鱼", "毛血旺", "辣子鸡", "口水鸡", "水煮牛肉",
            "干锅花菜", "干锅土豆片", "酸辣土豆丝", "蒜泥白肉",
        ],
    ),
    DishCategory(
        name="湘菜",
        weight=0.12,
        price_min=Decimal("16.00"),
        price_max=Decimal("78.00"),
        examples=[
            "剁椒鱼头", "小炒肉", "农家小炒肉", "臭豆腐", "口味虾",
            "剁椒蒸蛋", "酸辣牛百叶", "干锅茶树菇", "梅菜扣肉",
        ],
    ),
    DishCategory(
        name="粤菜",
        weight=0.12,
        price_min=Decimal("22.00"),
        price_max=Decimal("128.00"),
        examples=[
            "白切鸡", "烧鹅", "叉烧", "清蒸鲈鱼", "虾饺", "肠粉",
            "菠萝咕噜肉", "鼓汁蒸排骨", "白灼虾", "煲仔饭",
        ],
    ),
    DishCategory(
        name="火锅",
        weight=0.10,
        price_min=Decimal("38.00"),
        price_max=Decimal("188.00"),
        examples=[
            "麻辣锅底", "鸳鸯锅底", "番茄锅底", "菌汤锅底", "骨汤锅底",
            "鲜毛肚", "嫩牛肉", "手工虾滑", "精品肥牛", "嫩滑鱼片",
            "鸭血", "金针菇", "土豆片", "娃娃菜", "冻豆腐",
        ],
    ),
    DishCategory(
        name="烧烤",
        weight=0.08,
        price_min=Decimal("6.00"),
        price_max=Decimal("48.00"),
        examples=[
            "羊肉串", "牛肉串", "五花肉", "鸡翅", "烤茄子", "烤韭菜",
            "烤生蚝", "烤扇贝", "烤香肠", "烤鸡爪", "烤猪蹄",
        ],
    ),
    DishCategory(
        name="面食",
        weight=0.08,
        price_min=Decimal("12.00"),
        price_max=Decimal("35.00"),
        examples=[
            "兰州拉面", "重庆小面", "刀削面", "油泼面", "炸酱面",
            "热干面", "担担面", "牛肉拉面", "阳春面", "biangbiang面",
        ],
    ),
    DishCategory(
        name="小吃快餐",
        weight=0.10,
        price_min=Decimal("8.00"),
        price_max=Decimal("28.00"),
        examples=[
            "煎饼果子", "肉夹馍", "凉皮", "螺蛳粉", "肠粉",
            "炒饭", "炒面", "盖浇饭", "煲仔饭", "煎饺",
        ],
    ),
    DishCategory(
        name="饮品",
        weight=0.08,
        price_min=Decimal("6.00"),
        price_max=Decimal("25.00"),
        examples=[
            "珍珠奶茶", "芝芝莓莓", "柠檬水", "杨枝甘露", "椰汁西米露",
            "芋泥波波", "杨森奶茶", "西瓜汁", "芒果汁", "葡萄柚绿",
        ],
    ),
    DishCategory(
        name="甜品",
        weight=0.07,
        price_min=Decimal("8.00"),
        price_max=Decimal("32.00"),
        examples=[
            "提拉米苏", "芝士蛋糕", "芒果班戟", "杨枝甘露", "红豆沙",
            "芒果布丁", "榴莲千层", "雪媚娘", "双皮奶", "老婆饼",
        ],
    ),
    DishCategory(
        name="家常菜",
        weight=0.10,
        price_min=Decimal("15.00"),
        price_max=Decimal("48.00"),
        examples=[
            "红烧肉", "糖醋排骨", "可乐鸡翅", "红烧排骨", "土豆烧牛肉",
            "西红柿炒蛋", "青椒炒肉", "蒜蓉西兰花", "干煸四季豆",
            "凉拌黄瓜", "酸辣海带丝", "紫菜蛋花汤",
        ],
    ),
]

# 按权重随机选取分类
_WEIGHTED_CATEGORIES: list[DishCategory] = []
_TOTAL_WEIGHT = sum(c.weight for c in DISH_CATEGORIES)


def _pick_category() -> DishCategory:
    """按权重随机选取一个分类。"""
    r = random.random() * _TOTAL_WEIGHT
    cumulative = 0.0
    for cat in DISH_CATEGORIES:
        cumulative += cat.weight
        if r <= cumulative:
            return cat
    return DISH_CATEGORIES[-1]


# ---------------------------------------------------------------------------
# 菜品模板
# ---------------------------------------------------------------------------

@dataclass
class DishTemplate:
    """单个菜品数据模板。"""
    dish_name: str
    dish_desc: str
    price: Decimal
    stock: int
    sale_status: Literal["0", "1"] = "1"

    @staticmethod
    def random() -> DishTemplate:
        """从预设分类中随机生成一个菜品模板。"""
        cat = _pick_category()
        name = random.choice(cat.examples)

        # 给名字加点变化
        modifiers = ["微辣", "中辣", "特辣", "微麻", "招牌", "经典", "豪华", "精选", ""]
        modifier = random.choice(modifiers)
        display_name = f"{modifier}{name}" if modifier else name

        price = round(
            float(random.uniform(float(cat.price_min), float(cat.price_max))), 2
        )
        price = Decimal(str(price)).quantize(Decimal("0.01"))

        # 80% 上架，20% 下架
        sale_status: Literal["0", "1"] = random.choices(["1", "0"], weights=[0.8, 0.2])[0]

        stock = random.randint(30, 500)

        return DishTemplate(
            dish_name=display_name,
            dish_desc=f"{cat.name}，{name}，味道正宗，欢迎品尝",
            price=price,
            stock=stock,
            sale_status=sale_status,
        )


# ---------------------------------------------------------------------------
# 批量生成
# ---------------------------------------------------------------------------

def generate_dishes(count: int) -> list[DishTemplate]:
    """生成 N 个菜品模板（每个随机选取分类）。"""
    return [DishTemplate.random() for _ in range(count)]


# ---------------------------------------------------------------------------
# 商家专属菜品库（每个商家有固定的基础菜品集合，避免重复）
# ---------------------------------------------------------------------------

# 每个商家固定有 5 种招牌菜 + 随机补充
_SIGNATURE_DISHES_PER_MERCHANT = [
    ("招牌特色菜", Decimal("38.00")),
    ("老板推荐", Decimal("28.00")),
    ("秘制私房菜", Decimal("48.00")),
    ("今日特价", Decimal("18.00")),
    ("时令鲜蔬", Decimal("22.00")),
]


def generate_merchant_dishes(merchant_index: int) -> list[DishTemplate]:
    """
    为指定商家生成菜品列表。
    前 5 道为固定招牌菜，后 15 道为随机补充。
    """
    templates: list[DishTemplate] = []

    # 固定招牌菜
    for name, price in _SIGNATURE_DISHES_PER_MERCHANT:
        templates.append(DishTemplate(
            dish_name=f"商家{merchant_index + 1} {name}",
            dish_desc=f"{name}，商家精心制作，品质保障",
            price=price,
            stock=random.randint(50, 200),
            sale_status="1",
        ))

    # 随机补充
    templates.extend(generate_dishes(15))

    return templates
