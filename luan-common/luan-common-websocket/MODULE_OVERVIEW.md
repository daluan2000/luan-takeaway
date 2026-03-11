# pig-common-websocket 子模块说明

## 子模块定位
- WebSocket 通信封装模块，支持本地与 Redis 分发。

## 关键实现
- 自动装配：`WebSocketAutoConfiguration`。
- 分发策略：`LocalMessageDistributorConfiguration`、`RedisMessageDistributorConfiguration`。
- 处理器配置：`WebSocketHandlerConfig`。

## 实现流程
1. 服务启动后注册 WebSocket 端点与消息分发器。
2. 客户端建立 WebSocket 连接。
3. 消息通过本地或 Redis 通道分发到目标会话/节点。
