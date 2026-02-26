# pig-common-datasource 子模块说明

## 子模块定位
- 动态数据源能力模块，支持按上下文切换数据源。

## 关键实现
- 核心配置：`DynamicDataSourceAutoConfiguration`。
- 关键组件：`DynamicDataSourceProvider`、`DefaultDataSourceCreator`、`DsProcessor` 责任链。

## 实现流程
1. 启动时注册主数据源与动态数据源提供器。
2. 请求执行时由 `DsProcessor`（参数/请求头/session/spel）解析目标数据源。
3. 在当前线程上下文切换数据源后执行数据库操作。
4. 请求结束后通过过滤器清理上下文。
