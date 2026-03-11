# pig-upms 模块说明

## 模块定位
- 用户与权限管理中心，包含领域 API 与业务实现两层。

## 子模块
- `pig-upms-api`：跨服务可复用的实体、常量、Feign API。
- `pig-upms-biz`：实际业务服务实现（用户、角色、菜单、日志、文件、字典等）。

## 实现流程
1. 其他服务通过 `pig-upms-api` 定义的 Feign 接口远程调用 UPMS。
2. `pig-upms-biz` 提供 REST 接口并执行权限/审计校验。
3. Service 层处理业务规则，Mapper 层访问 MySQL。
4. 结果通过统一响应对象返回调用方。
