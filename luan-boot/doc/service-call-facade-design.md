# Service Call Facade 架构设计

## 问题背景

### 单体模式下的 Feign 调用问题

在单体部署模式下，如果业务 Service 继续使用 Feign 进行 HTTP 调用，会产生以下问题：

**线程占用问题：**

```
请求进来 → 线程A处理
              ↓
        Feign 发起 HTTP 调用 localhost:xxxx
              ↓
        Tomcat 线程池耗尽？
              ↓
        线程A 持有并等待新线程（类似死锁）
```

1. **线程持有**：当一个 Tomcat 线程发起 Feign HTTP 调用时，该线程不会释放，而是持有并等待响应
2. **线程阻塞**：如果并发量高，线程池中的可用线程会被迅速耗尽
3. **死锁风险**：线程A持有已有线程并等待新线程，如果新线程无法分配，会导致系统假死

**示例场景：**

```
用户下单流程：
1. 线程T1: WmOrderService.createOrder()
2.        → Feign调用 DishService 扣减库存
3.        → Feign调用 DeliveryService 创建配送单
4.        → 如果DishService或DeliveryService又需要回调OrderService...

每一步都需要新线程，但Tomcat线程池有限，最终导致：
- 线程T1 持有等待
- 新请求无法获得线程
- 系统假死
```

---

## 解决方案：接口门面 + 双模式实现

### 核心思想

引入 **Service Call Facade** 接口层，通过 **本地调用** 和 **远程调用** 两种实现，灵活切换部署模式：

| 部署模式 | 调用方式 | 特点 |
|---------|---------|------|
| 单体模式 (single) | 直接注入 Service，本地方法调用 | 无网络开销，无线程占用问题 |
| 微服务模式 (microservice) | Feign HTTP 远程调用 | 服务解耦，独立部署 |

### 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         luan-boot (单体启动器)                                │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │  SingleModeConfiguration (单体模式配置)                                  │  │
│  │                                                                       │  │
│  │  @Primary 标记本地实现优先级高于微服务实现                               │  │
│  │  Local 实现类被自动注入，无需额外条件判断                                │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                    Service Call Facade 接口层                          │  │
│  │                                                                       │  │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐       │  │
│  │  │DishServiceCall  │  │OrderServiceCall │  │DeliveryService  │       │  │
│  │  │    Facade       │  │    Facade       │  │CallFacade       │       │  │
│  │  │  (菜品服务)      │  │  (订单服务)      │  │  (配送服务)      │       │  │
│  │  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘       │  │
│  │           │                    │                    │                 │  │
│  └───────────┼────────────────────┼────────────────────┼─────────────────┘  │
│              │                    │                    │                     │
│  ┌───────────▼────────────────────▼────────────────────▼─────────────────┐ │
│  │                         实 现 层                                      │ │
│  │                                                                       │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐       │ │
│  │  │Local*Impl       │  │Local*Impl       │  │Local*Impl       │       │ │
│  │  │(@Primary)       │  │(@Primary)       │  │(@Primary)       │       │ │
│  │  │ 直接注入Service │  │ 直接注入Service │  │ 直接注入Service │       │ │
│  │  │ 无网络开销      │  │ 无网络开销      │  │ 无网络开销      │       │ │
│  │  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘       │ │
│  │           │                    │                    │                 │ │
│  │  ┌────────▼────────┐  ┌────────▼────────┐  ┌────────▼────────┐       │ │
│  │  │WmDishService    │  │WmOrderService   │  │WmDeliveryService│       │ │
│  │  │(菜品领域服务)    │  │(订单领域服务)   │  │(配送领域服务)   │       │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘       │ │
│  │                                                                       │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐       │ │
│  │  │Remote*Impl      │  │Remote*Impl      │  │Remote*Impl      │       │ │
│  │  │(Feign HTTP调用) │  │(Feign HTTP调用) │  │(Feign HTTP调用) │       │ │
│  │  │ 服务解耦        │  │ 服务解耦        │  │ 服务解耦        │       │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘       │ │
│  │                                                                       │ │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 调用流程对比

### 单体模式 (deploy-mode=single)

```
┌─────────────────────────────────────────────────────────────────┐
│                     单体模式 - 本地调用                            │
│                                                                 │
│  请求 → Controller → Service → Facade → LocalImpl → Service     │
│                                 ↓                               │
│                           直接方法调用                           │
│                           单线程完成                             │
│                           无线程等待                             │
└─────────────────────────────────────────────────────────────────┘
```

**优点：**
- 零网络开销
- 单线程完成整个调用链
- 无线程池竞争
- 无死锁风险

### 微服务模式 (deploy-mode=microservice)

```
┌─────────────────────────────────────────────────────────────────┐
│                   微服务模式 - Feign 远程调用                     │
│                                                                 │
│  Service-A → Facade → RemoteImpl → Feign Client → HTTP Request  │
│                                         ↓                       │
│                                    Service-B                    │
│                                                                 │
│  线程B: 等待HTTP响应...                                          │
│  线程A: 处理其他请求...                                          │
└─────────────────────────────────────────────────────────────────┘
```

