# pig-visual 模块说明

## 模块定位
- 运维辅助能力集合，当前包含监控子系统。

## 子模块
- `pig-monitor`：服务健康监控（Spring Boot Admin）。

## 实现流程
1. 子模块作为独立服务注册到 Nacos。
2. 复用 `pig-common` 的鉴权、日志、数据访问和文档组件。
