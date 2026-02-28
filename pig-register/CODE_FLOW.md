# pig-register 模块源码流程详解

## 1. 分析范围与模块定位

本文只基于 `pig-register` 模块的**源码与配置资源文件**分析，不涉及 `target/` 下产物与任何 jar 反编译。

模块定位：`pig-register` 是 Pig 微服务体系中的注册中心/配置中心进程，采用内嵌 Nacos 的方式启动。

本模块实际可读源码非常精简：

- Java 入口类：`src/main/java/com/alibaba/nacos/bootstrap/PigNacosApplication.java`
- 配置资源：`src/main/resources/application.properties`
- 日志配置：`src/main/resources/logback-spring.xml`
- 构建与依赖声明：`pom.xml`

---

## 2. 依赖与构建层（启动前）

`pom.xml` 体现了本模块的运行模型：

1. 模块打包类型是 `jar`。
2. 直接依赖：
   - `io.github.pig-mesh.nacos:nacos-console`
   - `io.github.pig-mesh.nacos:nacos-server`
3. 通过 `spring-boot-maven-plugin` 的 `repackage` 目标，生成可执行 Spring Boot 包。
4. `resources` 采用两段配置：
   - 普通资源启用 filtering（用于属性替换）
   - 字体资源关闭 filtering（避免二进制资源被破坏）

结论：`pig-register` 自身几乎不写业务代码，核心能力由 Nacos 依赖提供，模块主要承担“进程装配 + 运行参数定义”。

---

## 3. 启动入口主链路（核心）

入口类：`PigNacosApplication.main(String[] args)`

### 3.1 启动前系统属性设定

程序一开始设置两个关键系统属性：

1. `nacos.standalone=true`
   - 强制单机模式（独立模式），便于开发/本地部署。
2. `logging.config=classpath:logback-spring.xml`
   - 指定日志系统加载本模块资源目录下的 logback 配置。

### 3.2 部署类型解析

代码读取系统属性 `nacos.deployment.type`（默认 `merged`），并转换为 `DeploymentType` 后写入 `EnvUtil`。

这一步决定 Nacos 在当前 JVM 中采用哪种部署形态；本模块默认是 `merged`，即在同一进程内组合 server + console。

### 3.3 分三阶段启动三个 Spring 上下文

入口代码显式按阶段推进：

1. **CORE 阶段**
   - `NacosStartUpManager.start(CORE_START_UP_PHASE)`
   - 启动 `NacosServerBasicApplication`
   - 设置 `.web(WebApplicationType.NONE)`，表示基础上下文不启动 Web 容器
   - 产出 `coreContext`

2. **WEB 阶段**
   - `NacosStartUpManager.start(WEB_START_UP_PHASE)`
   - 启动 `NacosServerWebApplication`
   - 通过 `.parent(coreContext)` 继承核心上下文
   - 产出 Web 服务上下文（承载 Nacos 服务端 HTTP 能力）

3. **CONSOLE 阶段**
   - `NacosStartUpManager.start(CONSOLE_START_UP_PHASE)`
   - 启动 `NacosConsole`
   - 同样 `.parent(coreContext)` 继承核心上下文
   - 产出控制台上下文（承载控制台 UI/API）

这三段启动顺序说明：

- 基础能力（Core）先起来；
- Web 与 Console 在此基础上分别建立子上下文；
- 两个子上下文共享 core bean/环境，避免重复初始化底层组件。

---

## 4. 配置文件如何驱动运行行为

主配置文件：`application.properties`

### 4.1 端口与上下文

- `nacos.server.main.port=8848`：Nacos server 主端口
- `nacos.console.port=8080`：控制台端口
- `nacos.server.contextPath=/nacos`：服务端上下文路径
- `nacos.console.contextPath=`：控制台上下文路径（空）
- `nacos.console.remote.server.context-path=/nacos`：控制台访问服务端接口的路径前缀

可理解为：控制台对外入口与服务端 API 上下文被分离配置，通过 remote path 串接。

### 4.2 数据源与 SQL 初始化

- `spring.sql.init.platform=mysql`
- `db.num=1`
- `db.url.0=jdbc:mysql://${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}/${MYSQL_DB:pig_config}...`
- `db.user=root`
- `db.password=root`

关键点：

