# pig-auth 模块说明

## 模块定位
- 认证授权中心，基于 Spring Authorization Server。
- 负责 OAuth2 协议处理、令牌签发、登录扩展能力（密码/短信等）。

## 关键实现
- 启动入口：`PigAuthApplication`（注册发现 + Feign 能力）。
- 核心配置：`AuthorizationServerConfiguration`。
- 扩展点：
  - `ValidateCodeFilter`（验证码校验）
  - `PasswordDecoderFilter`（密码解密）
  - 自定义 `AuthenticationConverter`/`AuthenticationProvider`（密码、短信登录）
  - 自定义 Token 生成器与 Token Customizer

## 实现流程
1. `/oauth2/**` 请求进入授权安全链。
2. 过滤器先处理验证码与密码解密。
3. Converter 将请求转换为认证 Token（密码、短信、刷新等）。
4. Provider 执行认证并调用授权服务保存授权信息（Redis 实现）。
5. 生成访问令牌/刷新令牌并返回。
