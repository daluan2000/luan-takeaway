# Nacos 命名空间导致配置异常问题总结

## 问题现象

- 微服务可在 Nacos 中正常注册
- 但配置读取异常，`DataSource` 参数未加载（如 `spring.datasource.url` 缺失）
- 控制台中默认 `public(保留空间)` 显示配置数为 `0`

## 问题原因

核心原因是 **Nacos 命名空间不一致**：

1. 控制台默认 `public(保留空间)` 的真实 `namespaceId` 是空串 `""`
2. 业务配置数据写在 `config_info.tenant_id='public'`
3. 结果是“注册可见，但配置读不到”

即：配置读取落在 `namespaceId=""` 时，无法命中 `tenant_id='public'` 的配置。

## 为什么两种命名空间都能看到注册服务

这是 Nacos 的常见现象：

- **Naming（服务注册发现）** 对默认命名空间存在兼容/归一行为，空值与 `public` 可能都能查到同一批实例
- **Config（配置中心）** 按 `tenant_id` 更严格隔离，不匹配就会查不到配置

因此会出现：

- 两边都能看到服务数量
- 但只有 `namespaceId=public` 能看到你的配置

## 解决方案（已验证）

1. 在 Nacos 中创建并使用 `namespaceId=public` 命名空间
2. 将服务注册与配置读取统一到同一命名空间（`public`）
3. 保持配置数据持续写入 `tenant_id='public'`

你当前已验证：`namespaceId=public` 下服务与配置均正常，问题消失。

## 初始化保障（仓库已处理）

为避免重建后丢失命名空间，已在 `db/pig_config.sql` 增加：

- `tenant_info` 中的 `tenant_id='public'` 记录
- `tenant_capacity` 中的 `tenant_id='public'` 记录

这样全新初始化 Nacos 配置库时，会自动具备 `public` 命名空间元数据。

## 一句话结论

**本问题本质是命名空间 ID 不一致（`""` vs `"public"`）导致配置读取失败；统一使用 `namespaceId=public` 后恢复正常。**