**优点：**
- 服务独立部署
- 水平扩展能力
- 故障隔离

---

## 接口设计

### Facade 接口（定义在 luan-takeaway-common）

```java
// 菜品服务调用门面
public interface DishServiceCallFacade {
    R<Boolean> deductStock(DeductStockRequest request);
    R<List<WmDish>> listByIds(Long merchantUserId, List<Long> ids);
    R<Page<WmDish>> pageDish(...);
    R<List<HybridDishCandidateDTO>> searchHybridCandidates(...);
    // ...
}

// 订单服务调用门面
public interface OrderServiceCallFacade {
    R<WmOrder> getById(Long orderId);
    R<Boolean> markPaid(Long orderId);
    // ...
}

// 配送服务调用门面
public interface DeliveryServiceCallFacade {
    R<Boolean> createDeliveryOrder(CreateDeliveryOrderRequest request);
}
```

### 实现类（定义在 luan-boot）

| 接口 | 本地实现 | 远程实现 |
|-----|---------|---------|
| DishServiceCallFacade | LocalDishServiceCallFacadeImpl | RemoteDishServiceCallFacadeImpl |
| OrderServiceCallFacade | LocalOrderServiceCallFacadeImpl | RemoteOrderServiceCallFacadeImpl |
| DeliveryServiceCallFacade | LocalDeliveryServiceCallFacadeImpl | RemoteDeliveryServiceCallFacadeImpl |

---

## 配置切换

### application-dev.yml

```yaml
takeaway:
  # 部署模式: single(单体) 或 microservice(微服务)
  deploy-mode: single
```

### Spring 自动选择机制

由于本地实现类都标注了 `@Primary`：
- 单体模式下，Spring 自动注入 **Local***Impl
- 微服务模式下，可以配置条件注解或通过配置覆盖

---

## 业务 Service 依赖

```
┌─────────────────────────────────────────────────────────────────┐
│                      业务 Service 依赖关系                        │
│                                                                 │
│  WmMerchantService                                              │
│       ↓                                                         │
│       └─→ DishServiceCallFacade                                 │
│                     ↓                                           │
│                     ├─→ LocalDishServiceCallFacadeImpl          │
│                     │        ↓                                  │
│                     │        └─→ WmDishService                  │
│                     │                                           │
│                     └─→ RemoteDishServiceCallFacadeImpl         │
│                              ↓                                   │
│                              └─→ RemoteDishService (Feign)      │
│                                                                 │
│  WmOrderService                                                 │
│       ↓                                                         │
│       └─→ DishServiceCallFacade → 菜品服务                       │
│                                                                 │
│  WmOrderPayService                                               │
│       ↓                                                         │
│       ├─→ OrderServiceCallFacade → 订单服务                      │
│       └─→ DeliveryServiceCallFacade → 配送服务                    │
│                                                                 │
│  HybridRecommendationService                                     │
│       ↓                                                         │
│       └─→ DishServiceCallFacade → 菜品服务                       │
└─────────────────────────────────────────────────────────────────┘
```

**关键点：业务 Service 只依赖 Facade 接口，不直接依赖具体实现，天然支持模式切换**

---

## 消除循环依赖

### 问题分析

如果使用单一统一门面 `TakeawayServiceCallFacade`：

```
WmMerchantService ──→ TakeawayServiceCallFacade
      ↑                                    │
      └───── WmOrderService ←─────────────┘
```

### 解决方案

拆分三个独立门面：
- DishServiceCallFacade（菜品）
- OrderServiceCallFacade（订单）
- DeliveryServiceCallFacade（配送）

拆分后依赖关系变为：
```
WmMerchantService ──→ DishServiceCallFacade
WmOrderService    ──→ DishServiceCallFacade
WmOrderPayService ─→ OrderServiceCallFacade
                 ─→ DeliveryServiceCallFacade
```

**每个 Service 只依赖自己领域的 Facade，无循环依赖**

---

## 最佳实践

### 1. 新增服务间调用

当新增跨领域服务调用时：

1. 在对应领域的 Facade 接口中添加方法
2. 在 Local*Impl 中实现本地调用逻辑
3. 在 Remote*Impl 中实现 Feign 调用逻辑
4. 业务 Service 注入 Facade 接口使用

### 2. 配置管理

```bash
# 开发环境：单体模式
takeaway.deploy-mode=single

# 生产环境：微服务模式
takeaway.deploy-mode=microservice
```

### 3. 测试策略

- **单元测试**：直接 mock Facade 接口
- **集成测试**：使用 `@ActiveProfiles("single")` 激活本地实现

---

## 总结

| 特性 | 说明 |
|-----|------|
| **统一接口** | 通过 Facade 接口层解耦业务与调用方式 |
| **双模式支持** | 本地调用 vs Feign 调用，一套代码两种部署 |
| **线程安全** | 单体模式避免线程占用导致的假死问题 |
| **无循环依赖** | 三个独立门面，消除循环依赖 |
| **配置驱动** | 通过 deploy-mode 配置灵活切换 |
