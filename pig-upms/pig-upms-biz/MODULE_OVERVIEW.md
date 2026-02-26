# pig-upms-biz 子模块说明

## 子模块定位
- UPMS 业务实现模块，承载用户、角色、菜单、字典、日志、客户端配置等核心管理能力。

## 关键实现
- 启动入口：`PigAdminApplication`（资源服务 + Feign + Nacos + OpenAPI）。
- 控制层：`SysUserController`、`SysRoleController`、`SysMenuController`、`SysLogController` 等。
- 服务层：`Sys*Service` / `Sys*ServiceImpl` 处理业务规则。
- 依赖能力：`pig-common-security`、`pig-common-log`、`pig-common-mybatis`、`pig-common-oss`、`pig-common-xss`。

## 实现流程
1. 请求经网关转发至 `/admin/**` 接口。
2. 资源服务先完成令牌解析与权限校验。
3. Controller 调用 Service 执行业务（增删改查、权限树构建、日志写入等）。
4. Service 通过 MyBatis Plus 与数据库交互。
5. 返回统一响应给前端或调用方；必要时通过 Feign 与其它服务协同。
