"""
批量导入接口测试

前置条件：
1. 运行 python -m data.generators 生成测试数据文件
2. 确保管理员账号 admin/123456 存在且有权限

运行方式：
    python run_tests.py --mode microservice -k batch_import -v
"""

from __future__ import annotations

import json
from pathlib import Path

import pytest
from dotenv import load_dotenv

load_dotenv()

from core.client import ApiClient
from core.settings import TestSettings, load_settings


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def settings(pytestconfig: pytest.Config) -> TestSettings:
    mode = pytestconfig.getoption("--mode", default=None)
    return load_settings(mode_override=mode)


@pytest.fixture(scope="session")
def admin_client(settings: TestSettings) -> ApiClient:
    """创建管理员ApiClient，使用admin账号登录"""
    client = ApiClient(
        base_url=settings.base_url,
        timeout=settings.timeout,
        business_path=settings.business_path,
    )
    client.set_login_params(
        username="admin",
        client_credential=settings.client_credential,
        password_encrypt_key=settings.password_encrypt_key,
        password_encrypt_enabled=settings.password_encrypt_enabled,
    )
    payload = client.login_as(password="123456", login_path=settings.login_path)
    client.set_bearer_token(payload["access_token"])
    return client


@pytest.fixture(scope="session")
def batch_data() -> dict:
    """加载所有批量数据"""
    data_dir = Path(__file__).parent.parent / "data"

    users_file = data_dir / "batch_users.json"
    exts_file = data_dir / "batch_user_exts.json"
    dishes_file = data_dir / "batch_dishes.json"

    if not users_file.exists():
        pytest.skip(f"数据文件不存在: {users_file}，请先运行 python -m data.generators")
    if not exts_file.exists():
        pytest.skip(f"数据文件不存在: {exts_file}，请先运行 python -m data.generators")
    if not dishes_file.exists():
        pytest.skip(f"数据文件不存在: {dishes_file}，请先运行 python -m data.generators")

    with open(users_file, encoding="utf-8") as f:
        users_data = json.load(f)
    with open(exts_file, encoding="utf-8") as f:
        exts_data = json.load(f)
    with open(dishes_file, encoding="utf-8") as f:
        dishes_data = json.load(f)

    return {
        "users": users_data,
        "exts": exts_data,
        "dishes": dishes_data,
    }


# ---------------------------------------------------------------------------
# 测试用例
# ---------------------------------------------------------------------------

def test_admin_login(admin_client: ApiClient, settings: TestSettings) -> None:
    """测试管理员登录"""
    response = admin_client.get(settings.user_info_path)
    assert response.status_code == 200, f"管理员登录失败: {response.text}"

    body = response.json()
    user_data = body.get("data", body)  # 兼容有无 data 包装的情况
    print(f"\n  管理员登录成功: {user_data['username']} ({user_data.get('nickname', '')})")


