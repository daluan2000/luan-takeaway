# pig-codegen 子模块说明

## 子模块定位
- 代码生成服务，支持数据源配置、表结构读取、模板管理与代码打包导出。

## 关键实现
- 启动入口：`PigCodeGenApplication`（动态数据源 + 资源服务 + Feign + 文档）。
- 控制层：`GenTableController`、`GeneratorController`、`GenTemplateController` 等。
- 服务层：`GeneratorServiceImpl`、`GenTableServiceImpl`、`GenTemplateServiceImpl` 等。
- 技术栈：MyBatis Plus + Anyline + Velocity。

## 实现流程
1. 管理端配置数据源并选择库表。
2. 服务读取表元数据并与模板参数组装。
3. 基于 Velocity 渲染代码模板。
4. 生成结果打包并返回下载。
