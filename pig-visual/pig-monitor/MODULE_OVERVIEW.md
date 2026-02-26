# pig-monitor 子模块说明

## 子模块定位
- 监控中心服务，负责展示系统中各服务实例状态与指标。

## 关键实现
- 启动入口：`PigMonitorApplication`，启用 `@EnableAdminServer`。
- 安全配置：`SecuritySecureConfig`（监控页面访问控制）。
- 被监控服务通过 `spring-boot-admin-starter-client` 主动注册。

## 实现流程
1. `pig-monitor` 启动并注册到 Nacos。
2. 各业务服务将自身运行信息上报到监控中心。
3. 运维侧在监控页面查看健康状态、指标与实例列表。
