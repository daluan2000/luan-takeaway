# pig-auth 模块业务流程与关键实现

## 1. 模块定位

`pig-auth` 是 Pig 微服务体系的统一认证授权中心，基于 Spring Authorization Server 实现，承担以下职责：

- OAuth2 协议端点处理（token/authorize/consent 等）
- 多授权模式认证（标准模式 + 扩展模式）
- Token 生成、存储、校验、注销
- 登录前安全处理（验证码、密码解密）
- 认证/退出审计日志事件发布

启动入口为 `PigAuthApplication`，开启了服务发现与 Feign 客户端能力，配置通过 Nacos 下发。

## 2. 核心组件总览

### 2.1 安全配置入口

- `AuthorizationServerConfiguration`
  - 限定安全链只处理 `/oauth2/**`
  - 注入过滤器：`ValidateCodeFilter`、`PasswordDecoderFilter`
  - 配置 token 端点成功/失败处理器
  - 配置授权码确认页 `/oauth2/confirm_access`
  - 配置授权服务（`OAuth2AuthorizationService`）与发行者（issuer）
  - 注入自定义扩展 grant 的 `AuthenticationProvider`

### 2.2 端点控制器

- `PigTokenEndpoint`
  - `/token/login`：授权码模式登录页（Freemarker）
  - `/oauth2/confirm_access`：授权确认页
  - `/token/check_token`：校验 token 并返回 claims
  - `/token/logout`：当前 token 注销
  - `/token/remove/{token}`：内部调用删除 token
  - `/token/page`：分页查询在线 token
- `ImageCodeEndpoint`
  - `/code/image`：生成算术验证码并写入 Redis

### 2.3 自定义认证链

- Converter 层（请求 -> 认证 token）
  - `OAuth2ResourceOwnerPasswordAuthenticationConverter`
  - `OAuth2ResourceOwnerSmsAuthenticationConverter`
- Provider 层（认证 token -> access token）
  - `OAuth2ResourceOwnerPasswordAuthenticationProvider`
  - `OAuth2ResourceOwnerSmsAuthenticationProvider`
  - 抽象基类：`OAuth2ResourceOwnerBaseAuthenticationProvider`
- 用户名密码认证提供者
  - `PigDaoAuthenticationProvider`（动态选择 `PigUserDetailsService`）

### 2.4 Token 生成增强

- `CustomeOAuth2AccessTokenGenerator`
  - 生成 REFERENCE 格式 access token
  - 组装标准 claims（iss/sub/aud/iat/exp/scope）
- `CustomeOAuth2TokenCustomizer`
  - 增强 claims：license、clientId、用户信息（非 client_credentials）

### 2.5 事件与处理器

- `PigAuthenticationSuccessEventHandler`：登录成功返回 token + 发布登录成功日志
- `PigAuthenticationFailureEventHandler`：登录失败统一响应 + 发布失败日志
- `PigLogoutSuccessEventHandler`：监听退出事件并记录审计日志
- `FormAuthenticationFailureHandler`：表单登录失败重定向 `/token/login?error=...`
- `SsoLogoutSuccessHandler`：退出后按 `redirect_url` 或 `Referer` 跳转

## 3. 业务主流程

## 3.1 密码模式（grant_type=password）

1. 客户端调用 `/oauth2/token`。
2. 进入过滤器链：
   - `ValidateCodeFilter`：校验验证码（可按客户端白名单跳过）
   - `PasswordDecoderFilter`：对参数 `password` 做 AES 解密
3. `DelegatingAuthenticationConverter` 选择 `OAuth2ResourceOwnerPasswordAuthenticationConverter`，将请求转换为 `OAuth2ResourceOwnerPasswordAuthenticationToken`。
4. `OAuth2ResourceOwnerPasswordAuthenticationProvider` 执行认证：
   - 校验客户端是否允许 `password` 模式
   - 构造 `UsernamePasswordAuthenticationToken`
   - 交给 `AuthenticationManager`，由 `PigDaoAuthenticationProvider` 完成用户认证
