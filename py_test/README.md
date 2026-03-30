# py_test API 测试脚手架

支持两种后端运行模式：
- `microservice`：微服务模式（通常通过网关访问）
- `monolith`：单体模式（`pig-boot`）

脚手架流程：
1. 调用 `/auth/oauth2/token` 登录获取 `access_token`（使用 `test:test` 客户端凭证，后端会自动忽略验证码）
2. 后续业务请求自动携带 `Authorization: Bearer <token>`

## 1. 安装依赖

```bash
cd py_test
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## 2. 配置

```bash
cp .env.example .env  # 如果需要自定义配置
```

编辑 `.env`：
- `API_MODE`：运行模式，`microservice`（默认）或 `monolith`
- `API_CLIENT_CREDENTIAL`（格式：`clientId:clientSecret`，必需，默认 `test:test` 用于忽略验证码）
- `API_PWD_ENC_KEY`（默认 `thanks,pig4cloud`，用于登录密码 AES 加密）
- `API_PWD_ENCRYPT_ENABLED`（默认 `true`，若后端关闭密码解密过滤器可设为 `false`）

所有种子测试用户（商家/客户/骑手）会在测试启动时自动注册，无需手动配置账号。

如需区分两种模式的地址/路径，编辑 `config.yaml` 中对应的 `modes.monolith` 和 `modes.microservice`。

## 3. 运行测试

### 方式 A：直接 pytest

```bash
pytest  # 从 .env 读取模式
pytest --mode monolith  # 强制指定模式
pytest --fresh-seed  # 强制重建种子数据
```

### 方式 B：启动器脚本

```bash
python run_tests.py         # 从 .env 读取模式（默认 --fresh-seed）
python run_tests.py --mode monolith  # 强制指定模式
```

## 4. 当前测试用例

### 认证与业务测试 (`test_auth_and_business.py`)
- `test_login_should_return_token` - 验证登录返回 token
- `test_user_info_with_token` - 验证带 token 获取用户信息

### 种子用户测试 (`test_seed_users.py`)
- `test_seed_merchants_10` - 验证商家注册数量
- `test_seed_merchant_detail_addresses` - 打印商家详细信息
- `test_seed_customers_50` - 验证客户注册数量
- `test_seed_customer_detail_addresses` - 打印客户详细信息
- `test_seed_deliveries_10` - 验证骑手注册数量
- `test_seed_all_types` - 验证所有类型种子数据

### 种子菜品测试 (`test_seed_dishes.py`)
- `test_seed_dishes_count` - 验证菜品数量
- `test_seed_dishes_detail` - 打印菜品详细信息
- `test_seed_dishes_merchant_coverage` - 验证菜品覆盖商家
- `test_seed_dishes_price_range` - 验证菜品价格范围
- `test_seed_dishes_status_distribution` - 验证上架/下架比例

## 5. 环境变量

```bash
# 种子数据数量
SEED_MERCHANT_COUNT=10
SEED_CUSTOMER_COUNT=50
SEED_DELIVERY_COUNT=10
SEED_DISHES_PER_MERCHANT=20

# 并发种子数
MAX_SEED_WORKERS=1000

# 地址经纬度范围（留空则自动推断全国范围）
# SEED_LON_MIN=112.7
# SEED_LON_MAX=114.3
# SEED_LAT_MIN=22.8
# SEED_LAT_MAX=23.7
```

## 6. API 路径对照表

| 功能 | 微服务模式路径 | 单体模式路径 |
|------|--------------|-------------|
| 登录 | `/auth/oauth2/token` | `/admin/oauth2/token` |
| 用户信息 | `/admin/user/info` | `/admin/user/info` |
| 注册用户 | `/admin/register/user` | `/admin/register/user` |
| 创建地址 | `/takeaway/user/address` | `/admin/user/address` |
| 商家申请 | `/takeaway/user/merchant/apply` | `/admin/user/merchant/apply` |
| 客户信息 | `/takeaway/user/customer` | `/admin/user/customer` |
| 骑手信息 | `/takeaway/user/delivery/rider` | `/admin/user/delivery/rider` |
| 创建菜品 | `/takeaway/dish` | `/admin/dish` |
