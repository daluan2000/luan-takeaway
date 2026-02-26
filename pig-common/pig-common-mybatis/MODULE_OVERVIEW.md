# pig-common-mybatis 子模块说明

## 子模块定位
- MyBatis Plus 公共扩展模块。

## 关键实现
- 自动装配：`MybatisAutoConfiguration`。
- 提供分页、基础拦截、SQL 解析与通用 Mapper 相关配置。

## 实现流程
1. 业务服务引入后自动注册 MyBatis 相关 Bean。
2. Mapper 执行 SQL 时统一走扩展拦截与配置。
3. 输出统一的数据访问行为（分页、逻辑删除等）。
