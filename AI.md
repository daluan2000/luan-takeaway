明白，你这里更适合写成**正式设计文档风格**：不讲旧方案、不展开字段示例、不写 prompt，而是直接描述**升级后的 AI 混合架构方法、流程与技术路线**。下面是更像项目设计说明 / 毕设文档 / 简历项目说明的版本。🚀🤖

---

# AI 点餐助手混合智能架构设计

为提升点餐系统对自然语言需求的理解能力与推荐能力，在现有微服务体系基础上引入独立的 AI Service，将智能能力从传统业务服务中解耦，构建面向自然语言交互的混合检索推荐架构。

该架构采用**大模型语义理解 + 结构化检索 + 知识增强检索 + 结果编排解释**的统一链路，使系统既能处理明确约束查询，又能支持复杂语义推荐。

---

# 一、总体架构

系统在网关后新增独立智能服务：

```text
用户自然语言输入
↓
Gateway
↓
AI Service
↓
Query Understanding
↓
Hybrid Retrieval Orchestration
↓
Ranking & Explanation
↓
返回推荐结果
```

其中 AI Service 不直接维护业务主数据，而是作为智能编排层调用已有业务服务与知识服务：

* 菜品服务负责结构化数据查询
* 知识库负责语义知识检索
* 大模型负责语义理解与结果解释

因此整体仍保持原有 Spring Boot + Spring Cloud 微服务边界稳定。

---

# 二、核心设计思想：Hybrid Retrieval + LLM Orchestration

升级后的系统不再将 Tool Calling 与 RAG 视为两种独立模式，而是统一纳入混合检索链路中，由大模型完成检索编排。

核心思想为：

```text
LLM 负责理解需求
结构系统负责精确过滤
知识系统负责语义补充
统一排序后由 LLM 输出解释
```

即：

```text
Natural Language Query
→ Structured Constraints Extraction
→ Semantic Retrieval
→ Fusion Ranking
→ Response Generation
```

这种方式兼顾：

* 结构精度
* 语义灵活性
* 推荐解释能力

---

# 三、查询处理流程

## 1. Query Understanding（查询理解）

首先由大模型对用户输入进行统一语义解析，将自然语言拆解为两类信息：

---

### 结构约束信息

用于识别明确业务约束：

* 价格范围
* 辣度偏好
* 分量需求
* 菜品类别
* 人数规模

---

### 语义需求信息

用于识别隐含推荐意图：

* 饮食场景
* 身体状态
* 偏好倾向
* 推荐目标

---

这一阶段本质上完成：

```text
自然语言 → 可检索意图表示
```

为后续双通道检索提供统一输入。

---

# 四、混合检索执行机制

系统进入统一检索编排阶段，由 AI Service 同时调度两类检索能力。

---

# 1. Structured Retrieval（结构化检索）

结构化检索由业务服务完成。

AI Service 根据解析结果调用已有业务接口，对菜品进行条件过滤。

执行方式：

```text
AI Service → dish-service
```

完成：

* 条件筛选
* 范围过滤
* 分类匹配
* 基础候选集生成

这一层保证结果满足业务约束与实时数据一致性。

---

# 2. Semantic Retrieval（语义知识检索）

对于无法由结构字段直接表达的需求，引入知识库检索机制。

系统为菜品建立独立知识表示，并维护向量索引。

检索过程：

```text
Query Embedding
↓
Vector Search
↓
Knowledge Recall
```

完成：

* 语义相似召回
* 场景相关召回
* 饮食知识补充

该阶段提供结构数据无法覆盖的推荐能力。

---

# 五、知识增强建模机制

为支持语义检索，系统引入独立菜品知识层。

知识层与业务主表解耦，承担：

* 菜品知识表达
* 向量表示
* 推荐语义建模

知识生成采用自动化流程：

```text
菜品创建
↓
触发 AI 知识生成
↓
知识入库
↓
人工可编辑修正
```

即：

```text
Structured Data → Knowledge Representation
```

使业务数据具备大模型可理解性。

---

# 六、结果融合排序机制

结构检索结果与语义召回结果进入统一排序阶段。

融合排序综合考虑：

---

## 结构匹配程度

衡量业务条件满足程度。

---

## 语义相关度

衡量知识召回相似度。

---

## 业务侧优先级

可扩展：

* 销量
* 热门度
* 商家权重

---

形成最终候选结果集合。

即：

```text
Structured Score + Semantic Score + Business Score
```

---

# 七、LLM结果解释生成

最终由大模型对排序结果进行解释生成。

这一阶段不再负责检索，而负责：

* 推荐理由组织
* 结果自然语言表达
* 用户友好输出

输出内容具备可解释性，而非简单列表返回。

---

# 八、工程实现技术路线

---

# AI Service 层

建议独立服务：

```text
luan-takeaway-ai
```

内部模块：

```text
query-understanding
retrieval-orchestrator
tool-calling
rag-retrieval
ranking-engine
response-generator
```

---

# 大模型接入层

建议采用：

LangChain4j

用于：

* Prompt 编排
* Tool Calling
* Embedding 调用
* 模型统一接入

适配：

* 本地模型
* 云端模型

---

# 向量检索层

建议采用：

Redis
或
Milvus

实现：

* embedding 存储
* similarity search

---

# 业务集成层

通过已有微服务完成：

* 菜品检索
* 用户偏好读取（可扩展）
* 历史订单关联（可扩展）

保持业务服务无侵入。

---

# 九、架构特点

该升级后的核心价值在于：

---

## 智能能力独立部署

AI 服务不侵入核心订单链路。

---

## 检索能力统一融合

不是 Tool 或 RAG 二选一，而是统一编排。

---

## 知识建模可持续演进

知识层支持持续扩展与人工修正。

---

## 与现有微服务兼容性高

保留现有业务系统边界。

---
