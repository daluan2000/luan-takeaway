# Pig 前后端登录与 Token 授权流程（方法级）

本文基于当前仓库代码，按“前端登录 -> 后端认证签发 Token -> 携带 Token 访问资源 -> 方法级权限授权”完整梳理。

---

## 1. 前端发起登录（账号密码模式）

### 1.1 登录请求构造
- 文件：`pig-ui/src/stores/userInfo.ts`
- 方法：`useUserInfo.actions.login(data)`
  - 设置 `data.grant_type = 'password'`、`data.scope = 'server'`
  - 调用 `pig-ui/src/api/login/index.ts` 的 `login(data)`

- 文件：`pig-ui/src/api/login/index.ts`
- 方法：`login(data)`
  1. 组装客户端凭证头：`Authorization: Basic base64(clientId:clientSecret)`
  2. 可选密码加密：`other.encryption(data.password, VITE_PWD_ENC_KEY)`
  3. 发送 `POST /auth/oauth2/token`
  4. Header 包含：
     - `skipToken: true`（跳过请求拦截器自动加 Bearer）
     - `Authorization: Basic ...`
     - `Content-Type: application/x-www-form-urlencoded`

### 1.2 Token 持久化
- 文件：`pig-ui/src/stores/userInfo.ts`
- 方法：`useUserInfo.actions.login(data)`
  - 登录成功后保存：
    - `Session.set('token', res.access_token)`
    - `Session.set('refresh_token', res.refresh_token)`

---

## 2. 后端认证与签发 Token（auth 服务）

> `/auth/oauth2/token` 通过网关路由到认证服务，核心处理链在 Spring Security + Authorization Server 配置中。

### 2.1 安全链与 token 端点入口
- 文件：`pig-auth/src/main/java/com/pig4cloud/pig/auth/config/AuthorizationServerConfiguration.java`
- 方法：`authorizationServer(HttpSecurity http)`
  - `http.securityMatcher("/oauth2/**")`：仅匹配 OAuth2 端点
  - `tokenEndpoint.accessTokenRequestConverter(accessTokenRequestConverter())`：注册多种 grant 转换器
  - 在 `addCustomOAuth2GrantAuthenticationProvider(http)` 中注入：
    - `PigDaoAuthenticationProvider`（用户名密码校验）
    - `OAuth2ResourceOwnerPasswordAuthenticationProvider`（密码模式 OAuth2 provider）
    - `OAuth2ResourceOwnerSmsAuthenticationProvider`（短信模式）

### 2.2 password 请求转 Authentication
- 文件：`pig-auth/.../OAuth2ResourceOwnerPasswordAuthenticationConverter.java`
- 方法：
  - `support(String grantType)`：仅支持 `password`
  - `checkParams(HttpServletRequest request)`：强校验 `username/password`
  - `buildToken(...)`：构建 `OAuth2ResourceOwnerPasswordAuthenticationToken`

### 2.3 用户认证（账号密码校验）
- 文件：`pig-auth/.../OAuth2ResourceOwnerBaseAuthenticationProvider.java`
- 方法：`authenticate(Authentication authentication)`
  1. 校验客户端与 scope
  2. `buildToken(reqParameters)` 构建 `UsernamePasswordAuthenticationToken`
  3. 调用 `authenticationManager.authenticate(...)` 触发 DAO 认证

- 文件：`pig-auth/.../PigDaoAuthenticationProvider.java`
- 方法：
  - `retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)`
    - 根据 `clientId + grantType` 选择 `PigUserDetailsService`
    - 调用 `loadUserByUsername` 查询用户
  - `additionalAuthenticationChecks(...)`
    - 当 `grant_type=password` 时，使用 `PasswordEncoder.matches` 校验密码

### 2.4 Token 生成与落库
- 文件：`pig-auth/.../AuthorizationServerConfiguration.java`
- 方法：`oAuth2TokenGenerator()`
  - 使用 `CustomeOAuth2AccessTokenGenerator + OAuth2RefreshTokenGenerator`

- 文件：`pig-auth/.../CustomeOAuth2AccessTokenGenerator.java`
- 方法：`generate(OAuth2TokenContext context)`
  - 生成 access token（REFERENCE/Opaque token）
  - 设置 `sub/aud/iat/exp/scope` 等 claims

- 文件：`pig-auth/.../CustomeOAuth2TokenCustomizer.java`
- 方法：`customize(OAuth2TokenClaimsContext context)`
  - 注入自定义 claims：`client_id`、`user`、`user_id`、`username` 等

- 文件：`pig-auth/.../OAuth2ResourceOwnerBaseAuthenticationProvider.java`
- 方法：`authenticate(...)`
  - `authorizationService.save(authorization)`：保存授权对象（包含 access token/refresh token）
  - 返回 `OAuth2AccessTokenAuthenticationToken` 给前端

---

## 3. 前端携带 Token 发起业务请求

### 3.1 统一加 Bearer Token
- 文件：`pig-ui/src/utils/request.ts`
- 位置：`service.interceptors.request.use(...)`
  - `const token = Session.getToken()`
  - 若存在 token 且未设置 `skipToken`，自动加头：
    - `Authorization: Bearer ${token}`

### 3.2 Token 续期
- 文件：`pig-ui/src/api/login/index.ts`
- 方法：`checkToken(refreshTime, refreshLock)`
  - 调用 `GET /auth/token/check_token?token=...`
  - 若剩余有效期小于 30 分钟，触发 `useUserInfo().refreshToken()`

