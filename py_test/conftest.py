from __future__ import annotations

import json
import os
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

import pytest
from dotenv import load_dotenv

# 加载 .env 文件
load_dotenv()

MAX_SEED_WORKERS = int(os.getenv("MAX_SEED_WORKERS"))


def _env_int(name: str) -> int:
    """读取整数环境变量，缺失时抛出错误。"""
    val = os.getenv(name)
    if val is None:
        raise ValueError(f"环境变量 {name} 未设置")
    try:
        return int(val)
    except ValueError:
        raise ValueError(f"环境变量 {name} 必须为整数，当前值: {val}")


def _env_float(name: str) -> float | None:
    """读取浮点环境变量，缺失或为空时返回 None。"""
    val = os.getenv(name)
    if val is None or val == "":
        return None
    try:
        return float(val)
    except ValueError:
        raise ValueError(f"环境变量 {name} 必须为浮点数，当前值: {val}")


from core.client import ApiClient
from core.settings import TestSettings, load_settings, ROLE_MERCHANT_CODE, ROLE_CUSTOMER_CODE, ROLE_DELIVERY_CODE
from data.users import (
    DEFAULT_PASSWORD,
    CustomerTemplate,
    DeliveryTemplate,
    MerchantTemplate,
    generate_customers,
    generate_deliveries,
    generate_merchants,
)
from data.dishes import generate_merchant_dishes
from data.china_region import generate_address


# ---------------------------------------------------------------------------
# 缓存文件路径
# ---------------------------------------------------------------------------

def _cache_dir(pytest_config: pytest.Config) -> Path:
    root = Path(pytest_config.rootpath or Path(__file__).resolve().parent.parent)
    cache = root / ".pytest_seed_cache"
    cache.mkdir(exist_ok=True)
    return cache


def _seed_cache_file(pytest_config: pytest.Config, suffix: str = "seed") -> Path:
    return _cache_dir(pytest_config) / f"{suffix}.json"


