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

## Swagger 访问地址速查

### 1) 单体启动（IDE 直接启动 `pig-boot`）
- `doc.html`：`http://127.0.0.1:9999/admin/doc.html`
- `swagger-ui`：`http://127.0.0.1:9999/admin/swagger-ui/index.html`
- OpenAPI：`http://127.0.0.1:9999/admin/v3/api-docs`

说明：`pig-boot` 启用了 `context-path=/admin`，因此文档路径都带 `/admin` 前缀。

### 2) 微服务启动（通过 `pig-gateway` 统一访问）
- `doc.html`：`http://127.0.0.1:9999/doc.html`
- `swagger-ui`：`http://127.0.0.1:9999/swagger-ui/index.html`
- 聚合 OpenAPI 路径前缀：`http://127.0.0.1:9999/v3/api-docs/**`

说明：微服务模式下推荐统一从网关入口访问文档页面与接口定义。
