# pig-common-xss 子模块说明

## 子模块定位
- XSS 防护模块，对输入参数做统一清洗与过滤。

## 关键实现
- 自动装配：`PigXssAutoConfiguration`。
- 依赖 `jsoup` 与 Jackson/Spring Web 组件实现输入清洗。

## 实现流程
1. 请求参数进入应用。
2. XSS 过滤组件对文本进行白名单/规则清洗。
3. 清洗后的安全内容再进入业务层处理。
