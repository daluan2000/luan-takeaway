# pig 项目总览

## 1. 项目定位
- 这是一个基于 Spring Cloud + Spring Authorization Server + Nacos 的企业级权限与管理平台。
- 支持 **微服务模式**（`pig-gateway` + 各业务服务）与 **单体模式**（`pig-boot`）两种运行方式。

## 2. 顶层模块
- `pig-register`：注册中心/配置中心（Nacos）
- `pig-gateway`：统一网关（路由、限流、请求预处理）
- `pig-auth`：认证授权中心（OAuth2）
- `pig-upms`：用户/角色/菜单/日志等权限管理
- `pig-common`：公共基础能力（安全、Feign、MyBatis、日志、XSS 等）
- `pig-boot`：单体启动聚合器

## 3. 典型实现流程（微服务）
1. 客户端请求进入 `pig-gateway`。
2. 网关根据路由规则转发（如 `/auth/**` -> `pig-auth`、`/admin/**` -> `pig-upms-biz`）。
3. `pig-auth` 负责认证、签发令牌；业务服务通过 `pig-common-security` 做资源鉴权。
4. 业务服务（如 `pig-upms-biz`）执行 Controller -> Service -> Mapper，落库 MySQL。
5. 服务间通过 `pig-upms-api` 的 Feign 接口通信。
6. 运行态配置与服务发现依赖 `pig-register`（Nacos）。

## 4. 典型实现流程（单体）
1. 启动 `pig-boot`。
2. 直接聚合 `pig-auth`、`pig-upms-biz` 等能力。
3. 保留统一安全与文档能力，但不依赖网关做跨服务路由。

## 5. 基础设施
- `db/pig.sql`：核心业务库初始化脚本（用户、角色、字典、日志、任务等）。
- `db/luan_config.sql`：Nacos 配置库初始化脚本（服务路由与各服务配置）。
- `docker-compose.yml`：本地一键拉起 MySQL、Redis、Nacos 及各服务。
