# pig-common-security 与 pig-auth 的功能与关系说明

## 1. 结论先说

- `pig-auth` 是**认证授权中心**（Authorization Server），负责签发/校验/注销令牌、处理登录流程和扩展授权模式。
- `pig-common-security` 是**安全能力基础层**（Security Foundation），沉淀用户加载、客户端加载、令牌存储、资源服务器鉴权、Feign 令牌透传等可复用能力。
- 二者关系是：`pig-auth` **依赖并复用** `pig-common-security` 提供的通用安全组件；`pig-common-security` 不依赖 `pig-auth`，但为整个系统（含 `pig-auth`）提供统一安全底座。

---

## 2. 各自功能边界

## 2.1 pig-auth：认证中心（做“发证”和“登录流程”）

`pig-auth` 主要围绕 OAuth2 授权服务器展开，关键能力如下：

1. **授权服务器配置**
   - 在 `AuthorizationServerConfiguration` 中配置 `/oauth2/**` 安全链。
   - 注入验证码、密码解密过滤器。
   - 注入自定义 token 端点成功/失败处理器。
   - 注册自定义授权模式 Provider（密码模式、短信模式）。

2. **授权模式扩展**
   - 支持标准模式 + 扩展模式（`password`、`mobile`）。
   - 通过 Converter + Provider 机制把请求转换为认证对象并完成认证。

3. **Token 生命周期管理**
   - 生成 access token / refresh token。
   - 提供 `/token/check_token`、`/token/logout`、`/token/remove/{token}` 等管理接口。

4. **登录交互与审计**
   - 授权码模式登录页与确认页。
   - 认证成功/失败、退出事件处理与日志上报。

> 简单说：`pig-auth` 负责“用户如何登录、令牌如何签发、签发后如何管理”。

## 2.2 pig-common-security：安全基础模块（做“通用安全组件”）

`pig-common-security` 提供跨微服务复用的安全能力，关键能力如下：

1. **用户加载体系（UserDetails）**
   - 提供 `PigUserDetailsService` 抽象接口及实现。
   - 支持按 `clientId + grantType` 动态选择用户加载实现。
   - 包含普通用户名与手机号（短信模式）等加载策略。

2. **客户端与授权存储能力**
   - `PigRemoteRegisteredClientRepository`：远程加载 OAuth 客户端配置并缓存。
   - `PigRedisOAuth2AuthorizationService`：基于 Redis 存储/查询/删除 OAuth2 授权信息。
   - `PigRedisOAuth2AuthorizationConsentService`：处理授权同意信息（consent）。

3. **资源服务器能力（给业务服务用）**
   - `@EnablePigResourceServer` 一键启用资源服务器配置。
   - `PigResourceServerConfiguration` 配置鉴权、免鉴权白名单、Opaque Token 内省等。
   - `PigCustomOpaqueTokenIntrospector` 根据 token 回查授权并恢复用户上下文。

4. **服务间调用安全增强**
   - `PigFeignClientConfiguration` + `PigOAuthRequestInterceptor` 完成 Feign 调用 token 透传。

> 简单说：`pig-common-security` 负责“把安全通用能力沉淀成可插拔底座，供 auth 与各业务服务复用”。

---

## 3. 二者关系：从“依赖关系 + 运行关系”看

## 3.1 编译期依赖关系（静态）

- `pig-auth/pom.xml` 明确依赖 `pig-common-security`。
- 因此 `pig-auth` 可以直接使用 `pig-common-security` 的：
  - 用户模型与用户加载接口（`PigUser`, `PigUserDetailsService`）
  - OAuth2 工具类与异常定义
  - CORS 配置属性
  - OAuth2 存储、客户端仓库等基础 Bean

这说明：`pig-auth` 不是“从零实现所有安全逻辑”，而是站在 `pig-common-security` 之上做认证中心编排。

## 3.2 运行期协作关系（动态）

典型链路（密码模式）可概括为：

1. 请求进入 `pig-auth` 的 `/oauth2/token`。
2. `pig-auth` 的过滤器先做验证码校验、密码解密。
3. `pig-auth` 的 `PigDaoAuthenticationProvider` 从 Spring 容器中查找 `PigUserDetailsService`。
4. 实际被选中的 `PigUserDetailsService` 实现来自 `pig-common-security`（按 grantType/clientId 匹配）。
5. 认证成功后，授权数据通过 `OAuth2AuthorizationService` 落库；默认实现来自 `pig-common-security` 的 Redis 实现。
6. 资源服务后续鉴权时，使用 `pig-common-security` 的资源服务器配置和 token introspector 回查授权与用户信息。

这说明：`pig-auth` 负责“认证编排”，`pig-common-security` 负责“认证与鉴权所需的可复用基础能力”。

---

## 4. 关键耦合点（最重要）

以下是二者最核心的连接点：

1. **用户认证耦合点**
   - `pig-auth` 的 `PigDaoAuthenticationProvider` 依赖 `PigUserDetailsService`。
   - `PigUserDetailsService` 的主要实现由 `pig-common-security` 提供。

2. **授权存储耦合点**
   - `pig-auth` 广泛依赖 `OAuth2AuthorizationService`（签发后保存、check_token、logout/remove）。
   - 实际实现为 `pig-common-security` 的 `PigRedisOAuth2AuthorizationService`。

3. **客户端配置耦合点**
   - 授权服务器需要 `RegisteredClientRepository`。
   - `pig-common-security` 提供 `PigRemoteRegisteredClientRepository`，从 UPMS 远程读取客户端配置并缓存。

4. **协议与错误语义耦合点**
   - `pig-auth` 的扩展 grant Provider/Converter 直接使用 `pig-common-security` 的 OAuth2 工具与扩展错误码。

5. **统一跨域配置耦合点**
   - `pig-auth` 的授权服务器配置使用 `PigBootCorsProperties`（来自 `pig-common-security`）。

---

## 5. 容易混淆的点

1. `pig-auth` 与 `pig-common-security` **不是同层模块**：
   - 前者是“业务型认证服务”；
   - 后者是“技术型安全基础库”。

2. `pig-common-security` **不等于**只给资源服务用：
   - 它既服务资源服务器，也服务授权服务器（`pig-auth`）。

3. token “校验接口在 auth”与“鉴权能力在 common-security”并不冲突：
   - auth 提供管理和协议端点；
   - common-security 提供内省、用户恢复、过滤器/拦截器等底座能力。

---

## 6. 一句话总结

`pig-auth` 是“认证中心应用层”，`pig-common-security` 是“安全基础设施层”；前者编排认证流程并暴露 OAuth2 端点，后者提供可复用的用户、客户端、token 存储与资源鉴权能力，二者形成“上层服务 + 下层安全底座”的关系。