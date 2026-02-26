# db 模块说明

## 模块定位
- 提供项目运行所需的数据库初始化能力。
- 包含业务库 `pig` 与配置中心库 `pig_config` 的建库建表与初始数据。

## 关键内容
- `pig.sql`：系统业务表（如用户、角色、菜单、字典、日志、任务等）及初始化数据。
- `pig_config.sql`：Nacos 配置表与默认配置数据（包括 `pig-gateway-dev.yml` 路由、各服务配置）。
- `Dockerfile`：基于 MySQL 镜像，启动时自动执行上述 SQL 脚本。

## 实现流程
1. 容器启动后，MySQL 执行 `/docker-entrypoint-initdb.d` 下的 SQL。
2. 初始化 `pig`（业务数据）与 `pig_config`（Nacos 配置）数据库。
3. `pig-register` 连接 `pig_config` 读取配置，其他服务通过 Nacos 获取配置并启动。