1. 连接串支持环境变量回退（`MYSQL_HOST`/`MYSQL_PORT`/`MYSQL_DB`）。
2. `spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration`
   - 显式排除 Spring Boot 默认数据源自动配置。
   - 原因是 Nacos 自身会按其机制装配数据源与持久化组件。

### 4.3 鉴权与安全

- `nacos.core.auth.enabled=true`：开启鉴权
- `nacos.core.auth.admin.enabled=true`
- `nacos.core.auth.console.enabled=true`
- `nacos.core.auth.caching.enabled=true`
- `nacos.security.ignore.urls=...`：静态资源、健康检查、部分接口放行
- `nacos.core.auth.server.identity.key/value=...`
- `nacos.core.auth.plugin.nacos.token.secret.key=...`
- `nacos.core.auth.plugin.nacos.token.expire.seconds=18000`

意味着该实例不是“裸奔” Nacos，而是启用了完整认证链路（控制台和 API 均受控）。

### 4.4 服务治理与配置中心行为

- `nacos.config.push.maxRetryTime=50`：配置推送重试上限
- `nacos.naming.empty-service.auto-clean=true`
- `nacos.naming.empty-service.clean.initial-delay-ms=50000`
- `nacos.naming.empty-service.clean.period-time-ms=30000`
- 模糊监听匹配上限：
  - `nacos.config.fuzzy.watch.max.pattern.count=20`
  - `nacos.config.fuzzy.watch.max.pattern.match.config.count=500`
  - `nacos.naming.fuzzy.watch.max.pattern.count=20`
  - `nacos.naming.fuzzy.watch.max.pattern.match.service.count=500`

体现了配置分发、空服务清理、监听匹配的运行保护阈值。

### 4.5 观测与附加能力开关

- 关闭 Elastic/Influx metrics 导出：
  - `management.elastic.metrics.export.enabled=false`
  - `management.influx.metrics.export.enabled=false`
- Tomcat access log 开启并设置格式/保留天数。
- `nacos.istio.mcp.server.enabled=false`
- `nacos.k8s.sync.enabled=false`

即默认聚焦核心 Nacos 能力，不启用 Istio MCP、K8s 同步等扩展能力。

---

## 5. 日志体系流程

日志配置文件：`logback-spring.xml`

启动类通过系统属性指定该文件后，日志流程如下：

1. 定义日志目录：`logs/${project.artifactId}`（即 `logs/pig-register`）。
2. 三路输出：
   - `console`：控制台彩色日志
   - `debug`：滚动文件 `debug.log`
   - `error`：滚动文件 `error.log`（带 `ERROR` 阈值过滤）
3. root 级别 `INFO`，同时挂接三种 appender。
4. 特定包日志抑制：
   - `com.alibaba.nacos` 设置为 `OFF`
   - `com.anji.captcha` 设置为 `OFF`

影响：默认会显著减少 Nacos 心跳等高频 INFO 噪声，降低日志体积与干扰。

---

## 6. 端到端代码执行时序（按时间）

可将模块运行时序概括为：

1. **JVM 进入 main**
2. 设置 Nacos 单机模式 + 指定 logback 配置
3. 解析部署类型并写入 Nacos 环境上下文
4. 启动 Core Spring 上下文（非 Web）
5. 启动 Server Web 上下文（父上下文为 Core）
6. 启动 Console 上下文（父上下文为 Core）
7. Nacos 基于 `application.properties` 初始化数据库、鉴权、命名与配置能力
8. 外部微服务开始通过 8848 注册/发现服务与拉取配置，控制台通过 8080 管理集群

---

## 7. 关键设计特征总结

1. **薄启动壳**：业务代码极少，重点在 orchestrate Nacos 三阶段启动。
2. **父子上下文结构**：Core 复用给 Web/Console，减少重复初始化。
3. **配置外部化**：数据库地址等通过环境变量可覆盖，适配容器部署。
4. **鉴权默认开启**：适合生产基线，避免未认证访问。
5. **日志噪声控制**：显式压制部分高频日志，偏向运维可读性。

---

## 8. 阅读源码时的边界说明

由于本模块主要依赖 `nacos-server`/`nacos-console` 提供具体实现：

- 在 `pig-register` 源码内能看到的是**启动编排与参数注入**；
- 具体的注册、心跳、配置发布、权限校验细节位于依赖库内部；
- 因本文限定只读本模块源码和资源，因此未展开依赖内部实现。
