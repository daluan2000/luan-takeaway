# pig-common-log 子模块说明

## 子模块定位
- 日志采集与远程落库能力模块。

## 关键实现
- 自动装配：`LogAutoConfiguration`。
- 依赖 `pig-upms-api` 中的 `RemoteLogService` 进行远程日志保存。

## 实现流程
1. 业务操作触发日志切面/组件采集。
2. 组装日志上下文（用户、请求、结果、耗时）。
3. 通过 Feign 调用 UPMS 日志接口异步或同步落库。
