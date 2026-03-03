# py_test API 测试脚手架

支持两种后端运行模式：
- `microservice`：微服务模式（通常通过网关访问）
- `monolith`：单体模式（`pig-boot`）

脚手架流程：
1. 先调用 `/auth/oauth2/token` 登录获取 `access_token`
2. 后续业务请求自动携带 `Authorization: Bearer <token>`

## 1. 安装依赖

```bash
cd py_test
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## 2. 配置账号与客户端

```bash
cp .env.example .env
```

编辑 `.env`：
- `API_USERNAME`
- `API_PASSWORD`
- `API_CLIENT_CREDENTIAL`（格式：`clientId:clientSecret`）
- `API_PWD_ENC_KEY`（默认 `thanks,pig4cloud`，用于登录密码 AES 加密）
- `API_PWD_ENCRYPT_ENABLED`（默认 `true`，若后端关闭密码解密过滤器可设为 `false`）

默认账号密码已设置为 `test_admin / 123456`，可按需覆盖。

如需区分两种模式的地址/路径，编辑 `config.yaml` 中对应的 `modes.monolith` 和 `modes.microservice`。

## 3. 运行测试

### 方式 A：直接 pytest

```bash
pytest --mode microservice -q
pytest --mode monolith -q
```

### 方式 B：启动器脚本

```bash
python run_tests.py --mode microservice -q
python run_tests.py --mode monolith -q
```

## 4. 当前示例用例

- `tests/test_auth_and_business.py::test_login_should_return_token`
- `tests/test_auth_and_business.py::test_user_info_with_token`
- `tests/test_media_upload.py::test_media_image_upload_should_succeed`
- `tests/test_media_upload.py::test_media_upload_non_image_type_should_fail`

业务示例接口默认是 `/admin/user/info`，你可以按模块继续新增更多用例。
