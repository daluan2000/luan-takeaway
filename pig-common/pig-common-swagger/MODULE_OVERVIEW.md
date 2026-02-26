# pig-common-swagger 子模块说明

## 子模块定位
- 接口文档统一配置模块。

## 关键实现
- 提供 OpenAPI 元数据配置（如 `OpenAPIMetadataConfiguration`）。
- `openapi-config.yaml` 统一定义文档开关、网关地址、token 获取地址。

## 实现流程
1. 业务服务启用文档注解能力（如 `@EnablePigDoc`）。
2. 模块注入统一 OpenAPI 元数据与安全配置。
3. 网关与业务服务共同对外暴露文档端点。
