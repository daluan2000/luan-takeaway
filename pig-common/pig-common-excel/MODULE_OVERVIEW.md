# pig-common-excel 子模块说明

## 子模块定位
- Excel 导入导出增强模块。

## 关键实现
- 自动装配：`ExcelAutoConfiguration`。
- 字典支撑：`RemoteDictApiService`（通过远程字典接口完成导入导出值转换）。

## 实现流程
1. 业务模块引入后自动注册 Excel 能力。
2. 导出时将业务字段映射为可读字典值。
3. 导入时反向转换并执行校验后落库。
