# pig-visual 模块说明

## 模块定位
- 运维与开发辅助能力集合，包含监控、代码生成、任务调度三个子系统。

## 子模块
- `pig-monitor`：服务健康监控（Spring Boot Admin）。
- `pig-codegen`：图形化代码生成。
- `pig-quartz`：定时任务管理与执行。

## 实现流程
1. 各子模块作为独立服务注册到 Nacos。
2. 通过网关路由（如 `/gen/**`、`/job/**`）对外提供能力。
3. 复用 `pig-common` 的鉴权、日志、数据访问和文档组件。