- 文件：`pig-ui/src/stores/userInfo.ts`
- 方法：`refreshToken()`
  - 调用 `refreshTokenApi(refresh_token)` -> `POST /auth/oauth2/token`（grant_type=refresh_token）
  - 更新本地 `token/refresh_token`

---

## 4. 携带 Token 请求的后端授权流程（资源服务）

### 4.1 启用资源服务器
- 文件：`pig-common/pig-common-security/src/main/java/com/pig4cloud/pig/common/security/annotation/EnablePigResourceServer.java`
- 说明：业务微服务启动类加 `@EnablePigResourceServer` 后导入：
  - `PigResourceServerAutoConfiguration`
  - `PigResourceServerConfiguration`
  - `PigFeignClientConfiguration`

### 4.2 HTTP 级鉴权（是否已登录）
- 文件：`pig-common/.../PigResourceServerConfiguration.java`
- 方法：`resourceServer(HttpSecurity http)`
  - `authorizeHttpRequests(...).anyRequest().authenticated()`：除白名单外都需认证
  - `oauth2ResourceServer().opaqueToken(token -> token.introspector(customOpaqueTokenIntrospector))`
  - `bearerTokenResolver(pigBearerTokenExtractor)`：自定义 token 提取器

- 文件：`pig-common/.../PigBearerTokenExtractor.java`
- 方法：`resolve(HttpServletRequest request)`
  - 白名单 URL 直接返回 `null`
  - 从 `Authorization: Bearer xxx` 或参数 `access_token` 解析 token
  - 格式不合法时抛 `OAuth2AuthenticationException`

### 4.3 Token 内省（token -> 当前用户）
- 文件：`pig-common/.../PigCustomOpaqueTokenIntrospector.java`
- 方法：`introspect(String token)`
  1. `authorizationService.findByToken(token, ACCESS_TOKEN)` 查 token
  2. 不存在则抛 `InvalidBearerTokenException`
  3. 客户端模式直接返回客户端主体
  4. 用户模式：从授权属性中取出历史 principal，回查/重建 `PigUser`
  5. 将 `client_id` 注入 `PigUser.attributes`，返回给 SecurityContext

---

## 5. 方法级授权（是否有某个权限）

### 5.1 注解到表达式
- 文件：`pig-common/.../HasPermission.java`
- 注解：`@PreAuthorize("@pms.hasPermission('{value}'.split(','))")`
  - 控制器方法上的 `@HasPermission("xxx")` 会转到 `pms.hasPermission(...)`

### 5.2 权限判定实现
- 文件：`pig-common/.../PigResourceServerAutoConfiguration.java`
- 方法：`permissionService()`
  - 注册 Bean 名称 `pms`

- 文件：`pig-common/.../PermissionService.java`
- 方法：`hasPermission(String... permissions)`
  - 从 `SecurityContextHolder.getContext().getAuthentication()` 取当前用户权限集
  - 与注解中权限表达式做 `PatternMatchUtils.simpleMatch`
  - 命中任一权限即允许

### 5.3 业务接口示例
- 文件：`pig-upms/pig-upms-biz/src/main/java/com/pig4cloud/pig/admin/controller/SysUserController.java`
- 方法：
  - `saveUser(...)`：`@HasPermission("sys_user_add")`
  - `updateUser(...)`：`@HasPermission("sys_user_edit")`
  - `userDel(...)`：`@HasPermission("sys_user_del")`

这意味着：
1. 请求先通过 token 认证（确认“是谁”）
2. 再执行 `@HasPermission`（确认“能做什么”）

---

## 6. 一条完整时序（密码登录 + 调用受保护接口）

1. 前端 `useUserInfo.login` -> `api/login.login` -> `POST /auth/oauth2/token`
2. 认证服务 `AuthorizationServerConfiguration.authorizationServer` 接管请求
3. `OAuth2ResourceOwnerPasswordAuthenticationConverter` 校验参数并构建认证对象
4. `OAuth2ResourceOwnerPasswordAuthenticationProvider.authenticate` 执行认证流程
5. `PigDaoAuthenticationProvider.retrieveUser/additionalAuthenticationChecks` 完成用户+密码校验
6. `CustomeOAuth2AccessTokenGenerator.generate` + `CustomeOAuth2TokenCustomizer.customize` 生成并增强 token
7. `authorizationService.save` 持久化授权信息，响应 `access_token/refresh_token`
8. 前端保存 token；后续请求由 `request.ts` 自动加 `Authorization: Bearer ...`
9. 资源服务 `PigBearerTokenExtractor.resolve` 提取 token
10. `PigCustomOpaqueTokenIntrospector.introspect` 内省 token，构建当前登录主体
11. 到达控制器方法时，`@HasPermission` -> `PermissionService.hasPermission` 判定权限
12. 权限通过则执行业务方法，否则返回无权限响应

---

## 7. 认证 vs 授权（在本项目中的边界）

- 认证（Authentication）
  - 关注“你是谁”
  - 关键方法：
    - `PigDaoAuthenticationProvider.retrieveUser`
    - `PigDaoAuthenticationProvider.additionalAuthenticationChecks`
    - `PigCustomOpaqueTokenIntrospector.introspect`

- 授权（Authorization）
  - 关注“你能做什么”
  - 关键方法：
    - `PermissionService.hasPermission`
    - `@HasPermission`（基于 `@PreAuthorize`）

以上就是当前代码实现下，前后端登录与 token 鉴权/授权的完整方法级流程。