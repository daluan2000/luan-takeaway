

# Luan Takeaway System

一个基于 Spring Boot + Spring Cloud 构建的外卖业务系统，系统按照业务领域拆分为用户、菜品、订单和支付等多个服务，并通过 Gateway、Nacos、Redis、RabbitMQ等组件构建完整的服务治理与异步处理体系。

项目支持一键切换微服务部署模式，开发阶段可以通过单体模式快速启动和调试，而在生产环境中可以按需拆分为多个微服务独立部署。

前端工程位于`luan-ui`，基于 Vue3 + TypeScript + Vite 构建。

本项目的权限管理服务`luan-upms`、认证服务`luan-auth`以及前端工程`luan-ui`基于开源项目 [https://gitee.com/log4j/pig](https://gitee.com/log4j/pig) 二次开发。原项目提供通用后台管理框架，不包含具体业务实现。本项目在此基础上设计并实现了完整的外卖业务系统，并对原有框架进行了业务适配改造。

本项目仓库地址：
- gitee: [https://gitee.com/daluan/luan-takeaway](https://gitee.com/daluan/luan-takeaway)
- github: [https://github.com/daluan2000/luan-takeaway](https://github.com/daluan2000/luan-takeaway)

## 1 项目概述

本项目基于以下技术栈构建：

* Spring Boot
* Spring Cloud
* Nacos
* Redis
* MySQL
* RabbitMQ
* Vue3 + Typescript + Vite

系统支持一键切换两种运行模式：

1. **微服务模式**，多个服务独立部署：
```
luan-gateway  网关服务
luan-auth  认证服务
luan-upms  用户权限管理服务
luan-takeaway-user 外卖业务用户管理服务
luan-takeaway-dish 菜品管理服务
luan-takeaway-order 订单管理服务
luan-takeaway-pay 支付管理服务
```

2. **单体模式**：
通过 `luan-boot` 聚合所有业务模块，以单进程方式运行。


## 2 系统架构

系统整体采用基于Spring Cloud的微服务架构，通过Gateway统一对外提供接口入口，并结合Nacos实现服务注册与配置管理。系统按照业务领域拆分为认证、权限管理以及外卖业务等多个微服务，各服务之间通过Feign进行同步调用，并通过RabbitMQ进行异步消息通信。

在数据层面，系统使用MySQL作为核心业务数据库，Redis用于缓存与分布式锁控制，以提升高并发场景下的系统性能与稳定性。

整体架构中各组件职责如下：
- Gateway：系统统一入口，负责路由转发、统一鉴权与接口限流  
- Auth：认证服务，负责Token签发与认证校验  
- Upms：权限管理服务，提供用户、角色、菜单等系统管理能力  
- Takeaway Services：外卖业务服务，包括用户、菜品、订单和支付等业务模块  
- RabbitMQ：用于订单相关的异步处理与延时任务，例如库存更新、订单自动取消等  
- Redis：提供缓存能力与分布式锁支持  
- MySQL：存储系统核心业务数据  
- Nacos：提供服务注册发现与配置管理能力

系统依赖的基础组件：
- **Nacos**：服务注册与配置中心
- **MySQL**：数据库
- **Redis**：缓存与分布式锁
- **RabitMQ**: 服务间异步通信



## 3 业务模块说明

### 3.1 平台基础能力

系统基础能力主要包括认证、权限管理及网关等通用组件：

认证服务（`luan-auth`）：提供系统统一认证能力。

权限管理服务（`luan-upms`）：提供用户、角色与菜单等系统管理能力。

网关服务（`luan-gateway`）：作为系统统一入口，负责请求路由与网关过滤。

公共组件（`luan-common`）：提供各服务共享的基础组件，例如安全组件、日志组件及Feign调用封装等。

### 3.2 外卖业务域

外卖业务`luan-takeaway`按领域划分为多个服务。

用户域（`luan-takeaway-user`）负责外卖业务用户相关能力：
* 商家管理
* 客户管理
* 骑手管理


菜品域（`luan-takeaway-dish`）负责菜品相关能力：
* 菜品维护
* 菜品上下架
* 库存管理


订单域（`luan-takeaway-order`）负责订单核心业务：
* 创建订单
* 订单状态流转
* 订单查询


支付域（`luan-takeaway-pay`）负责支付流程：
* 模拟支付
* 支付状态更新

## 4 典型业务流程

系统最小可用业务闭环如下：
1. 商家入驻、维护店铺信息
2. 商家上架菜品
3. 用户浏览并下单
4. 用户完成支付
5. 商家接单
6. 骑手配送
7. 订单完成


## 5 微服务划分

基础中间件：

| 服务           | 说明                 |
| ------------ | ------------------ |
| luan-register | Nacos（注册中心 + 配置中心） |
| luan-mysql    | MySQL 数据库          |
| luan-redis    | Redis 缓存           |
| luan-rabbitmq | MQ异步通信|


mysql中包含两个数据库：
```
luan （业务数据库）
luan_config （微服务配置数据，提供nacos使用）
```



核心服务：
| 服务                 | 说明              |
| ------------------ | --------------- |
| luan-gateway        | 系统网关（默认端口 9999） |
| luan-auth           | 认证中心            |
| luan-upms           | 系统管理服务          |
| luan-takeaway-user  | 用户服务            |
| luan-takeaway-dish  | 菜品服务            |
| luan-takeaway-order | 订单服务            |
| luan-takeaway-pay   | 支付服务            |



## 6 项目启动

项目支持 **微服务模式** 与 **单体模式** 两种启动方式。


### 6.1 微服务模式启动

**如果是linux系统并且已安装docker**，系统提供了一键启动脚本：
```bash
# 启动中间件
./start-middlewares.sh

# 启动 Java 微服务
./start-java-services.sh
```

先执行`./start-middlewares.sh`，会自动拉取官网的mysql、redis、nacos、rabbitmq镜像，创建容器并初始化，自动执行初始化sql脚本。

后执行`start-java-services.sh` 会自动使用mvn编译打包所有java代码，然后使用docker讲每个微服务的jar包转为镜像，创建对应微服务容器并启动。

查看中间件（mysql、redis、nacos、rabbitmq）状态：

```bash
docker compose ps luan-mysql luan-redis luan-register
```

查看微服务状态：

```bash
docker compose ps luan-gateway luan-auth luan-upms luan-takeaway-user luan-takeaway-dish luan-takeaway-order luan-takeaway-pay
```

停止 Java 服务：

```bash
./stop-java-services.sh
```

停止中间件：

```bash
./stop-middlewares.sh
```

如果需要图形化查看编辑mysql数据内容，请运行以下指令并浏览器访问`8082`端口：
```bash
docker compose up luan-phpmyadmin -d
```

**如果是其他情况**，需自行配置微服务、mysql、redis、nacos端口，并自行执行初始化sql脚本：
```
db/luan.sql
db/luan_config.sql
```

### 6.2 单体模式启动

单体模式由 `luan-boot` 聚合运行，直接运行该项目即可，系统也提供了一键运行脚本（linux）`./start.sh`。

其实现方式是在 `luan-boot` 的 `pom.xml` 中直接聚合认证、权限和外卖各业务模块依赖，让原本拆分的 Controller、Service、Mapper 一起加载到同一个 Spring Boot 应用上下文中。

运行时再通过关闭 Nacos 的服务发现与配置能力，并统一使用 `/admin` 单入口和单端口，使Feign顺着入口“回调自己”，把原本多服务部署的系统收敛为单 JVM 进程运行。

单体模式依然需要：
* MySQL
* Redis
* RabbitMQ

配置与微服务模式相同，依然建议使用Docker启动mysql redis rabbitMQ：

```bash
docker compose up -d luan-mysql luan-redis luan-rabbitmq
```

### 6.3 前端启动

进入前端工程：

```
luan-ui
```

微服务模式：

```bash
npm run dev
```

单体模式：

```bash
npm run dev:mono
```


## 7 业务技术实现

本系统在实现外卖业务流程的同时，也针对常见的高并发与分布式场景做了一些工程化处理，包括缓存保护、分布式锁、库存扣减以及异步消息处理等。下面介绍几个核心实现。

### 7.1 Redis 缓存优化

在商户信息和菜品查询等高频读场景中，系统通过 Redis 缓存降低数据库访问压力。

项目中封装了统一的缓存访问组件`RedisSafeCacheService`，业务代码只需提供：
* `cacheKey`
* `lockKey`
* 数据库回源函数
即可完成完整的缓存读写逻辑。

该组件封装了个常见缓存问题的解决方案：
* **缓存穿透**：对空值进行短TTL缓存
* **缓存击穿**：通过互斥锁控制并发回源
* **缓存雪崩**：为缓存TTL增加随机偏移


商户查询与菜品查询均复用了该组件，从而避免各业务模块重复实现缓存保护逻辑。

相关代码：
```
RedisSafeCacheService.java
WmMerchantServiceImpl.java
WmDishServiceImpl.java
```


### 7.2 Redis 分布式锁（骑手抢单）

在骑手抢单场景中，可能存在多个骑手同时尝试接同一个订单的问题。为避免并发导致的重复接单，系统在接单逻辑中引入**Redis 分布式锁**。锁以 `orderId` 作为粒度：
```java
String lockKey = "takeaway:order:delivery:start:lock:" + orderId;
```

从而保证同一订单在同一时间只能被一个骑手处理，相关代码：
```
WmOrderServiceImpl.java
RedisUtils.java
```



### 7.3 Redis库存扣减 + MQ异步落库

在高并发下单场景中，如果每次下单都直接操作数据库库存，很容易成为系统瓶颈。

因此系统采用 **Redis预扣减库存 + MQ异步更新数据库** 的方式处理库存。

处理流程如下：
```
下单请求
   ↓
Redis Lua脚本校验库存并扣减
   ↓
扣减成功 → 返回下单成功
   ↓
发送MQ消息
   ↓
消费者异步更新MySQL库存
```

其中Redis扣减库存通过原子化的**Lua 脚本**完成，先后完成两个步骤：
* 校验库存
* 扣减库存


数据库更新通过 MQ 异步完成，减少主链路的数据库压力。为避免消息重复消费带来的问题，消费者侧通过 `orderNo` 进行幂等控制。

相关代码：
```
WmDishServiceImpl.java
DishStockDeductConsumer.java
DishStockMqConfig.java
```


### 7.4 RabbitMQ 异步与延时任务

系统在订单流程中使用RabbitMQ处理异步任务和延时任务。

**自动取消未支付订单**。用户下单后，如果在一定时间内未完成支付，系统需要自动取消订单。

实现方式：
1. 创建订单后发送 **10分钟 TTL 的消息**
2. 消息过期后进入 **死信队列**
3. 消费者接收到消息后执行订单取消逻辑

为了防止用户已经支付的情况下，自动取消逻辑覆盖已支付状态，订单取消采用条件更新：
```
WAIT_PAY → CANCELED
```

**订单状态异步通知**

订单状态变化（接单、配送、完成等）会通过MQ发送通知消息。后端Order服务先处理接单、配送、完成的核心逻辑，然后通过异步通过MQ向服务发送消息，Upms服务通过WebSocket通道向前端推送通知，主动通知用户订单状态变化，Order服务无需等待异步结果，直接返回。
```
Order服务 → MQ → Upms服务 → WebSocket → 前端
```

这种方式可以避免在主业务线程中直接进行推送操作，从而提高接口响应效率。

相关代码：
```
OrderAutoCancelMqConfig.java
OrderAutoCancelConsumer.java
OrderStatusMqPublisher.java
```

## 8 系统设计总结

本项目在实现外卖业务流程的基础上，重点实践了基于Spring Cloud的微服务架构设计。系统按照业务领域拆分为用户、菜品、订单和支付等服务，并通过Gateway + Nacos + Feign实现服务治理与调用。同时，通过`luan-boot`模块对各业务模块进行聚合，使系统在开发阶段可以以单体模式快速运行，在部署阶段又可以按需拆分为多个微服务，提高了架构的灵活性。

在高并发场景下，系统通过Redis + RabbitMQ对关键链路进行了优化。例如在商户和菜品查询中引入Redis缓存，并通过统一封装逻辑解决缓存穿透、击穿和雪崩问题；在下单场景中采用Redis预扣减库存 + MQ异步落库的方式降低数据库压力，并利用Lua脚本保证库存扣减的原子性；在骑手抢单场景中通过Redis分布式锁控制并发，避免同一订单被重复接单。

此外，系统通过RabbitMQ将订单自动取消、订单状态通知等逻辑从主业务流程中解耦出来，并结合 WebSocket 实现订单状态的实时推送。整体架构在保证业务完整性的同时，也为后续扩展（如骑手服务拆分、限流与熔断、Redis集群等）预留了空间。