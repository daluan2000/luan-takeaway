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
