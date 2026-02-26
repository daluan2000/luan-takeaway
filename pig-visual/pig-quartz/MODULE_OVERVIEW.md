# pig-quartz 子模块说明

## 子模块定位
- 定时任务管理中心，提供任务定义、发布、执行与日志追踪能力。

## 关键实现
- 启动入口：`PigQuartzApplication`（资源服务 + Feign + 文档 + 服务发现）。
- Quartz 配置：`PigQuartzConfig`（调度器工厂、触发器、Job 注入）。
- 业务接口：`SysJobController`、`SysJobLogController`。
- 业务服务：`SysJobServiceImpl`、`SysJobLogServiceImpl`。

## 实现流程
1. 管理端创建或修改任务定义。
2. 服务将任务配置持久化并同步到 Quartz 调度器。
3. Quartz 按触发规则执行任务。
4. 执行结果写入任务日志并提供查询。