def test_batch_import_all(admin_client: ApiClient, batch_data: dict) -> None:
    """
    完整的批量导入工作流（一次请求完成每一步）

    Step 1: 批量注册所有用户
    Step 2: 批量导入所有用户扩展信息
    Step 3: 批量导入菜品（使用第一个商家）
    """
    users_data = batch_data["users"]
    exts_data = batch_data["exts"]
    dishes_data = batch_data["dishes"]

    all_users = users_data.get("all", [])
    merchants = users_data.get("merchants", [])
    dishes = dishes_data.get("dishes", [])

    print(f"\n{'='*60}")
    print(f"  批量导入数据总量")
    print(f"{'='*60}")
    print(f"  用户总数: {len(all_users)}")
    print(f"    - 商家: {len(merchants)}")
    print(f"    - 客户: {len(users_data.get('customers', []))}")
    print(f"    - 骑手: {len(users_data.get('deliveries', []))}")
    print(f"  扩展信息: {len(exts_data.get('all', []))}")
    print(f"  菜品: {len(dishes)}")
    print(f"{'='*60}")

    # Step 1: 批量注册用户（一次请求）
    print(f"\n[Step 1] 批量注册 {len(all_users)} 个用户...")
    response = admin_client.post("/user/batch/register", json={"users": all_users})
    assert response.status_code == 200, f"批量注册失败: {response.text}"

    register_result = response.json()
    assert register_result.get("code") == 0 or register_result.get("code") is None

    print(f"  注册结果: 成功 {register_result.get('successCount', 0)}, "
          f"失败 {register_result.get('failCount', 0)}")

    # 构建 userId 映射: username -> userId
    user_id_map: dict[str, int] = {}
    for item in register_result.get("results", []):
        if item.get("success") and item.get("userId"):
            user_id_map[item["username"]] = item["userId"]

    print(f"  获取到 {len(user_id_map)} 个用户ID")

    # 打印注册失败的用户
    failed_users = [r for r in register_result.get("results", []) if not r.get("success")]
    if failed_users:
        print(f"\n  注册失败的用户 (前5个):")
        for item in failed_users[:5]:
            print(f"    - {item['username']}: {item.get('errorMessage', '未知错误')}")

    # Step 2: 批量导入用户扩展信息（一次请求）
    all_exts = exts_data.get("all", [])
    if all_exts:
        print(f"\n[Step 2] 批量导入 {len(all_exts)} 个扩展信息...")

        # 更新 userId（使用注册后的真实ID）
        # 扩展数据中的 realName 存储的是 username
        exts_with_ids = []
        for ext in all_exts:
            ext_copy = ext.copy()
            real_name = ext.get("realName", "")
            if real_name in user_id_map:
                ext_copy["userId"] = user_id_map[real_name]
            else:
                ext_copy["userId"] = 0  # 未找到，跳过
            exts_with_ids.append(ext_copy)

        # 过滤有效的扩展信息
        valid_exts = [e for e in exts_with_ids if e["userId"] > 0]
        print(f"  有效扩展信息: {len(valid_exts)}/{len(all_exts)}")

        if valid_exts:
            response = admin_client.post("/user/ext/batch/import", json={"items": valid_exts})
            assert response.status_code == 200, f"批量导入扩展信息失败: {response.text}"

            ext_result = response.json()
            assert ext_result.get("code") == 0 or ext_result.get("code") is None

            print(f"  导入结果: 成功 {ext_result.get('successCount', 0)}, "
                  f"失败 {ext_result.get('failCount', 0)}")

    # Step 3: 批量导入菜品（一次请求）
    if dishes and merchants:
        # 找到注册成功的商家
        registered_merchants = [m for m in merchants if m["username"] in user_id_map]

        if registered_merchants:
            # 使用第一个注册成功的商家
            first_merchant = registered_merchants[0]
            merchant_user_id = user_id_map[first_merchant["username"]]

            print(f"\n[Step 3] 批量导入 {len(dishes)} 道菜品 (商家ID: {merchant_user_id})...")

            # 为菜品设置商家ID
            dishes_with_ids = [
                {**dish, "merchantUserId": merchant_user_id}
                for dish in dishes
            ]

            payload = {
                "merchantUserId": merchant_user_id,
                "dishes": dishes_with_ids,
            }

            response = admin_client.post("/takeaway/dish/batch/import", json=payload)
            assert response.status_code == 200, f"批量导入菜品失败: {response.text}"

            dish_result = response.json()
            assert dish_result.get("code") == 0 or dish_result.get("code") is None

            print(f"  导入结果: 成功 {dish_result.get('successCount', 0)}, "
                  f"失败 {dish_result.get('failCount', 0)}")

            # 打印失败的菜品
            failed_dishes = [r for r in dish_result.get("results", []) if not r.get("success")]
            if failed_dishes:
                print(f"\n  导入失败的菜品 (前5道):")
                for item in failed_dishes[:5]:
                    print(f"    - {item.get('dishName', '未知')}: {item.get('errorMessage', '未知错误')}")
        else:
            print("\n[Step 3] 跳过: 没有注册成功的商家")

    print(f"\n{'='*60}")
    print(f"  批量导入完成!")
    print(f"{'='*60}")


def test_permission_check(admin_client: ApiClient) -> None:
    """验证管理员批量操作权限"""
    print("\n[权限检查]")

    # 批量注册权限
    response = admin_client.post("/user/batch/register", json={"users": []})
    assert response.status_code != 403, "管理员应有批量注册权限"
    print("  ✓ 批量注册权限正常")

    # 扩展信息导入权限
    response = admin_client.post("/user/ext/batch/import", json={"items": []})
    assert response.status_code != 403, "管理员应有扩展信息导入权限"
    print("  ✓ 扩展信息导入权限正常")

    # 菜品批量导入权限
    response = admin_client.post("/dish/batch/import", json={"dishes": []})
    assert response.status_code != 403, "管理员应有菜品批量导入权限"
    print("  ✓ 菜品批量导入权限正常")
