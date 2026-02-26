# pig-common-core 子模块说明

## 子模块定位
- 公共核心工具与基础 Spring 配置模块。

## 关键实现
- 自动装配入口：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。
- 主要自动配置：`JacksonConfiguration`、`RedisTemplateConfiguration`、`RestTemplateConfiguration`、`WebMvcConfiguration`。

## 实现流程
1. 业务服务引入本模块。
2. Spring Boot 自动注册序列化、Redis、RestTemplate、WebMVC 等基础 Bean。
3. 上层业务与其他 common 子模块复用这些基础能力。