def _load_cache(pytest_config: pytest.Config, suffix: str = "seed") -> dict:
    fpath = _seed_cache_file(pytest_config, suffix)
    if fpath.exists():
        try:
            with fpath.open(encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            pass
    return {}


def _save_cache(pytest_config: pytest.Config, data: dict, suffix: str = "seed") -> None:
    """写入缓存前排除 ApiClient 等不可序列化的对象。"""
    import json

    def _strip_clients(obj):
        if isinstance(obj, dict):
            return {k: _strip_clients(v) for k, v in obj.items() if k != "client"}
        if isinstance(obj, list):
            return [_strip_clients(item) for item in obj]
        return obj

    fpath = _seed_cache_file(pytest_config, suffix)
    with fpath.open("w", encoding="utf-8") as f:
        json.dump(_strip_clients(data), f, ensure_ascii=False, indent=2)


# ---------------------------------------------------------------------------
# pytest_addoption
# ---------------------------------------------------------------------------

def pytest_addoption(parser: pytest.Parser) -> None:
    """添加命令行选项，优先级：命令行 > .env > 默认值"""
    # 运行模式
    parser.addoption(
        "--mode",
        action="store",
        choices=["monolith", "microservice"],
        default=None,
        help="后端运行模式：monolith 或 microservice（环境变量：API_MODE）",
    )
    # 强制重建种子数据
    parser.addoption(
        "--fresh-seed",
        action="store_true",
        default=None,
        help="强制重新生成种子数据，忽略缓存（环境变量：SEED_FRESH=true）",
    )
    # 种子数据数量
    parser.addoption(
        "--merchant-count",
        action="store",
        type=int,
        default=None,
        help="商家数量（环境变量：SEED_MERCHANT_COUNT）",
    )
    parser.addoption(
        "--customer-count",
        action="store",
        type=int,
        default=None,
        help="客户数量（环境变量：SEED_CUSTOMER_COUNT）",
    )
    parser.addoption(
        "--delivery-count",
        action="store",
        type=int,
        default=None,
        help="骑手数量（环境变量：SEED_DELIVERY_COUNT）",
    )
    parser.addoption(
        "--dishes-per-merchant",
        action="store",
        type=int,
        default=None,
        help="每个商家的菜品数量（环境变量：SEED_DISHES_PER_MERCHANT）",
    )
    # 地址经纬度范围
    parser.addoption("--lon-min", action="store", type=float, default=None)
    parser.addoption("--lon-max", action="store", type=float, default=None)
    parser.addoption("--lat-min", action="store", type=float, default=None)
    parser.addoption("--lat-max", action="store", type=float, default=None)


def _env_bool(name: str) -> bool:
    """读取布尔环境变量，缺失时抛出错误。"""
    val = os.getenv(name)
    if val is None:
        raise ValueError(f"环境变量 {name} 未设置")
    return val.strip().lower() in {"1", "true", "yes", "on"}


def pytest_configure(config: pytest.Config) -> None:
    config.seed_lon_min = config.getoption("--lon-min", default=None)
    config.seed_lon_max = config.getoption("--lon-max", default=None)
    config.seed_lat_min = config.getoption("--lat-min", default=None)
    config.seed_lat_max = config.getoption("--lat-max", default=None)

    # 种子数据数量配置（命令行 > .env）
    config.seed_merchant_count = config.getoption("--merchant-count", default=None) or _env_int("SEED_MERCHANT_COUNT")
    config.seed_customer_count = config.getoption("--customer-count", default=None) or _env_int("SEED_CUSTOMER_COUNT")
    config.seed_delivery_count = config.getoption("--delivery-count", default=None) or _env_int("SEED_DELIVERY_COUNT")
    config.seed_dishes_per_merchant = config.getoption("--dishes-per-merchant", default=None) or _env_int("SEED_DISHES_PER_MERCHANT")

    # 是否强制重建种子数据（命令行 > .env）
    fresh_arg = config.getoption("--fresh-seed", default=None)
    config.seed_fresh = True if fresh_arg else _env_bool("SEED_FRESH")


# ---------------------------------------------------------------------------
# 高层 seed fixtures
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def settings(pytestconfig: pytest.Config) -> TestSettings:
    mode = pytestconfig.getoption("mode")
    return load_settings(mode_override=mode)


# ---------------------------------------------------------------------------
# Token fixtures（每个用户类型各自有 token）
# ---------------------------------------------------------------------------

def _make_authenticated_client(
    settings: TestSettings,
    username: str,
    password: str,
) -> ApiClient:
    """用指定账号登录，返回已设置 Bearer token 的 ApiClient。"""
    client = ApiClient(
        base_url=settings.base_url,
        timeout=settings.timeout,
        business_path=settings.business_path,
    )
    client.set_login_params(
        username=username,
        client_credential=settings.client_credential,
        password_encrypt_key=settings.password_encrypt_key,
        password_encrypt_enabled=settings.password_encrypt_enabled,
    )
    payload = client.login_as(password=password, login_path=settings.login_path)
    client.set_bearer_token(payload["access_token"])
    return client


@pytest.fixture(scope="session")
def merchant_clients(
    settings: TestSettings,
    seed_merchants: list[dict],
) -> list[ApiClient]:
    """每个商家一个已认证的 ApiClient。"""
    return [
        _make_authenticated_client(settings, m["username"], m["password"])
        for m in seed_merchants
    ]


@pytest.fixture(scope="session")
def customer_clients(
    settings: TestSettings,
    seed_customers: list[dict],
) -> list[ApiClient]:
    """每个客户一个已认证的 ApiClient。"""
    return [
        _make_authenticated_client(settings, c["username"], c["password"])
        for c in seed_customers
    ]


@pytest.fixture(scope="session")
def delivery_clients(
    settings: TestSettings,
    seed_deliveries: list[dict],
) -> list[ApiClient]:
    """每个骑手一个已认证的 ApiClient。"""
    return [
        _make_authenticated_client(settings, d["username"], d["password"])
        for d in seed_deliveries
    ]


# ---------------------------------------------------------------------------
# 种子数据缓存 fixture
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def seed_cache(pytestconfig: pytest.Config) -> dict:
    """加载或初始化种子数据缓存。"""
    fresh = pytestconfig.getoption("fresh_seed", default=False)
    if fresh:
        return {}
    return _load_cache(pytestconfig)


@pytest.fixture(scope="session")
def seed_cache_mut(pytestconfig: pytest.Config, seed_cache: dict) -> dict:
    """session 级别：整个测试会话共享一份缓存，结束时统一写盘。"""
    yield seed_cache
    _save_cache(pytestconfig, seed_cache)


# ---------------------------------------------------------------------------
# Admin 工具：注册 / 创建地址 / 创建扩展表（以普通用户身份）
# ---------------------------------------------------------------------------

def _register_user(
    client: ApiClient,
    username: str,
    password: str,
    phone: str,
    role_code: str | None = None,
) -> dict:
    """
    用 admin client 注册一个新用户（注册接口本身不需要登录态）。
    若传入 roleCode 则直接绑定对应角色（推荐），否则后端使用默认 GENERAL_USER。
    """
    payload = {
        "username": username,
        "password": password,
        "phone": phone,
    }
    if role_code:
        payload["roleCode"] = role_code
    resp = client.post("/admin/register/user", json=payload)
    resp.raise_for_status()
    return resp.json()


def _ensure_address(client: ApiClient, lon_min: float | None = None, lon_max: float | None = None,
                    lat_min: float | None = None, lat_max: float | None = None) -> dict:
    """
    为当前登录用户创建一个地址。
    lon_min/lon_max/lat_min/lat_max 可传经纬度范围覆盖。
    返回 {"id": int, ...}
    """
    addr = generate_address(
        lon_min=lon_min, lon_max=lon_max, lat_min=lat_min, lat_max=lat_max,
    )
    return client.create_address(addr)


# ---------------------------------------------------------------------------
# 高层 seed fixtures
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session")
def seed_merchants(
    settings: TestSettings,
    seed_cache_mut: dict,
    pytestconfig: pytest.Config,
) -> list[dict]:
    """
    注册 N 个商家（默认 10），自动创建地址和商家扩展表。
    并发执行，MAX_SEED_WORKERS 控制并发数。

    配置优先级：命令行 > .env > 默认值
    """
    count = pytestconfig.seed_merchant_count
    fresh = pytestconfig.seed_fresh
    key = "merchants"

    if not fresh and key in seed_cache_mut and len(seed_cache_mut[key]) >= count:
        cached = seed_cache_mut[key][:count]
        # 从缓存加载时 client 未被序列化，需重新构建
        for m in cached:
            if "client" not in m:
                m["client"] = _make_authenticated_client(
                    settings, m["username"], m["password"],
                )
        print(f"\n[seed] 复用 {count} 个已有商家（--fresh-seed 可强制重建）")
        return cached

    merchants: list[dict] = seed_cache_mut.get(key, [])
    existing_usernames = {m["username"] for m in merchants}
    if merchants:
        print(f"\n[seed] 已有 {len(merchants)} 个商家，继续补充至 {count} 个...")

    lon_min = pytestconfig.getoption("--lon-min") or _env_float("SEED_LON_MIN")
    lon_max = pytestconfig.getoption("--lon-max") or _env_float("SEED_LON_MAX")
    lat_min = pytestconfig.getoption("--lat-min") or _env_float("SEED_LAT_MIN")
    lat_max = pytestconfig.getoption("--lat-max") or _env_float("SEED_LAT_MAX")

    templates = generate_merchants(count)
    pending = [tmpl for tmpl in templates if tmpl.username not in existing_usernames]
    if not pending:
        return merchants[:count]

    registered: dict[str, dict] = {}

    # 阶段一：并发注册
    def _phase1_reg(tmpl: MerchantTemplate) -> dict | None:
        try:
            client = ApiClient(
                base_url=settings.base_url,
                timeout=settings.timeout,
                business_path=settings.business_path,
            )
            client.set_login_params(
                username=tmpl.username,
                client_credential=settings.client_credential,
                password_encrypt_key=settings.password_encrypt_key,
                password_encrypt_enabled=settings.password_encrypt_enabled,
            )
            _register_user(
                client, tmpl.username, tmpl.password, tmpl.phone,
                role_code=ROLE_MERCHANT_CODE,
            )
            return {"client": client, "template": tmpl}
        except Exception as e:
            print(f"[seed] 商家注册失败 {tmpl.username}: {e}")
            return None

    print(f"[seed] 阶段一：并发注册 {len(pending)} 个商家（{MAX_SEED_WORKERS} 并发）...")
    with ThreadPoolExecutor(max_workers=MAX_SEED_WORKERS) as ex:
        futures = {ex.submit(_phase1_reg, tmpl): tmpl for tmpl in pending}
        for fut in as_completed(futures):
            result = fut.result()
            if result:
                registered[result["template"].username] = result

    if not registered:
        return merchants

    time.sleep(0.3)  # 等待数据库写入生效

    # 阶段二：并发登录 + 创建地址/商家扩展
    def _phase2_setup(item: dict) -> dict | None:
        client = item["client"]
        tmpl = item["template"]
        try:
            payload = client.login_as(password=tmpl.password, login_path=settings.login_path)
            client.set_bearer_token(payload["access_token"])
            user_id = payload.get("user_id")
            if not user_id:
                user_id = payload.get("user_info", {}).get("id")
            addr = _ensure_address(client, lon_min, lon_max, lat_min, lat_max)
            # 创建商家扩展（API 返回 {"code":0,"data":true}，成功为 True）
            merchant_id = None
            try:
                create_result = client.create_merchant(
                    merchant_name=tmpl.merchant_name,
                    contact_name=tmpl.username,
                    store_address_id=addr["id"],
                )
                # 如果返回的是布尔值（True = 成功），则需要额外查询获取 merchantId
                if isinstance(create_result, bool) and create_result:
                    try:
                        # 查询商家列表，取最新创建的（第一条）
                        merchant_list = client.list_merchants(page=1, size=1)
                        if merchant_list and "records" in merchant_list and merchant_list["records"]:
                            merchant_id = merchant_list["records"][0].get("id")
                    except Exception:
                        pass
                elif isinstance(create_result, dict):
                    merchant_id = create_result.get("id")
            except Exception as e:
                # 如果创建失败（可能已存在），尝试查询现有商家
                if "already exists" in str(e).lower() or "已存在" in str(e):
                    try:
                        merchant_list = client.list_merchants(page=1, size=1)
                        if merchant_list and "records" in merchant_list and merchant_list["records"]:
                            merchant_id = merchant_list["records"][0].get("id")
                    except Exception:
                        pass

            return {
                "userId": user_id,
                "merchantId": merchant_id,
                "addressId": addr["id"],
                "username": tmpl.username,
                "password": tmpl.password,
                "phone": tmpl.phone,
                "merchantName": tmpl.merchant_name,
                "_client": client,
            }
        except Exception as e:
            print(f"[seed] 商家后续设置失败 {tmpl.username}: {e}")
            return None

    print(f"[seed] 阶段二：并发登录+创建商家扩展（{MAX_SEED_WORKERS} 并发）...")
    with ThreadPoolExecutor(max_workers=MAX_SEED_WORKERS) as ex:
        futures = {ex.submit(_phase2_setup, item): item for item in registered.values()}
        for fut in as_completed(futures):
            result = fut.result()
            if result:
                client = result.pop("_client")
                merchants.append(result)
                result["client"] = client  # 原位补充，缓存写入时会通过 _save_cache 清理

    seed_cache_mut[key] = merchants
    print(f"[seed] 共注册了 {len(merchants)} 个商家")
    return merchants


@pytest.fixture(scope="session")
def seed_customers(
    settings: TestSettings,
    seed_cache_mut: dict,
    pytestconfig: pytest.Config,
) -> list[dict]:
    """
    注册 N 个客户（默认 50），自动创建地址和客户扩展表。
    并发执行，MAX_SEED_WORKERS 控制并发数。

    配置优先级：命令行 > .env > 默认值
    """
    count = pytestconfig.seed_customer_count
    fresh = pytestconfig.seed_fresh
    key = "customers"

    if not fresh and key in seed_cache_mut and len(seed_cache_mut[key]) >= count:
        cached = seed_cache_mut[key][:count]
        for c in cached:
            if "client" not in c:
                c["client"] = _make_authenticated_client(
                    settings, c["username"], c["password"],
                )
        print(f"\n[seed] 复用 {count} 个已有客户（--fresh-seed 可强制重建）")
        return cached

    customers: list[dict] = seed_cache_mut.get(key, [])
    existing_usernames = {c["username"] for c in customers}
    if customers:
        print(f"\n[seed] 已有 {len(customers)} 个客户，继续补充至 {count} 个...")

    lon_min = pytestconfig.getoption("--lon-min") or _env_float("SEED_LON_MIN")
    lon_max = pytestconfig.getoption("--lon-max") or _env_float("SEED_LON_MAX")
    lat_min = pytestconfig.getoption("--lat-min") or _env_float("SEED_LAT_MIN")
    lat_max = pytestconfig.getoption("--lat-max") or _env_float("SEED_LAT_MAX")

    templates = generate_customers(count)
    pending = [tmpl for tmpl in templates if tmpl.username not in existing_usernames]
    if not pending:
        return customers[:count]

    registered: dict[str, dict] = {}

    # 阶段一：并发注册
    def _phase1_reg(tmpl: CustomerTemplate) -> dict | None:
        try:
            client = ApiClient(
                base_url=settings.base_url,
                timeout=settings.timeout,
                business_path=settings.business_path,
            )
            client.set_login_params(
                username=tmpl.username,
                client_credential=settings.client_credential,
                password_encrypt_key=settings.password_encrypt_key,
                password_encrypt_enabled=settings.password_encrypt_enabled,
            )
            _register_user(
                client, tmpl.username, tmpl.password, tmpl.phone,
                role_code=ROLE_CUSTOMER_CODE,
            )
            return {"client": client, "template": tmpl}
        except Exception as e:
            print(f"[seed] 客户注册失败 {tmpl.username}: {e}")
            return None

    print(f"[seed] 阶段一：并发注册 {len(pending)} 个客户（{MAX_SEED_WORKERS} 并发）...")
    with ThreadPoolExecutor(max_workers=MAX_SEED_WORKERS) as ex:
        futures = {ex.submit(_phase1_reg, tmpl): tmpl for tmpl in pending}
        for fut in as_completed(futures):
            result = fut.result()
            if result:
                registered[result["template"].username] = result

    if not registered:
        return customers

    time.sleep(0.3)

    # 阶段二：并发登录 + 创建地址/客户扩展
    def _phase2_setup(item: dict) -> dict | None:
        client = item["client"]
        tmpl = item["template"]
        try:
            payload = client.login_as(password=tmpl.password, login_path=settings.login_path)
            client.set_bearer_token(payload["access_token"])
            user_id = payload.get("user_id")
            if not user_id:
                user_id = payload.get("user_info", {}).get("id")
            addr = _ensure_address(client, lon_min, lon_max, lat_min, lat_max)
            # 创建客户扩展
            customer_id = None
            try:
                create_result = client.create_customer(
                    real_name=tmpl.real_name,
                    default_address_id=addr["id"],
                )
                # 如果返回的是布尔值（True = 成功），则查询获取 customerId
                if isinstance(create_result, bool) and create_result:
                    try:
                        customer_info = client.get_current_customer()
                        customer_id = customer_info.get("id")
                    except Exception:
                        customer_id = None
                elif isinstance(create_result, dict):
                    # API 返回了完整对象
                    customer_id = create_result.get("id")
            except Exception as e:
                # 如果创建失败（可能已存在），尝试查询现有客户
                if "already exists" in str(e).lower() or "已存在" in str(e):
                    try:
                        customer_info = client.get_current_customer()
                        customer_id = customer_info.get("id")
                    except Exception:
                        pass

            return {
                "userId": user_id,
                "customerId": customer_id,
                "addressId": addr["id"],
                "username": tmpl.username,
                "password": tmpl.password,
                "phone": tmpl.phone,
                "realName": tmpl.real_name,
                "_client": client,
            }
        except Exception as e:
            print(f"[seed] 客户后续设置失败 {tmpl.username}: {e}")
            return None

    print(f"[seed] 阶段二：并发登录+创建客户扩展（{MAX_SEED_WORKERS} 并发）...")
    with ThreadPoolExecutor(max_workers=MAX_SEED_WORKERS) as ex:
        futures = {ex.submit(_phase2_setup, item): item for item in registered.values()}
        for fut in as_completed(futures):
            result = fut.result()
            if result:
                client = result.pop("_client")
                customers.append(result)
                result["client"] = client

    seed_cache_mut[key] = customers
    print(f"[seed] 共注册了 {len(customers)} 个客户")
    return customers


@pytest.fixture(scope="session")
def seed_deliveries(
    settings: TestSettings,
    seed_cache_mut: dict,
    pytestconfig: pytest.Config,
) -> list[dict]:
    """
    注册 N 个骑手（默认 10），自动创建骑手扩展表。
    并发执行，MAX_SEED_WORKERS 控制并发数。

    配置优先级：命令行 > .env > 默认值
    """
    count = pytestconfig.seed_delivery_count
    fresh = pytestconfig.seed_fresh
    key = "deliveries"

    if not fresh and key in seed_cache_mut and len(seed_cache_mut[key]) >= count:
        cached = seed_cache_mut[key][:count]
        for d in cached:
            if "client" not in d:
                d["client"] = _make_authenticated_client(
                    settings, d["username"], d["password"],
                )
        print(f"\n[seed] 复用 {count} 个已有骑手（--fresh-seed 可强制重建）")
        return cached

    deliveries: list[dict] = seed_cache_mut.get(key, [])
    existing_usernames = {d["username"] for d in deliveries}
    if deliveries:
        print(f"\n[seed] 已有 {len(deliveries)} 个骑手，继续补充至 {count} 个...")

    templates = generate_deliveries(count)
    pending = [tmpl for tmpl in templates if tmpl.username not in existing_usernames]
    if not pending:
        return deliveries[:count]

    registered: dict[str, dict] = {}

    # 阶段一：并发注册
    def _phase1_reg(tmpl: DeliveryTemplate) -> dict | None:
        try:
            client = ApiClient(
                base_url=settings.base_url,
                timeout=settings.timeout,
                business_path=settings.business_path,
            )
            client.set_login_params(
                username=tmpl.username,
                client_credential=settings.client_credential,
                password_encrypt_key=settings.password_encrypt_key,
                password_encrypt_enabled=settings.password_encrypt_enabled,
            )
            _register_user(
                client, tmpl.username, tmpl.password, tmpl.phone,
                role_code=ROLE_DELIVERY_CODE,
            )
            return {"client": client, "template": tmpl}
        except Exception as e:
            print(f"[seed] 骑手注册失败 {tmpl.username}: {e}")
            return None

    print(f"[seed] 阶段一：并发注册 {len(pending)} 个骑手（{MAX_SEED_WORKERS} 并发）...")
    with ThreadPoolExecutor(max_workers=MAX_SEED_WORKERS) as ex:
        futures = {ex.submit(_phase1_reg, tmpl): tmpl for tmpl in pending}
        for fut in as_completed(futures):
            result = fut.result()
            if result:
                registered[result["template"].username] = result

    if not registered:
        return deliveries

    time.sleep(0.3)

    # 阶段二：并发登录 + 创建骑手扩展
    def _phase2_setup(item: dict) -> dict | None:
        client = item["client"]
        tmpl = item["template"]
        try:
            payload = client.login_as(password=tmpl.password, login_path=settings.login_path)
            client.set_bearer_token(payload["access_token"])
            user_id = payload.get("user_id")
            if not user_id:
                user_id = payload.get("user_info", {}).get("id")
            # 创建骑手扩展
            delivery_id = None
            try:
                create_result = client.create_delivery_rider(
                    real_name=tmpl.real_name,
                    delivery_scope_km=5.0,
                )
                # 如果返回的是布尔值（True = 成功），则查询获取 deliveryId
                if isinstance(create_result, bool) and create_result:
                    try:
                        rider_info = client.get_current_rider()
                        delivery_id = rider_info.get("id")
                    except Exception:
                        delivery_id = None
                elif isinstance(create_result, dict):
                    # API 返回了完整对象
                    delivery_id = create_result.get("id")
            except Exception as e:
                # 如果创建失败（可能已存在），尝试查询现有骑手
                if "already exists" in str(e).lower() or "已存在" in str(e):
                    try:
                        rider_info = client.get_current_rider()
                        delivery_id = rider_info.get("id")
                    except Exception:
                        pass

            return {
                "userId": user_id,
                "deliveryId": delivery_id,
                "username": tmpl.username,
                "password": tmpl.password,
                "phone": tmpl.phone,
                "realName": tmpl.real_name,
                "client": client,
            }
        except Exception as e:
            print(f"[seed] 骑手后续设置失败 {tmpl.username}: {e}")
            return None

    print(f"[seed] 阶段二：并发登录+创建骑手扩展（{MAX_SEED_WORKERS} 并发）...")
    with ThreadPoolExecutor(max_workers=MAX_SEED_WORKERS) as ex:
        futures = {ex.submit(_phase2_setup, item): item for item in registered.values()}
        for fut in as_completed(futures):
            result = fut.result()
            if result:
                deliveries.append(result)

    seed_cache_mut[key] = deliveries
    print(f"[seed] 共注册了 {len(deliveries)} 个骑手")
    return deliveries


@pytest.fixture(scope="session")
def seed_dishes(
    settings: TestSettings,
    seed_cache_mut: dict,
    seed_merchants: list[dict],
    pytestconfig: pytest.Config,
) -> list[dict]:
    """
    为每个商家创建 N 道菜品，每个商家用自己的 token 创建。
    所有菜品并发创建，MAX_SEED_WORKERS 控制并发数。
    缓存：按 merchantUserId + dishName 去重，已存在则跳过。

    配置优先级：命令行 > .env > 默认值
    """
    fresh = pytestconfig.seed_fresh
    key = "dishes"

    if not fresh and key in seed_cache_mut and len(seed_cache_mut[key]) > 0:
        print(f"\n[seed] 复用已有菜品缓存（--fresh-seed 可强制重建）")
        return seed_cache_mut[key]

    # 已有菜品去重集合
    all_dishes: list[dict] = seed_cache_mut.get(key, [])
    existing_keys: set[str] = set()
    for d in all_dishes:
        existing_keys.add(f"{d.get('merchantUserId')}|{d.get('dishName')}")

    # 每个商家生成的菜品数量（命令行 > .env > 默认值20）
    dish_count = pytestconfig.seed_dishes_per_merchant

    # 收集所有待创建的菜品任务
    tasks: list[tuple] = []
    for idx, merchant in enumerate(seed_merchants):
        user_id = merchant["userId"]
        for tpl in generate_merchant_dishes(idx, dish_count=dish_count):
            key_str = f"{user_id}|{tpl.dish_name}"
            if key_str not in existing_keys:
                tasks.append((merchant, user_id, tpl))

    if not tasks:
        return all_dishes

    lock = threading.Lock()
    dish_count = [0]  # mutable container for closure

    def _create_one(task: tuple) -> dict | None:
        merchant, user_id, tpl = task
        try:
            resp = merchant["client"].create_dish(
                merchant_user_id=user_id,
                dish_name=tpl.dish_name,
                price=float(tpl.price),
                stock=tpl.stock,
                dish_desc=tpl.dish_desc,
                sale_status=tpl.sale_status,
                auto_generate_knowledge=False,
            )
            if resp.get("data") or resp.get("success"):
                entry = {
                    "merchantUserId": user_id,
                    "dishName": tpl.dish_name,
                    "price": float(tpl.price),
                    "stock": tpl.stock,
                    "saleStatus": tpl.sale_status,
                }
                with lock:
                    all_dishes.append(entry)
                    dish_count[0] += 1
                return entry
        except Exception as e:
            print(f"[seed] 菜品创建失败 {tpl.dish_name}: {e}")
        return None

    print(f"[seed] 并发创建 {len(tasks)} 道菜品（{MAX_SEED_WORKERS} 并发）...")
    with ThreadPoolExecutor(max_workers=MAX_SEED_WORKERS) as ex:
        futures = [ex.submit(_create_one, task) for task in tasks]
        for fut in as_completed(futures):
            fut.result()  # 等待完成，异常已在 _create_one 内处理

    seed_cache_mut[key] = all_dishes
    print(f"[seed] 共创建了 {dish_count[0]} 道新菜品，总菜品数 {len(all_dishes)}")
    return all_dishes


# ---------------------------------------------------------------------------
# 辅助：打印种子数据摘要
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session", autouse=True)
def _seed_summary(seed_merchants: list, seed_customers: list, seed_deliveries: list, seed_dishes: list) -> None:
    """自动打印种子数据摘要。"""
    total_dishes = len(seed_dishes)
    print("\n" + "=" * 60)
    print("  种子数据摘要")
    print("=" * 60)
    print(f"  商家  : {len(seed_merchants)} 个")
    print(f"  客户  : {len(seed_customers)} 个")
    print(f"  骑手  : {len(seed_deliveries)} 个")
    print(f"  菜品  : {total_dishes} 道（总计）")
    print("=" * 60)
