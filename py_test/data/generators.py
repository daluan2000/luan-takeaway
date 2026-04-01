"""
批量导入数据生成模块（非pytest独立模块）

功能：仅生成测试数据并保存到JSON文件，不调用任何API。

使用方式：
    python -m data.generators --merchants 3 --customers 5 --deliveries 3 --dishes-min 5 --dishes-max 15 --listing-prob 0.8

输出文件：
    data/batch_users.json      : 批量注册用户数据
    data/batch_user_exts.json : 用户扩展信息数据
    data/batch_dishes.json    : 菜品批量导入数据
"""

from __future__ import annotations

import argparse
import json
import random
import sys
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Literal

# 添加项目根目录到路径
sys.path.insert(0, str(Path(__file__).parent.parent))

from data.users import (
    DEFAULT_PASSWORD,
    MerchantTemplate,
    CustomerTemplate,
    DeliveryTemplate,
    generate_merchants,
    generate_customers,
    generate_deliveries,
)
from data.dishes import DishTemplate, generate_merchant_dishes


# ---------------------------------------------------------------------------
# 配置
# ---------------------------------------------------------------------------

OUTPUT_DIR = Path(__file__).parent


# ---------------------------------------------------------------------------
# 数据结构（用于序列化为JSON）
# ---------------------------------------------------------------------------

@dataclass
class BatchUserDTO:
    """批量注册用户DTO"""
    username: str
    password: str
    nickname: str = ""
    name: str = ""
    phone: str = ""
    email: str = ""
    roleCode: str = ""


@dataclass
class BatchUserExtDTO:
    """用户扩展信息导入DTO"""
    userId: int  # 占位符，导入时需替换为真实userId
    userType: Literal["customer", "merchant", "delivery"]
    realName: str = ""
    # 商家专属字段
    storeName: str = ""
    storeAddressId: int = 0
    auditStatus: str = ""
    businessStatus: str = ""
    # 骑手专属字段
    deliveryScopeKm: float = 5.0
    onlineStatus: str = ""
    employmentStatus: str = ""


@dataclass
class BatchDishDTO:
    """菜品批量导入DTO"""
    merchantUserId: int = 0  # 占位符，导入时需替换
    dishName: str = ""
    dishDesc: str = ""
    dishImage: str = ""
    price: float = 0.0
    stock: int = 0
    saleStatus: str = "1"
    autoGenerateKnowledge: bool = False


# ---------------------------------------------------------------------------
# 数据生成
# ---------------------------------------------------------------------------

def generate_batch_users(
    merchant_count: int,
    customer_count: int,
    delivery_count: int,
    prefix: str,
) -> dict:
    """生成批量注册用户数据"""
    merchants_tmpl = generate_merchants(merchant_count, prefix=f"{prefix}_merchant")
    customers_tmpl = generate_customers(customer_count, prefix=f"{prefix}_customer")
    deliveries_tmpl = generate_deliveries(delivery_count, prefix=f"{prefix}_delivery")

    def to_dto(tmpl, role_code: str, role_type: str) -> BatchUserDTO:
        return BatchUserDTO(
            username=tmpl.username,
            password=tmpl.password,
            nickname=tmpl.username,
            name=tmpl.username,
            phone=tmpl.phone,
            email=f"{tmpl.username}@test.com",
            roleCode=role_code,
        )

    merchants = [to_dto(t, "ROLE_MERCHANT", "merchant") for t in merchants_tmpl]
    customers = [to_dto(t, "ROLE_CUSTOMER", "customer") for t in customers_tmpl]
    deliveries = [to_dto(t, "ROLE_DELIVERY", "delivery") for t in deliveries_tmpl]

    return {
        "merchants": merchants,
        "customers": customers,
        "deliveries": deliveries,
        "all": merchants + customers + deliveries,
    }


def generate_batch_user_exts(
    merchants: list[BatchUserDTO],
    customers: list[BatchUserDTO],
    deliveries: list[BatchUserDTO],
) -> dict:
    """生成用户扩展信息数据（userId为0占位）"""
    merchant_exts = []
    for m in merchants:
        merchant_exts.append(BatchUserExtDTO(
            userId=0,
            userType="merchant",
            realName=m.username,
            storeName=m.username,
            storeAddressId=0,
            auditStatus="AUDIT_APPROVED",
            businessStatus="BUSINESS_OPEN",
        ))

    customer_exts = []
    for c in customers:
        customer_exts.append(BatchUserExtDTO(
            userId=0,
            userType="customer",
            realName=c.username,
        ))

    delivery_exts = []
    for d in deliveries:
        delivery_exts.append(BatchUserExtDTO(
            userId=0,
            userType="delivery",
            realName=d.username,
            deliveryScopeKm=random.uniform(3.0, 10.0),
            onlineStatus="ONLINE_OFF",
            employmentStatus="EMPLOYMENT_ON",
        ))

    return {
        "merchant_exts": merchant_exts,
        "customer_exts": customer_exts,
        "delivery_exts": delivery_exts,
        "all": merchant_exts + customer_exts + delivery_exts,
    }


