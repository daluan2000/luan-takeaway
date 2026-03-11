

# Luan Takeaway System

一个基于 **Spring Boot + Spring Cloud** 的外卖系统示例项目，支持 **微服务架构** 与 **单体架构** 两种运行模式，适用于微服务学习、系统架构实践以及业务系统开发参考。

前端工程位于 `luan-ui`，基于 **Vue3 + Typescript + Vite** 构建。


# 1 项目概述

本项目基于以下技术栈构建：

* Spring Boot
* Spring Cloud
* Nacos
* Redis
* MySQL
* Vue3 + Typescript + Vite

系统在保持 **模块化业务边界** 的前提下，支持两种运行模式：

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


2. **单体模式**

通过 `luan-boot` 聚合所有业务模块，以 **单进程方式运行**，实现微服务与单体架构的“无缝切换”。


# 2 系统架构

系统整体架构如下：

```
        Client / Browser
               │
               │
           Gateway
               │
     ┌─────────┼─────────┐
     │         │         │
   Auth       UPMS   Takeaway Services
                        │
        ┌───────────────┼────────────────┼──────────────┐
        │               │                │              │
     User Service   Dish Service    Order Service   Pay Service
                                                                 
```

系统依赖的基础组件：
- **Nacos**：服务注册与配置中心
- **MySQL**：业务数据库
- **Redis**：缓存与分布式锁



# 3 业务模块说明

## 3.1 平台基础能力

认证授权（`luan-auth`）提供系统统一认证能力：
* Token签发
* 验证码校验



权限管理（`luan-upms`）系统管理能力：

* 用户管理
* 角色管理
* 菜单权限
* 操作日志



网关服务（`luan-gateway`）系统统一入口：
* 路由转发
* 网关过滤器
* 统一异常处理
* 接口限流


公共组件（`luan-common`）所有服务共享的基础能力：
* 安全组件
* MyBatis 封装
* Feign 调用
* 日志组件
* Swagger 文档
* WebSocket


# 3.2 外卖业务域（`luan-takeaway`）

外卖业务按领域划分为多个服务。

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

# 4 典型业务流程

系统最小可用业务闭环如下：
1. 商家入驻或维护店铺信息
2. 商家上架菜品
3. 用户浏览并下单
4. 用户完成支付
5. 商家接单
6. 骑手配送
7. 订单完成


# 5 微服务划分

基础中间件：

| 服务           | 说明                 |
| ------------ | ------------------ |
| pig-register | Nacos（注册中心 + 配置中心） |
| pig-mysql    | MySQL 数据库          |
| pig-redis    | Redis 缓存           |


mysql中包含两个数据库：
```
luan （业务数据库）
pig_config （微服务配置数据，提供nacos使用）
```



核心服务：
| 服务                 | 说明              |
| ------------------ | --------------- |
| pig-gateway        | 系统网关（默认端口 9999） |
| pig-auth           | 认证中心            |
| pig-upms           | 系统管理服务          |
| pig-takeaway-user  | 用户服务            |
| pig-takeaway-dish  | 菜品服务            |
| pig-takeaway-order | 订单服务            |
| pig-takeaway-pay   | 支付服务            |



# 6 项目启动

项目支持 **微服务模式** 与 **单体模式** 两种启动方式。


# 6.1 微服务模式启动

**如果是linux系统并且已安装docker**，系统提供了一键启动脚本：
```bash
# 启动中间件
./start-middlewares.sh

# 启动 Java 微服务
./start-java-services.sh
```

先执行`./start-middlewares.sh`，会自动拉取官网的mysql、redis、nacos镜像，创建容器并初始化，自动执行初始化sql脚本。

后执行`start-java-services.sh` 会自动使用mvn编译打包所有java代码，然后使用docker讲每个微服务的jar包转为镜像，创建对应微服务容器并启动。

查看中间件状态：

```bash
docker compose ps pig-mysql pig-redis pig-register
```

查看微服务状态：

```bash
docker compose ps pig-gateway pig-auth pig-upms pig-takeaway-user pig-takeaway-dish pig-takeaway-order pig-takeaway-pay
```

停止 Java 服务：

```bash
./stop-java-services.sh
```

停止中间件：

```bash
./stop-middlewares.sh
```

**如果是其他情况**，需自行配置微服务、mysql、redis、nacos端口，并自行执行初始化sql脚本：
```
db/luan.sql
db/luan_config.sql
```

# 6.2 单体模式启动

单体模式由 `luan-boot` 聚合运行，直接运行该项目即可，系统也提供了一键运行脚本（linux）`./start.sh`
其实现方式是在 `luan-boot` 的 `pom.xml` 中直接聚合认证、权限和外卖各业务模块依赖，让原本拆分的 Controller、Service、Mapper 一起加载到同一个 Spring Boot 应用上下文中。
运行时再通过关闭 Nacos 的服务发现与配置能力，并统一使用 `/admin` 单入口和单端口，使Feign顺着入口“回调自己”，把原本多服务部署的系统收敛为单 JVM 进程运行。

单体模式依然需要：
* MySQL
* Redis

配置与微服务模式相同，依然建议使用Docker启动mysql redis：

```bash
docker compose up -d pig-mysql pig-redis
```

# 6.3 前端启动

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


# 7 业务技术实现

## 7.1 Redis 分布式锁

应用场景：**菜品库存扣减**

高并发下单场景：

```
库存 = 10

用户A -> 查询库存 = 10
用户B -> 查询库存 = 10

A扣减 -> 9
B扣减 -> 8
```

可能导致：

* 超卖
* 库存负数

解决方案：**Redis 分布式锁**

流程：

```
1 获取 redis 锁 (dishId)
2 查询库存
3 扣减库存
4 创建订单
5 释放锁
```


## 7.2 Redis 缓存

适用于 **读多写少数据**（如菜品信息）。

查询流程：

```
1 查询 Redis
2 未命中 -> 查询 MySQL
3 写入 Redis
```


### 缓存击穿

Hot Key 过期导致大量请求访问数据库。

解决方案：

```
互斥锁 + rebuild
```

流程：

```
1 Redis 未命中
2 获取锁
3 查询数据库
4 写入缓存
5 释放锁
```


### 缓存穿透

恶意请求不存在的数据：

```
dishId = 999999
```

解决：

缓存空值：

```
dish:999999 -> null
ttl = 60s
```


### 缓存雪崩

大量缓存同时过期。

解决方案：

```
TTL = baseTTL + random(0~300)
```


## 7.3 RabbitMQ 异步处理

用于 **服务解耦与异步处理**。


### 订单事件异步处理

订单创建后，通过 MQ 触发后续业务流程。



### 订单超时自动取消

用户下单后 **30分钟未支付自动取消**。

流程：

```
创建订单
    │
发送延迟消息 (30min)
    │
消费者监听
    │
检查订单状态
    │
未支付 -> 取消订单
```

