"""
批量创建种子菜品数据。

依赖 seed_merchants fixture，会为每个商家创建 20 道菜品。
用法：
    python run_tests.py --mode microservice -k seed_dish -v
    python run_tests.py --mode microservice -k seed_dish --fresh-seed  # 强制重建
    SEED_DISHES_PER_MERCHANT=30 pytest --mode microservice -k seed_dish -v  # 每个商家30道菜
"""
from __future__ import annotations

import os

import pytest


def test_seed_dishes_count(seed_dishes: list[dict]) -> None:
    """验证菜品数量：每个商家 20 道，默认 10 个商家，共 200 道。"""
    expected_total = int(os.getenv("SEED_DISHES_PER_MERCHANT", "20")) * int(os.getenv("SEED_MERCHANT_COUNT", "10"))
    assert len(seed_dishes) >= expected_total, f"期望至少 {expected_total} 道菜品，实际 {len(seed_dishes)}"


def test_seed_dishes_detail(seed_dishes: list[dict]) -> None:
    """打印前 10 道菜品的详细信息。"""
    for dish in seed_dishes[:10]:
        print(f"\n  商家ID: {dish.get('merchantUserId')}")
        print(f"  菜品名: {dish.get('dishName')}")
        print(f"  价格  : {dish.get('price')}")
        print(f"  库存  : {dish.get('stock')}")
        print(f"  状态  : {'上架' if dish.get('saleStatus') == '1' else '下架'}")


def test_seed_dishes_merchant_coverage(seed_dishes: list[dict], seed_merchants: list[dict]) -> None:
    """验证菜品覆盖了所有商家。"""
    merchant_ids = {m["userId"] for m in seed_merchants}
    dish_merchant_ids = {d.get("merchantUserId") for d in seed_dishes}

    missing = merchant_ids - dish_merchant_ids
    assert not missing, f"以下商家没有菜品: {missing}"


def test_seed_dishes_price_range(seed_dishes: list[dict]) -> None:
    """验证菜品价格在合理范围内。"""
    for dish in seed_dishes[:20]:
        price = dish.get("price", 0)
        assert price > 0, f"菜品价格异常: {dish.get('dishName')} -> {price}"
        assert price < 500, f"菜品价格过高: {dish.get('dishName')} -> {price}"


def test_seed_dishes_status_distribution(seed_dishes: list[dict]) -> None:
    """验证上架/下架比例接近 80%/20%。"""
    total = len(seed_dishes)
    if total == 0:
        pytest.skip("没有菜品数据")

    on_sale = sum(1 for d in seed_dishes if d.get("saleStatus") == "1")
    off_sale = sum(1 for d in seed_dishes if d.get("saleStatus") == "0")

    on_ratio = on_sale / total
    print(f"\n  总菜品: {total}")
    print(f"  上架  : {on_sale} ({on_ratio:.1%})")
    print(f"  下架  : {off_sale} ({1 - on_ratio:.1%})")

    # 允许一定偏差，但上架空比不应低于 60%
    assert on_ratio > 0.6, f"上架比例异常: {on_ratio:.1%}"