def generate_batch_dishes(
    merchant_count: int,
    dishes_per_merchant: int | tuple[int, int],
    listing_prob: float = 0.8,
) -> list[BatchDishDTO]:
    """生成菜品批量导入数据
    
    Args:
        merchant_count: 商家数量
        dishes_per_merchant: 每个商家的菜品数量，支持整数或(min, max)元组
        listing_prob: 菜品上架概率，默认0.8（80%%概率上架）
    """
    dishes = []
    for idx in range(merchant_count):
        templates = generate_merchant_dishes(idx, dish_count=dishes_per_merchant, listing_prob=listing_prob)
        for tpl in templates:
            dishes.append(BatchDishDTO(
                merchantUserId=0,  # 占位，导入时替换
                dishName=tpl.dish_name,
                dishDesc=tpl.dish_desc,
                dishImage=f"https://via.placeholder.com/400x300?text={tpl.dish_name[:10]}",
                price=float(tpl.price),
                stock=tpl.stock,
                saleStatus=tpl.sale_status,
                autoGenerateKnowledge=False,
            ))
    return dishes


# ---------------------------------------------------------------------------
# 工具函数
# ---------------------------------------------------------------------------

def _serialize(obj):
    """将dataclass对象序列化为字典"""
    if hasattr(obj, '__dataclass_fields__'):
        return {k: _serialize(v) for k, v in obj.__dict__.items()}
    if isinstance(obj, list):
        return [_serialize(item) for item in obj]
    if isinstance(obj, dict):
        return {k: _serialize(v) for k, v in obj.items()}
    return obj


def _save_json(data: dict, filepath: Path) -> None:
    """保存数据到JSON文件"""
    filepath.parent.mkdir(parents=True, exist_ok=True)
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"  保存: {filepath}")


# ---------------------------------------------------------------------------
# 主函数
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(description="批量导入数据生成器")
    parser.add_argument("--merchants", type=int, default=3000, help="商家数量")
    parser.add_argument("--customers", type=int, default=100, help="客户数量")
    parser.add_argument("--deliveries", type=int, default=200, help="骑手数量")
    parser.add_argument("--dishes-min", type=int, default=5, help="每个商家菜品数量下限")
    parser.add_argument("--dishes-max", type=int, default=10, help="每个商家菜品数量上限")
    parser.add_argument("--listing-prob", type=float, default=0.8, help="菜品上架概率 (0.0-1.0)")
    parser.add_argument("--output", type=str, default=str(OUTPUT_DIR), help="输出目录")
    args = parser.parse_args()
    
    # 解析菜品数量范围
    dishes_range = (args.dishes_min, args.dishes_max)
    
    # 验证参数
    if args.dishes_min > args.dishes_max:
        print("错误: --dishes-min 不能大于 --dishes-max")
        sys.exit(1)
    if not 0.0 <= args.listing_prob <= 1.0:
        print("错误: --listing-prob 必须在 0.0-1.0 之间")
        sys.exit(1)

    timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
    prefix = f"batch_{timestamp}"

    print("=" * 50)
    print(f"  批量导入数据生成器")
    print("=" * 50)
    print(f"  商家: {args.merchants}")
    print(f"  客户: {args.customers}")
    print(f"  骑手: {args.deliveries}")
    print(f"  菜品/商家: {args.dishes_min}-{args.dishes_max} (随机)")
    print(f"  上架概率: {args.listing_prob * 100:.0f}%")
    print("=" * 50)

    # 1. 生成用户数据
    print("\n[1/3] 生成用户数据...")
    users_data = generate_batch_users(
        merchant_count=args.merchants,
        customer_count=args.customers,
        delivery_count=args.deliveries,
        prefix=prefix,
    )
    total_users = len(users_data["all"])

    # 2. 生成扩展信息数据
    print("[2/3] 生成扩展信息数据...")
    exts_data = generate_batch_user_exts(
        merchants=users_data["merchants"],
        customers=users_data["customers"],
        deliveries=users_data["deliveries"],
    )

    # 3. 生成菜品数据
    print("[3/3] 生成菜品数据...")
    dishes = generate_batch_dishes(
        merchant_count=args.merchants,
        dishes_per_merchant=dishes_range,
        listing_prob=args.listing_prob,
    )

    # 保存文件
    output_dir = Path(args.output)
    _save_json(_serialize(users_data), output_dir / "batch_users.json")
    _save_json(_serialize(exts_data), output_dir / "batch_user_exts.json")
    _save_json({
        "dishes": _serialize(dishes),
        "total": len(dishes),
        "merchant_count": args.merchants,
        "dishes_range": f"{args.dishes_min}-{args.dishes_max}",
        "listing_prob": args.listing_prob,
    }, output_dir / "batch_dishes.json")

    print(f"\n总计: {total_users} 个用户, {len(dishes)} 道菜品")
    print("=" * 50)


if __name__ == "__main__":
    main()
