"""
批量导入接口测试

测试说明：
- 使用管理员账号（admin/123456）登录
- 读取 data/generators.py 生成的JSON文件中的测试数据
- 调用批量注册接口导入用户
- 调用用户扩展信息批量导入接口
- 调用菜品批量导入接口

前置条件：
1. 运行 python -m data.generators 生成测试数据文件
2. 确保管理员账号 admin/123456 存在且有权限

运行方式：
    python run_tests.py --mode microservice -k batch_import -v
"""

from __future__ import annotations

import json
import time
from pathlib import Path

import pytest

from core.client import ApiClient
from core.settings import TestSettings


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def admin_client(settings: TestSettings) -> ApiClient:
    """
    创建管理员ApiClient，使用admin账号登录

    管理员账号：
    - 用户名：admin
    - 密码：123456
    """
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
def batch_data_dir() -> Path:
    """批量数据文件目录"""
    return Path(__file__).parent.parent / "data"


@pytest.fixture(scope="session")
def batch_users_data(batch_data_dir: Path) -> dict:
    """加载批量注册用户数据"""
    filepath = batch_data_dir / "batch_users.json"
    if not filepath.exists():
        pytest.skip(f"数据文件不存在: {filepath}，请先运行 python -m data.generators")
    with open(filepath, encoding="utf-8") as f:
        return json.load(f)


@pytest.fixture(scope="session")
def batch_user_exts_data(batch_data_dir: Path) -> dict:
    """加载用户扩展信息数据"""
    filepath = batch_data_dir / "batch_user_exts.json"
    if not filepath.exists():
        pytest.skip(f"数据文件不存在: {filepath}，请先运行 python -m data.generators")
    with open(filepath, encoding="utf-8") as f:
        return json.load(f)


@pytest.fixture(scope="session")
def batch_dishes_data(batch_data_dir: Path) -> dict:
    """加载菜品批量导入数据"""
    filepath = batch_data_dir / "batch_dishes.json"
    if not filepath.exists():
        pytest.skip(f"数据文件不存在: {filepath}，请先运行 python -m data.generators")
    with open(filepath, encoding="utf-8") as f:
        return json.load(f)


# ---------------------------------------------------------------------------
# 测试用例
# ---------------------------------------------------------------------------

def test_admin_login(admin_client: ApiClient, settings: TestSettings) -> None:
    """测试管理员登录是否成功"""
    response = admin_client.get(settings.user_info_path)
    assert response.status_code == 200, f"管理员登录失败: {response.text}"

    body = response.json()
    print(f"\n  管理员登录成功: {body}")


def test_batch_register_users(admin_client: ApiClient, batch_users_data: dict) -> None:
    """
    测试批量注册用户接口

    接口：POST /user/batch/register
    权限：sys_user_batch_register
    """
    users = batch_users_data.get("all", [])

    print(f"\n  准备注册 {len(users)} 个用户:")
    for u in users[:3]:
        print(f"    - {u['username']} ({u['roleCode']})")

    payload = {
        "users": users
    }

    response = admin_client.post("/user/batch/register", json=payload)
    assert response.status_code == 200, f"批量注册请求失败: {response.text}"

    result = response.json()
    print(f"\n  批量注册结果:")
    print(f"    总数: {result.get('total', 0)}")
    print(f"    成功: {result.get('successCount', 0)}")
    print(f"    失败: {result.get('failCount', 0)}")

    # 验证结果
    assert result.get("code") == 0 or result.get("code") is None, f"接口返回错误: {result}"
    assert result.get("successCount", 0) > 0, "应该至少成功注册一个用户"

    # 收集成功的用户ID
    success_items = [r for r in result.get("results", []) if r.get("success")]
    print(f"\n  成功注册的用户:")
    for item in success_items[:5]:
        print(f"    - {item['username']} (userId: {item.get('userId')})")

    # 打印失败的用户
    fail_items = [r for r in result.get("results", []) if not r.get("success")]
    if fail_items:
        print(f"\n  注册失败的用户:")
        for item in fail_items:
            print(f"    - {item['username']}: {item.get('errorMessage')}")


