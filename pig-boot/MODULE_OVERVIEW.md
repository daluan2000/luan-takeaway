# pig-boot 模块说明

## 模块定位
- 单体模式启动器；用于在不拆分微服务时一次性启动核心功能。

## 关键实现
- 启动入口：`PigBootApplication`。
- 通过依赖聚合 `pig-auth`、`pig-upms-biz`、`pig-codegen`、`pig-quartz`。
- 启用资源服务与接口文档能力（`EnablePigResourceServer`、`EnablePigDoc`）。

## 实现流程
1. 启动 `PigBootApplication`。
2. Spring Boot 自动装配被聚合模块的 Bean。
3. 统一对外提供管理、认证、代码生成、定时任务等能力。
4. 与微服务模式相比，省去网关跨服务转发链路。
