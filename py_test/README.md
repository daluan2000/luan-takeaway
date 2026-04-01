# Pytest 测试指南

本目录包含外卖系统的自动化测试用例，支持独立运行各个功能模块。

## 目录结构

```
py_test/
├── README.md                    # 本文档
├── run_tests.py                 # 测试启动脚本
├── config.yaml                  # 配置文件
├── .env                         # 环境变量配置
├── data/
│   ├── generators.py             # 批量导入数据生成器
│   ├── batch_users.json          # 批量用户数据
│   ├── batch_user_exts.json     # 用户扩展数据
│   └── batch_dishes.json         # 菜品数据
├── core/
│   ├── client.py                # API客户端封装
│   └── settings.py               # 配置加载
├── data/
│   ├── users.py                  # 用户数据生成
│   ├── dishes.py                 # 菜品数据生成
│   └── china_region.py           # 地址数据生成
└── tests/
    ├── test_batch_import.py      # 批量导入接口测试
    ├── test_seed_users.py         # 用户种子数据测试
    ├── test_seed_dishes.py        # 菜品种子数据测试
    └── test_auth_and_business.py  # 认证和业务测试
```

## 快速开始

### 1. 环境配置

确保 `.env` 文件包含以下配置：

```bash
# API配置
API_MODE=microservice
API_BASE_URL=http://localhost:8080
API_TIMEOUT=30
API_CLIENT_CREDENTIAL=app:secret
API_PWD_ENC_KEY=1234567890123456
API_PWD_ENCRYPT_ENABLED=false

# 种子数据配置
SEED_MERCHANT_COUNT=10
SEED_CUSTOMER_COUNT=50
SEED_DELIVERY_COUNT=10
SEED_DISHES_PER_MERCHANT=20
SEED_FRESH=false

# 并发配置
MAX_SEED_WORKERS=5

# 地址经纬度范围（可选）
SEED_LON_MIN=113.0
SEED_LON_MAX=114.5
SEED_LAT_MIN=22.4
SEED_LAT_MAX=23.5
```

### 2. 启动后端服务

确保后端服务已启动，默认地址：`http://localhost:8080`

### 3. 运行测试

#### 运行所有测试

```bash
python run_tests.py --mode microservice
```

#### 运行特定模块测试

```bash
# 批量导入测试
python -m pytest --mode microservice tests/test_batch_import.py -v

# 用户种子数据测试
python -m pytest --mode microservice tests/test_seed_users.py -v

# 菜品种子数据测试
python -m pytest --mode microservice tests/test_seed_dishes.py -v

# 认证和业务测试
python -m pytest --mode microservice tests/test_auth_and_business.py -v
```

---

## 功能模块详解

### 模块一：批量导入测试 (`test_batch_import.py`)

**功能**：测试管理员通过批量接口导入用户、扩展信息和菜品。

**测试用例**：
- `test_admin_login` - 管理员登录验证
- `test_batch_register_users` - 批量注册用户
- `test_batch_import_user_extensions` - 批量导入用户扩展信息
- `test_batch_import_dishes` - 批量导入菜品
- `test_batch_import_full_workflow` - 完整工作流测试
- `test_permission_check` - 权限验证

**前置条件**：
1. 管理员账号 `admin/123456` 存在
2. 运行数据生成器生成测试数据

**运行步骤**：

```bash
# 1. 生成测试数据文件
python -m data.generators --merchants 3 --customers 5 --deliveries 3 --dishes 10

# 2. 运行测试
python -m pytest --mode microservice tests/test_batch_import.py -v
```

**参数说明**：
```bash
--merchants N     # 商家数量（默认3）
--customers N     # 客户数量（默认5）
--deliveries N    # 骑手数量（默认3）
--dishes N        # 每个商家的菜品数量（默认10）
```

**输出文件**：
- `data/batch_users.json` - 批量注册用户数据
- `data/batch_user_exts.json` - 用户扩展信息数据
- `data/batch_dishes.json` - 菜品批量导入数据

---

### 模块二：用户种子数据测试 (`test_seed_users.py`)

**功能**：注册商家、客户、骑手用户，并创建对应的扩展信息（地址、客户资料、骑手资料）。

**测试用例**：
- `test_seed_merchants_10` - 商家注册验证
- `test_seed_merchant_detail_addresses` - 商家地址详情
- `test_seed_customers_50` - 客户注册验证
- `test_seed_customer_detail_addresses` - 客户详情
- `test_seed_deliveries_10` - 骑手注册验证
- `test_seed_all_types` - 全量种子数据验证

**前置条件**：
- 后端服务运行中

**运行步骤**：

```bash
# 运行所有用户种子测试
python -m pytest --mode microservice tests/test_seed_users.py -v

# 指定用户数量
python -m pytest --mode microservice tests/test_seed_users.py -v \
    --merchant-count 5 \
    --customer-count 20 \
    --delivery-count 5
```

**参数说明**：
```bash
--merchant-count N     # 商家数量（默认从.env读取）
--customer-count N     # 客户数量
--delivery-count N     # 骑手数量
--fresh-seed           # 强制重新生成种子数据
```

**缓存机制**：
- 种子数据自动缓存到 `.pytest_seed_cache/seed.json`
- 使用 `--fresh-seed` 可强制重建

