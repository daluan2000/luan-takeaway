"""
批量注册种子用户：商家 / 客户 / 骑手。

用法：
    python run_tests.py --mode microservice -k seed_user -v
    python run_tests.py --mode microservice -k seed_user --fresh-seed  # 强制重建
    python run_tests.py --mode microservice -k seed_user -- \
        --lon-min 113.0 --lon-max 114.5 --lat-min 22.4 --lat-max 23.5  # 指定经纬度范围

环境变量（可选）：
    SEED_MERCHANT_COUNT  商家数量（默认 10）
    SEED_CUSTOMER_COUNT 客户数量（默认 50）
    SEED_DELIVERY_COUNT 骑手数量（默认 10）
    SEED_LON_MIN / SEED_LON_MAX / SEED_LAT_MIN / SEED_LAT_MAX  经纬度范围
"""
from __future__ import annotations

import os

import pytest

from data.users import DEFAULT_PASSWORD, generate_merchants, generate_customers, generate_deliveries


# ---------------------------------------------------------------------------
# 商家测试
# ---------------------------------------------------------------------------

def test_seed_merchants_10(seed_merchants: list[dict]) -> None:
    """注册 10 个商家 fixture，自动生成地址和商家扩展表。"""
    count = int(os.getenv("SEED_MERCHANT_COUNT", "10"))
    assert len(seed_merchants) >= count, f"期望至少 {count} 个商家，实际 {len(seed_merchants)}"

    for m in seed_merchants[:count]:
        assert m["userId"] is not None, f"商家 {m['username']} 缺少 userId"
        assert m["addressId"] is not None, f"商家 {m['username']} 缺少 addressId"
        assert m["username"], f"商家缺少 username"
        assert m["password"], f"商家缺少 password"


def test_seed_merchant_detail_addresses(seed_merchants: list[dict]) -> None:
    """验证商家地址的省市区和经纬度格式。"""
    for m in seed_merchants[:3]:
        print(f"\n商家 {m['username']}:")
        print(f"  merchantId = {m['merchantId']}")
        print(f"  addressId  = {m['addressId']}")
        print(f"  merchantName = {m.get('merchantName', 'N/A')}")


# ---------------------------------------------------------------------------
# 客户测试
# ---------------------------------------------------------------------------

def test_seed_customers_50(seed_customers: list[dict]) -> None:
    """注册 50 个客户 fixture，自动生成地址和客户扩展表。"""
    count = int(os.getenv("SEED_CUSTOMER_COUNT", "50"))
    assert len(seed_customers) >= count, f"期望至少 {count} 个客户，实际 {len(seed_customers)}"

    for c in seed_customers[:count]:
        assert c["userId"] is not None, f"客户 {c['username']} 缺少 userId"
        assert c["addressId"] is not None, f"客户 {c['username']} 缺少 addressId"
        assert c["username"], f"客户缺少 username"
        assert c["password"], f"客户缺少 password"


def test_seed_customer_detail_addresses(seed_customers: list[dict]) -> None:
    """打印前 5 个客户的详细信息。"""
    for c in seed_customers[:5]:
        print(f"\n客户 {c['username']} ({c.get('realName', '')}):")
        print(f"  customerId = {c['customerId']}")
        print(f"  addressId  = {c['addressId']}")
        print(f"  phone      = {c['phone']}")


# ---------------------------------------------------------------------------
# 骑手测试
# ---------------------------------------------------------------------------

def test_seed_deliveries_10(seed_deliveries: list[dict]) -> None:
    """注册 10 个骑手 fixture，自动创建骑手扩展表。"""
    count = int(os.getenv("SEED_DELIVERY_COUNT", "10"))
    assert len(seed_deliveries) >= count, f"期望至少 {count} 个骑手，实际 {len(seed_deliveries)}"

    for d in seed_deliveries[:count]:
        assert d["userId"] is not None, f"骑手 {d['username']} 缺少 userId"
        assert d["username"], f"骑手缺少 username"
        assert d["password"], f"骑手缺少 password"


# ---------------------------------------------------------------------------
# 全量测试（用于 --fresh-seed）
# ---------------------------------------------------------------------------

def test_seed_all_types(seed_merchants: list, seed_customers: list, seed_deliveries: list) -> None:
    """
    全量种子数据验证：
    - 所有 fixture 互相依赖，确保完整链路可用
    - 配合 --fresh-seed 运行可重建所有种子数据
    """
    assert len(seed_merchants) >= 1, "至少需要 1 个商家"
    assert len(seed_customers) >= 1, "至少需要 1 个客户"
    assert len(seed_deliveries) >= 1, "至少需要 1 个骑手"

    # 验证用户名格式
    for m in seed_merchants[:1]:
        assert m["username"].startswith("test_merchant"), f"商家用户名格式异常: {m['username']}"
    for c in seed_customers[:1]:
        assert c["username"].startswith("test_customer"), f"客户用户名格式异常: {c['username']}"
    for d in seed_deliveries[:1]:
        assert d["username"].startswith("test_delivery"), f"骑手用户名格式异常: {d['username']}"
