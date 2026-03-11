# pig-upms-api 子模块说明

## 子模块定位
- 提供 UPMS 的跨服务接口契约与共享模型。

## 关键实现
- 主要内容：`api/feign` 下的远程服务接口（如 `RemoteUserService`、`RemoteLogService`）。
- `RemoteLogService` 通过 `@FeignClient` 指向 `UPMS_SERVICE`，并定义 `/log/save` 等远程端点。
- 被 `pig-auth`、`pig-common-security`、`pig-common-log` 等模块复用。

## 实现流程
1. 调用方注入 `Remote*Service`。
2. 通过 OpenFeign + 服务发现定位 `pig-upms-biz`。
3. 发起 HTTP 调用并返回统一 `R<T>` 结构。
4. 调用方根据返回值执行后续流程（如鉴权、审计、缓存刷新）。