---

### 模块三：菜品种子数据测试 (`test_seed_dishes.py`)

**功能**：为每个商家创建菜品，自动分配菜品到各商家。

**测试用例**：
- `test_seed_dishes_count` - 菜品数量验证
- `test_seed_dishes_detail` - 菜品详情
- `test_seed_dishes_merchant_coverage` - 商家菜品覆盖
- `test_seed_dishes_price_range` - 价格范围验证
- `test_seed_dishes_status_distribution` - 上架状态分布

**前置条件**：
- 商家种子数据已存在（依赖 `seed_merchants` fixture）

**运行步骤**：

```bash
# 运行菜品种子测试（自动依赖商家数据）
python -m pytest --mode microservice tests/test_seed_dishes.py -v

# 指定每个商家的菜品数量
python -m pytest --mode microservice tests/test_seed_dishes.py -v \
    --dishes-per-merchant 30
```

**参数说明**：
```bash
--dishes-per-merchant N    # 每个商家的菜品数量（默认20，支持范围如 15-25）
```

---

### 模块四：认证和业务测试 (`test_auth_and_business.py`)

**功能**：验证用户登录、Token认证、用户信息查询等基础功能。

**测试用例**：
- `test_login_should_return_token` - 登录返回Token
- `test_user_info_with_token` - Token验证用户信息

**前置条件**：
- 用户种子数据已存在

**运行步骤**：

```bash
python -m pytest --mode microservice tests/test_auth_and_business.py -v
```

---

## 数据生成器 (`generators.py`)

独立的非pytest模块，用于生成测试数据。

### 使用方式

```bash
# 生成测试数据文件
python -m data.generators --merchants 3 --customers 5 --deliveries 3 --dishes 10
```

### 参数说明

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `--merchants N` | 商家数量 | 3 |
| `--customers N` | 客户数量 | 5 |
| `--deliveries N` | 骑手数量 | 3 |
| `--dishes N` | 每个商家的菜品数量 | 10 |
| `--output DIR` | 输出目录 | data/ |

### 输出文件

| 文件 | 说明 |
|------|------|
| `batch_users.json` | 批量注册用户数据 |
| `batch_user_exts.json` | 用户扩展信息数据 |
| `batch_dishes.json` | 菜品批量导入数据 |

---

## 常用命令速查

```bash
# ========== 批量导入测试 ==========
# 生成数据文件
python -m data.generators --merchants 3 --customers 5 --deliveries 3 --dishes 10

# 运行批量导入测试
python -m pytest --mode microservice tests/test_batch_import.py -v

# ========== 种子数据测试 ==========
# 运行所有种子测试（用户+菜品）
python -m pytest --mode microservice tests/test_seed_users.py tests/test_seed_dishes.py -v

# 指定数量
python -m pytest --mode microservice tests/test_seed_users.py -v \
    --merchant-count 5 --customer-count 20 --delivery-count 5

# 强制重建种子数据
python -m pytest --mode microservice tests/test_seed_users.py -v --fresh-seed

# ========== 指定经纬度范围 ==========
python -m pytest --mode microservice tests/test_seed_users.py -v \
    --lon-min 113.0 --lon-max 114.5 --lat-min 22.4 --lat-max 23.5

# ========== 查看详细输出 ==========
python -m pytest --mode microservice -v -s

# ========== 仅收集测试不执行 ==========
python -m pytest --mode microservice --collect-only
```

---

## 故障排除

### Connection Refused 错误

```
ConnectionRefusedError: [Errno 111] Connection refused
```

**解决方案**：启动后端服务

```bash
# 检查服务是否运行
curl http://localhost:8080/actuator/health

# 启动服务（根据项目实际情况）
cd /path/to/project && ./start.sh
```

### 403 Forbidden 错误

**解决方案**：检查管理员权限配置

```bash
# 确认管理员账号存在
# 检查菜单权限配置是否包含批量操作权限
```

### 种子数据重复

**解决方案**：使用 `--fresh-seed` 强制重建

```bash
python -m pytest --mode microservice --fresh-seed -v
```

### 密码加密错误

**解决方案**：检查 `.env` 中的密码加密配置

```bash
API_PWD_ENCRYPT_ENABLED=false  # 测试环境建议关闭
```

---

## 测试数据说明

### 用户类型

| 类型 | RoleCode | 说明 |
|------|---------|------|
| 商家 | `ROLE_MERCHANT` | 拥有店铺，可创建菜品 |
| 客户 | `ROLE_CUSTOMER` | 普通消费者，可下单 |
| 骑手 | `ROLE_DELIVERY` | 配送员，可接单配送 |

### 扩展信息

| 类型 | 创建接口 | 必需字段 |
|------|---------|---------|
| 商家扩展 | `/user/merchant/apply` | merchantName, contactName, storeAddressId |
| 客户扩展 | `/user/customer` | realName, defaultAddressId |
| 骑手扩展 | `/user/delivery/rider` | realName |

### 菜品状态

| 状态码 | 说明 |
|--------|------|
| `1` / `SALE_ON` | 上架（可销售） |
| `0` / `SALE_OFF` | 下架（暂停销售） |
