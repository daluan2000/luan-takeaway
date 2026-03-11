# pig-common-seata 子模块说明

## 子模块定位
- 分布式事务支持模块。

## 关键实现
- 自动装配：`SeataAutoConfiguration`。
- 配套配置文件：`seata-config.yml`。

## 实现流程
1. 业务模块引入本模块并启用 Seata 配置。
2. 跨服务事务由 Seata 协调。
3. 在全局事务中统一提交或回滚。