def test_batch_import_user_extensions(admin_client: ApiClient, batch_user_exts_data: dict, batch_users_data: dict) -> None:
    """
    测试用户扩展信息批量导入接口

    接口：POST /takeaway/user/ext/batch/import
    权限：sys_user_ext_batch_import

    注意：需要先用批量注册接口创建用户，获取userId后再导入扩展信息
    """
    # 从batch_users_data中提取用户名到userId的映射
    # 由于批量注册返回的userId可能没有完整返回，这里简化处理
    # 实际测试时，用户ID需要通过查询接口获取

    all_exts = batch_user_exts_data.get("all", [])

    print(f"\n  准备导入 {len(all_exts)} 个用户扩展信息:")
    merchant_exts = batch_user_exts_data.get("merchant_exts", [])
    customer_exts = batch_user_exts_data.get("customer_exts", [])
    delivery_exts = batch_user_exts_data.get("delivery_exts", [])
    print(f"    - 商家扩展: {len(merchant_exts)}")
    print(f"    - 客户扩展: {len(customer_exts)}")
    print(f"    - 骑手扩展: {len(delivery_exts)}")

    # 注意：这里需要真实的userId
    # 简化处理：如果userId为0，说明还没有注册用户，跳过扩展信息导入测试
    items_with_ids = [e for e in all_exts if e.get("userId", 0) > 0]

    if not items_with_ids:
        print("\n  [跳过] 没有有效的userId，请先运行批量注册测试")
        pytest.skip("需要先注册用户并获取userId")

    payload = {
        "items": items_with_ids
    }

    response = admin_client.post("/takeaway/user/ext/batch/import", json=payload)
    assert response.status_code == 200, f"批量导入扩展信息请求失败: {response.text}"

    result = response.json()
    print(f"\n  批量导入扩展信息结果:")
    print(f"    总数: {result.get('total', 0)}")
    print(f"    成功: {result.get('successCount', 0)}")
    print(f"    失败: {result.get('failCount', 0)}")

    # 验证结果
    assert result.get("code") == 0 or result.get("code") is None, f"接口返回错误: {result}"

    # 打印失败项
    fail_items = [r for r in result.get("results", []) if not r.get("success")]
    if fail_items:
        print(f"\n  导入失败:")
        for item in fail_items[:5]:
            print(f"    - userId={item['userId']} ({item['userType']}): {item.get('errorMessage')}")


def test_batch_import_dishes(admin_client: ApiClient, batch_dishes_data: dict, batch_users_data: dict) -> None:
    """
    测试菜品批量导入接口（管理员模式）

    接口：POST /takeaway/dish/batch/import
    权限：wm_dish_batch_import

    注意：需要先注册商家用户，获取merchantUserId
    """
    dishes = batch_dishes_data.get("dishes", [])
    merchants = batch_users_data.get("merchants", [])

    print(f"\n  准备导入 {len(dishes)} 道菜品")

    # 为菜品设置商家ID（简化处理：使用请求级别的merchantUserId）
    if merchants and len(merchants) > 0:
        # 使用第一个商家的userId作为示例
        # 实际应该根据菜品对应的商家来设置
        merchant_user_id = merchants[0].get("userId", 0)

        if merchant_user_id > 0:
            # 设置所有菜品的商家ID
            for dish in dishes:
                dish["merchantUserId"] = merchant_user_id

            payload = {
                "merchantUserId": merchant_user_id,
                "dishes": dishes
            }

            response = admin_client.post("/takeaway/dish/batch/import", json=payload)
            assert response.status_code == 200, f"批量导入菜品请求失败: {response.text}"

            result = response.json()
            print(f"\n  菜品批量导入结果:")
            print(f"    总数: {result.get('total', 0)}")
            print(f"    成功: {result.get('successCount', 0)}")
            print(f"    失败: {result.get('failCount', 0)}")

            # 验证结果
            assert result.get("code") == 0 or result.get("code") is None, f"接口返回错误: {result}"
            assert result.get("successCount", 0) > 0, "应该至少成功导入一道菜品"

            # 打印成功导入的菜品
            success_items = [r for r in result.get("results", []) if r.get("success")]
            print(f"\n  成功导入的菜品 (前5道):")
            for item in success_items[:5]:
                print(f"    - {item['dishName']} (dishId: {item.get('dishId')})")

            # 打印失败的菜品
            fail_items = [r for r in result.get("results", []) if not r.get("success")]
            if fail_items:
                print(f"\n  导入失败的菜品:")
                for item in fail_items[:5]:
                    print(f"    - {item['dishName']}: {item.get('errorMessage')}")
        else:
            print("\n  [跳过] 商家userId无效，请先运行批量注册测试")
            pytest.skip("需要先注册商家并获取userId")
    else:
        print("\n  [跳过] 没有商家数据")
        pytest.skip("没有商家数据")


