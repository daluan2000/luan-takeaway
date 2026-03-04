# pig-common-security 子模块说明

## 子模块定位
- 安全基础模块，提供资源服务、用户加载、授权数据存储与内部安全机制。

## 关键实现
- 自动装配内容包括：
  - `PigUserDetailsServiceImpl` / `PigAppUserDetailsServiceImpl`
  - `PigRedisOAuth2AuthorizationService` / `PigRedisOAuth2AuthorizationConsentService`
  - `PigRemoteRegisteredClientRepository`
  - `PigSecurityInnerAspect`、`PigBootCorsProperties`
- 常用于 `@EnablePigResourceServer` 场景。

## 实现流程
1. 服务启动时自动注册安全相关 Bean。
2. 请求到达资源服务后进行令牌解析与权限鉴定。
3. 用户/客户端信息通过远程或缓存加载。
4. 授权与同意信息按配置存储（如 Redis）。

## 鉴权流程总结（前端到后端）

### 1）前端请求携带了什么
- 前端在请求拦截器中统一注入请求头：`Authorization: Bearer <token>`。
- 鉴权核心依赖 `Authorization` 头中的 Bearer Token。
- 代码中还预留了 `TENANT-ID`、`Enc-Flag` 头；其中 `TENANT-ID` 用于多租户场景，`Enc-Flag` 用于报文加密开关。

### 2）后端如何获取当前用户的 permission
- 资源服务器过滤链要求：
  - 对白名单（permitAll）URL 放行；
  - 其余请求必须先完成认证（`authenticated`）。
- Token 解析流程：
  - 使用 `PigBearerTokenExtractor` 提取 Token；
  - 优先从 `Authorization: Bearer ...` 获取，兼容 `access_token` 参数（GET/表单场景）。
- Token 内省流程：
  - `PigCustomOpaqueTokenIntrospector` 通过 `OAuth2AuthorizationService` 查询访问令牌对应授权记录；
  - 再通过 `PigUserDetailsService` 加载用户详情。
- 用户权限来源：
  - 远程调用 upms 的 `/user/info/query` 获取 `UserInfo`；
  - `UserInfo.permissions` 来自角色关联菜单后提取 `sys_menu.permission`；
  - 最终写入 `Authentication.authorities`，进入 `SecurityContext`。

### 3）后端如何验证“接口需要的 permission”
- 接口通过 `@HasPermission("xxx")` 声明需要的权限。
- `@HasPermission` 底层等价为：`@PreAuthorize("@pms.hasPermission('{value}'.split(','))")`。
- `PermissionService#hasPermission(...)` 从 `SecurityContextHolder` 取出当前 `Authentication`：
  - 读取用户 `authorities`；
  - 与注解声明的权限做匹配（`simpleMatch`，支持通配）；
  - 命中即放行，否则拒绝访问。

### 4）一句话串起来
- 前端携带 Bearer Token -> 资源服务器解析并内省 Token -> 拉取用户并组装 `authorities`（来源于 `sys_menu.permission`）-> 控制器方法上的 `@HasPermission` 调用 `@pms.hasPermission(...)` 与 `authorities` 比对 -> 通过则执行业务，否则返回无权限。
