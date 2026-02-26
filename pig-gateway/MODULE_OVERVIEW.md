# pig-gateway 模块说明

## 模块定位
- 系统统一入口网关，负责路径路由、请求预处理、限流与异常处理。

## 关键实现
- 启动入口：`PigGatewayApplication`。
- 全局过滤器：`PigRequestGlobalFilter`（清洗请求头、写入请求起始时间、重写 StripPrefix）。
- 配置类：`GatewayConfiguration`、`RateLimiterConfiguration`、`SpringDocConfiguration`。
- 路由定义主要来源于 Nacos 配置（如 `pig-gateway-dev.yml`）。

## 实现流程
1. 客户端请求进入网关。
2. 全局过滤器处理 Header 与路径。
3. 匹配路由规则（如 `/auth/**`、`/admin/**`）。
4. 负载均衡转发到目标服务（`lb://`）。
5. 异常由全局异常处理器统一响应。
