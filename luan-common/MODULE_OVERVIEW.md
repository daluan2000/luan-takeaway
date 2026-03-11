# pig-common 模块说明

## 模块定位
- 公共能力聚合模块，提供基础组件、自动装配与统一规范。

## 子模块
- `pig-common-bom`：统一依赖版本与公共组件清单
- `pig-common-core`：核心工具与基础配置
- `pig-common-datasource`：动态数据源
- `pig-common-log`：日志采集与远程落库
- `pig-common-mybatis`：MyBatis Plus 扩展
- `pig-common-oss`：文件存储抽象
- `pig-common-seata`：分布式事务支持
- `pig-common-security`：安全与资源服务能力
- `pig-common-feign`：Feign 扩展
- `pig-common-swagger`：OpenAPI 文档聚合配置
- `pig-common-websocket`：WebSocket 封装
- `pig-common-xss`：XSS 防护
- `pig-common-excel`：Excel 导入导出能力

## 实现流程
1. 业务模块引入对应 `pig-common-*` 依赖。
2. Spring Boot 通过 `AutoConfiguration.imports` 自动注册配置。
3. 业务代码直接使用统一的安全、日志、数据访问、调用与工具能力。
