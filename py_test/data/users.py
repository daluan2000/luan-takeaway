from __future__ import annotations

import random
from dataclasses import dataclass, field
from typing import Literal

# ---------------------------------------------------------------------------
# 用户模板数据
# ---------------------------------------------------------------------------

# 统一登录密码（符合后端密码策略）
DEFAULT_PASSWORD = "123456"


@dataclass
class UserTemplate:
    """单个用户的数据模板。"""
    username: str
    phone: str
    user_type: Literal["merchant", "customer", "delivery"] = "customer"

    @property
    def password(self) -> str:
        return DEFAULT_PASSWORD


@dataclass
class MerchantTemplate(UserTemplate):
    """商家用户模板。"""
    user_type: Literal["merchant"] = field(default="merchant", init=False)
    merchant_name: str = ""


@dataclass
class CustomerTemplate(UserTemplate):
    """客户用户模板。"""
    user_type: Literal["customer"] = field(default="customer", init=False)
    real_name: str = ""


@dataclass
class DeliveryTemplate(UserTemplate):
    """骑手用户模板。"""
    user_type: Literal["delivery"] = field(default="delivery", init=False)
    real_name: str = ""


# ---------------------------------------------------------------------------
# 数据生成器
# ---------------------------------------------------------------------------

# 商家店名关键词
_MERCHANT_NAME_PARTS = [
    "老字号", "正宗", "私房", "特色", "精品", "传统", "新派", "地道",
    "网红", "人气", "正宗", "农家", "手作", "秘制", "传承",
]
_MERCHANT_SUFFIX = [
    "川菜馆", "湘菜馆", "粤菜馆", "火锅店", "烧烤店", "面馆",
    "小吃店", "快餐店", "家常菜", "私房菜", "海鲜馆", "卤味店",
    "麻辣烫", "串串香", "奶茶店", "咖啡厅", "烘焙坊", "甜品店",
]
# 联系人名
_CONTACT_NAMES = [
    "张老板", "李经理", "王师傅", "刘老板", "陈大厨", "杨老板",
    "赵老板", "黄师傅", "周经理", "吴老板", "徐老板", "孙大厨",
    "马老板", "朱老板", "胡经理", "郭师傅", "林老板", "何经理",
]
# 骑手/客户姓名
_REAL_NAMES = [
    "张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十",
    "郑小明", "王小芳", "李建国", "张丽华", "刘德明", "陈志强",
    "杨晓红", "黄文龙", "周海洋", "吴秀英", "徐建平", "孙丽丽",
    "马俊杰", "朱艳芳", "胡志强", "郭小红", "林俊杰", "何文强",
]


def _generate_phones(start: int, count: int) -> list[str]:
    """从起始号码开始递增生成电话号码列表。"""
    base = str(start)
    # 确保 base 是11位，以 1 开头
    if not base.startswith("1") or len(base) != 11:
        base = "13800000000"
    phones = []
    for i in range(count):
        num = int(base) + i
        phones.append(f"1{int(str(int(base[:4])) + str(i).zfill(7))}"[:11])
        phones[-1] = f"1{str(13800000000 + i + random.randint(0, 99999999))[:10]}"[:11]
    return phones


def _make_phones(start: int, n: int) -> list[str]:
    """
    生成 n 个不重复的 11 位手机号。
    使用随机大偏移确保唯一性，避免并发时重复。
    """
    used: set[str] = set()
    base_num = 13800000000  # 基准值
    
    phones = []
    for i in range(n):
        # 使用大的随机范围 + 索引偏移，确保唯一性
        while True:
            # 生成一个在 10000000-99999999 范围内的随机8位数
            random_part = random.randint(10000000, 99999999)
            phone = f"138{random_part}"
            if phone not in used:
                used.add(phone)
                phones.append(phone)
                break
            # 如果重复，再加索引偏移
            phone = f"138{random_part + i}"
            if phone not in used and len(phone) == 11:
                used.add(phone)
                phones.append(phone)
                break
    
    return phones


# ---------------------------------------------------------------------------
# 批量生成
# ---------------------------------------------------------------------------

def generate_merchants(count: int, *, prefix: str = "test_merchant", phone_start: int = 13800000001) -> list[MerchantTemplate]:
    """生成商家模板列表。"""
    phones = _make_phones(phone_start, count)
    templates = []
    for i in range(count):
        suffix = random.choice(_MERCHANT_SUFFIX)
        prefix_part = random.choice(_MERCHANT_NAME_PARTS)
        templates.append(MerchantTemplate(
            username=f"{prefix}_{i + 1}",
            phone=phones[i],
            merchant_name=f"{prefix_part}{suffix}",
        ))
    return templates


def generate_customers(count: int, *, prefix: str = "test_customer", phone_start: int = 13801000001) -> list[CustomerTemplate]:
    """生成客户模板列表。"""
    phones = _make_phones(phone_start, count)
    templates = []
    for i in range(count):
        templates.append(CustomerTemplate(
            username=f"{prefix}_{i + 1}",
            phone=phones[i],
            real_name=random.choice(_REAL_NAMES),
        ))
    return templates


def generate_deliveries(count: int, *, prefix: str = "test_delivery", phone_start: int = 13802000001) -> list[DeliveryTemplate]:
    """生成骑手模板列表。"""
    phones = _make_phones(phone_start, count)
    templates = []
    for i in range(count):
        templates.append(DeliveryTemplate(
            username=f"{prefix}_{i + 1}",
            phone=phones[i],
            real_name=random.choice(_REAL_NAMES),
        ))
    return templates
