# pig-common-feign 子模块说明

## 子模块定位
- OpenFeign 统一增强模块。

## 关键实现
- 自动装配：`PigFeignAutoConfiguration`、`SentinelAutoConfiguration`。
- 核心能力：内部调用标识、请求拦截、熔断/降级、统一异常处理。
- 注解支持：`@EnablePigFeignClients`。

## 实现流程
1. 服务启用 `@EnablePigFeignClients`。
2. Feign 请求通过拦截器注入公共 Header/内部调用标识。
3. 调用失败时由 Sentinel 与全局异常处理接管。
4. 调用方获得统一结构结果并继续业务流程。