5. `PigDaoAuthenticationProvider` 根据 `clientId + grantType` 动态选择 `PigUserDetailsService` 并验证密码。
6. 认证成功后，基类 Provider 生成 access token / refresh token，保存 `OAuth2Authorization`（底层由授权服务实现，项目中为 Redis 存储）。
7. `PigAuthenticationSuccessEventHandler` 输出统一 token 响应并发布登录成功日志事件。

## 3.2 短信模式（grant_type=mobile）

1. `/oauth2/token` 请求由 `OAuth2ResourceOwnerSmsAuthenticationConverter` 转成 `OAuth2ResourceOwnerSmsAuthenticationToken`。
2. `OAuth2ResourceOwnerSmsAuthenticationProvider` 校验客户端是否支持 `mobile`。
3. Provider 以手机号构造 `UsernamePasswordAuthenticationToken(phone, null)` 并走 `AuthenticationManager`。
4. 后续 token 生成、授权保存、成功/失败处理与密码模式一致。

## 3.3 授权码模式（前后端页面交互）

1. 登录页：`GET /token/login` 渲染 `templates/ftl/login.ftl`。
2. 表单提交到 `POST /oauth2/form`（由 `FormIdentityLoginConfigurer` 配置）。
3. 失败由 `FormAuthenticationFailureHandler` 重定向回登录页并带错误信息。
4. 需要用户授权时，`GET /oauth2/confirm_access` 渲染确认页 `confirm.ftl`，展示客户端请求的 scope。
5. 用户确认后完成授权码流程并换取 token。

## 3.4 Token 校验与注销

- `GET /token/check_token`
  - 按 access_token 查询 `OAuth2Authorization`
  - 不存在则返回认证失败
  - 存在则将 claims 转换并输出
- `DELETE /token/logout`
  - 从请求头提取 bearer token
  - 调用 `removeToken` 删除授权信息
  - 清理用户缓存并发布 `LogoutSuccessEvent`

## 4. 关键实现细节

### 4.1 过滤器前置安全

- `ValidateCodeFilter` 仅对 `/oauth2/token` 生效。
- `refresh_token` 请求直接放行，避免刷新链路受验证码影响。
- `ignoreClients` 支持按客户端跳过验证码，兼顾机器调用与安全策略。

### 4.2 动态用户体系适配

- `PigDaoAuthenticationProvider` 不直接绑定单一用户来源。
- 通过扫描所有 `PigUserDetailsService`，按 `support(clientId, grantType)` 选择最匹配实现（按 `Ordered` 最高优先级）。
- 支持同一认证中心对接多租户/多业务用户域。

### 4.3 统一异常语义

- `OAuth2ResourceOwnerBaseAuthenticationProvider` 将 `UsernameNotFoundException`、`BadCredentialsException`、`LockedException` 等转换为 OAuth2 标准错误码/扩展错误码。
- 对外返回保持协议统一，便于前端与网关做错误分流处理。

### 4.4 Token 载荷增强策略

- 对 `client_credentials` 不注入用户明细。
- 对用户态模式注入 `PigUser`、`user_id`、`username` 等 claims。
- 兼顾服务鉴权（client 维度）与业务鉴权（user 维度）需求。

### 4.5 审计日志闭环

- 登录成功、登录失败、退出成功都通过事件机制发布日志。
- 记录耗时、操作者、异常信息、服务标识，实现认证审计闭环。

## 5. 扩展点建议（基于当前实现）

1. 新增授权模式：
   - 新建 `AuthenticationConverter + AuthenticationToken + AuthenticationProvider`
   - 在 `accessTokenRequestConverter()` 与 `addCustomOAuth2GrantAuthenticationProvider()` 中注册
2. 新增 token 字段：
   - 在 `CustomeOAuth2TokenCustomizer` 中追加 claims
3. 增强登录前校验：
   - 在 `ValidateCodeFilter` 扩展设备指纹、滑块、人机校验等逻辑
4. 新增用户源：
   - 实现 `PigUserDetailsService` 并声明 `support(clientId, grantType)`

## 6. 一句话总结

`pig-auth` 通过“过滤器前置安全 + 可插拔 grant 扩展 + 统一 token 增强 + 事件化审计日志”形成了完整的企业级认证中心实现，既兼容 OAuth2 标准链路，也保留了业务侧灵活扩展能力。
