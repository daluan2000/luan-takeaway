# pig-common-bom 子模块说明

## 子模块定位
- Maven BOM 模块，统一 `pig-common-*` 与核心三方依赖版本。

## 关键实现
- 通过 `dependencyManagement` 固定公共组件版本。
- 统一纳管 `pig-common-core/security/feign/mybatis/log/xss/...` 以及 `pig-upms-api`。

## 实现流程
1. 上层模块导入 `pig-common-bom`。
2. 子模块声明依赖时无需重复写版本。
3. 保持多模块依赖收敛与升级一致性。
