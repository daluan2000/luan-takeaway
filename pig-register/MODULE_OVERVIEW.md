# pig-register 模块说明

## 模块定位
- 注册中心与配置中心模块，基于 Nacos（内嵌启动方式）。

## 关键实现
- 启动入口：`PigNacosApplication`。
- 依赖 `nacos-console` 与 `nacos-server`，在同进程中分阶段启动 Core/Web/Console。
- Docker 运行时对外暴露 8848/9848/8080 端口。

## 实现流程
1. 启动时设置 Nacos 单机模式与日志配置。
2. 依次启动 Nacos Core、Web、Console 上下文。
3. 其他服务向 Nacos 注册并拉取配置。
4. 网关与各业务服务通过服务发现实现动态路由与调用。
