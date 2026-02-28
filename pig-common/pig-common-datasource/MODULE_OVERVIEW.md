# pig-common-datasource 子模块说明

## 子模块定位
- 动态数据源能力模块，支持按上下文切换数据源。

## 关键实现
- 核心配置：`DynamicDataSourceAutoConfiguration`。
- 关键组件：`DynamicDataSourceProvider`、`DefaultDataSourceCreator`、`DsProcessor` 责任链。

## 实现流程
1. 启动时注册主数据源与动态数据源提供器。
2. 请求执行时由 `DsProcessor`（参数/请求头/session/spel）解析目标数据源。
3. 在当前线程上下文切换数据源后执行数据库操作。
4. 请求结束后通过过滤器清理上下文。



这个类是 **pig 项目动态数据源体系的核心自动配置类**。
一句话概括它的作用：

> ✅ 在 Spring Boot 启动时，把“动态数据源相关组件”全部装配进 Spring 容器。

它本身不写业务逻辑，而是做：

```text
注册 Bean → 组装数据源创建器 → 注册数据源提供者 → 配置数据源解析链 → 处理线程清理
```

---

# 一、这个类在 Spring 启动流程中的位置

```java
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DataSourceProperties.class)
```

意思是：

* 这是一个自动配置类
* 在 Spring Boot 默认数据源配置之后执行
* 绑定自定义的数据源配置

---

## Spring Boot 数据源启动顺序

```text
1️⃣ DataSourceAutoConfiguration（Spring 默认）
2️⃣ DynamicDataSourceAutoConfiguration（你这个类）
3️⃣ 覆盖默认 DataSource
```

它的作用就是：

> 在默认 DataSource 之后，接管数据源体系。

---

# 二、核心组件讲解

---

## ① dynamicDataSourceProvider()

```java
@Bean
public DynamicDataSourceProvider dynamicDataSourceProvider(...)
```

### 它是干什么的？

`DynamicDataSourceProvider` 负责：

> 从数据库 / 配置文件 / 远程配置中心加载“所有真实数据源”

比如：

```text
master
slave
tenant1
tenant2
```

你这个实现是：

```java
new JdbcDynamicDataSourceProvider(...)
```

说明：

> 数据源信息是从数据库里查出来的

而不是写死在 yml。

---

## ② masterDataSourceProvider()

```java
@Bean
public DynamicDataSourceProvider masterDataSourceProvider(...)
```

这个是：

> 专门提供“主数据源”的 Provider

常用于：

* 初始化数据库
* 启动时加载其他数据源信息

---

# 三、defaultDataSourceCreator()

```java
@Bean
public DefaultDataSourceCreator defaultDataSourceCreator(HikariDataSourceCreator druidDataSourceCreator)
```

这个方法非常关键。

### DataSourceCreator 是什么？

它是：

> 真实 DataSource 的“工厂”

你这里：

```java
creators.add(druidDataSourceCreator);
```

说明：

> 你用的是 Hikari 连接池

它最终会创建：

```text
HikariDataSource
```

所以流程是：

```text
Provider 读取数据源配置
   ↓
Creator 根据配置创建真实 DataSource
   ↓
交给 DynamicRoutingDataSource 管理
```

---

# 四、dsProcessor() —— 数据源解析责任链

这是高级部分 👇

```java
@Bean
public DsProcessor dsProcessor(BeanFactory beanFactory)
```

这里组装了一个：

> 责任链模式（Chain of Responsibility）

结构：

```text
LastParamDsProcessor
        ↓
HeaderProcessor
        ↓
SessionProcessor
        ↓
SpELProcessor
```

---

## 它解决什么问题？

当你写：

```java
@DS("#tenant")
```

或者：

```java
@DS("header:ds-name")
```

系统需要解析：

> 到底用哪个数据源？

解析顺序：

1️⃣ 从方法参数最后一个取
2️⃣ 从 HTTP Header 取
3️⃣ 从 Session 取
4️⃣ 从 SpEL 表达式解析

这是一套：

> 动态数据源 Key 解析机制

非常灵活。

---

# 五、ClearTtlDataSourceFilter()

```java
@Bean
public ClearTtlDataSourceFilter clearTtlDsFilter()
```

这是为了解决：

> ThreadLocal 泄漏问题

动态数据源底层依赖：

```text
ThreadLocal 保存当前数据源 key
```

如果你使用：

```text
线程池
异步任务
TTL（TransmittableThreadLocal）
```

可能会：

> 数据源 key 没被清除，导致串数据源

这个 Filter 的作用是：

```text
请求结束时清除数据源上下文
```

避免污染。

---

# 六、整体架构图

```text
                Spring Boot
                     ↓
     DynamicDataSourceAutoConfiguration
                     ↓
      ┌─────────────────────────────┐
      │ DataSourceProvider          │
      │ DataSourceCreator           │
      │ DsProcessor 责任链          │
      │ TTL 清理 Filter             │
      └─────────────────────────────┘
                     ↓
        DynamicRoutingDataSource
                     ↓
           真实 HikariDataSource
```

---

# 七、这个类的设计水平

从架构角度评价：

### 优点

* 分层清晰
* 使用责任链模式
* 使用工厂模式
* 使用自动配置
* 支持扩展

### 设计模式包含

| 模式    | 位置                        |
| ----- | ------------------------- |
| 工厂模式  | DataSourceCreator         |
| 策略模式  | 不同 Provider               |
| 责任链模式 | DsProcessor               |
| 单例模式  | Spring Bean               |
| 模板方法  | AbstractRoutingDataSource |

---

# 八、和你之前问的“写架构”对应关系

这个类属于：

> ✔ 写架构层代码
> ✔ 基础设施层
> ✔ 框架封装层

不是 CRUD。

它在做：

```text
抽象
扩展点设计
启动流程接管
组件装配
```

---

# 九、为什么你现在能看懂这个？

因为你最近在问：

* 动态数据源原理
* enum 配置设计
* Spring Boot 自动装配
* 架构分层

这说明你已经在往：

> 从“业务代码阅读” → “框架层理解”

过渡。

---

# 十、一句话总结

这个类的本质是：

> 动态数据源体系的“启动组装中心”。

它不直接切换数据源，它负责：

```text
把所有“切换所需组件”装进 Spring 容器
```

---

如果你愿意，我可以给你画一张：

* pig 动态数据源完整调用时序图
* 从 @DS 注解到获取真实数据库连接的完整流程

那你会彻底理解动态数据源是怎么工作的。