def test_batch_import_full_workflow(admin_client: ApiClient, batch_users_data: dict, batch_dishes_data: dict) -> None:
    """
    完整的批量导入工作流测试

    1. 批量注册用户
    2. 从注册结果中提取userId
    3. 更新菜品数据中的merchantUserId
    4. 调用菜品批量导入接口
    """
    # Step 1: 批量注册
    users = batch_users_data.get("all", [])
    payload = {"users": users}

    print("\n  [Step 1] 批量注册用户...")
    response = admin_client.post("/user/batch/register", json=payload)
    assert response.status_code == 200, f"批量注册失败: {response.text}"

    register_result = response.json()
    print(f"    成功: {register_result.get('successCount', 0)}, 失败: {register_result.get('failCount', 0)}")

    # Step 2: 提取userId映射
    user_id_map = {}  # username -> userId
    results = register_result.get("results", [])
    for item in results:
        if item.get("success") and item.get("userId"):
            user_id_map[item["username"]] = item["userId"]

    print(f"\n  [Step 2] 获取到 {len(user_id_map)} 个用户ID")

    # Step 3: 为菜品设置商家ID
    merchants = batch_users_data.get("merchants", [])
    dishes = batch_dishes_data.get("dishes", [])

    if not merchants or not dishes:
        pytest.skip("没有商家或菜品数据")

    # 找到注册成功的商家
    registered_merchants = [m for m in merchants if m["username"] in user_id_map]

    if not registered_merchants:
        print("\n  [跳过] 没有注册成功的商家")
        pytest.skip("没有注册成功的商家")

    # 使用第一个注册成功的商家
    first_merchant = registered_merchants[0]
    merchant_user_id = user_id_map[first_merchant["username"]]

    print(f"\n  [Step 3] 为菜品设置商家ID: {merchant_user_id}")

    # 更新菜品数据
    dishes_with_ids = []
    for dish in dishes:
        dish_copy = dish.copy()
        dish_copy["merchantUserId"] = merchant_user_id
        dishes_with_ids.append(dish_copy)

    # Step 4: 批量导入菜品
    payload = {
        "merchantUserId": merchant_user_id,
        "dishes": dishes_with_ids
    }

    print(f"\n  [Step 4] 批量导入 {len(dishes_with_ids)} 道菜品...")
    response = admin_client.post("/takeaway/dish/batch/import", json=payload)
    assert response.status_code == 200, f"菜品批量导入失败: {response.text}"

    dish_result = response.json()
    print(f"\n  菜品批量导入结果:")
    print(f"    总数: {dish_result.get('total', 0)}")
    print(f"    成功: {dish_result.get('successCount', 0)}")
    print(f"    失败: {dish_result.get('failCount', 0)}")

    # 验证
    assert dish_result.get("successCount", 0) > 0, "应该至少成功导入一道菜品"

    # 打印摘要
    print(f"\n  完整工作流测试通过!")
    print(f"  - 注册用户: {register_result.get('successCount', 0)}")
    print(f"  - 导入菜品: {dish_result.get('successCount', 0)}")


# ---------------------------------------------------------------------------
# 辅助测试：验证接口权限
# ---------------------------------------------------------------------------

def test_permission_check(admin_client: ApiClient) -> None:
    """
    测试管理员是否拥有批量操作权限

    通过调用接口并检查返回状态来验证权限
    """
    # 1. 测试批量注册权限
    print("\n  [权限检查] 测试批量注册接口权限...")
    response = admin_client.post("/user/batch/register", json={"users": []})
    # 空列表应该返回成功（权限通过）或业务错误（数据验证），但不应该返回403
    assert response.status_code != 403, "管理员应该有批量注册权限"
    print("    ✓ 批量注册权限正常")

    # 2. 测试用户扩展导入权限
    print("\n  [权限检查] 测试用户扩展导入接口权限...")
    response = admin_client.post("/takeaway/user/ext/batch/import", json={"items": []})
    assert response.status_code != 403, "管理员应该有用户扩展导入权限"
    print("    ✓ 用户扩展导入权限正常")

    # 3. 测试菜品批量导入权限
    print("\n  [权限检查] 测试菜品批量导入接口权限...")
    response = admin_client.post("/takeaway/dish/batch/import", json={"dishes": []})
    assert response.status_code != 403, "管理员应该有菜品批量导入权限"
    print("    ✓ 菜品批量导入权限正常")

    print("\n  所有权限检查通过!")
