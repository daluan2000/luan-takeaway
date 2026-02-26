# pig-common-oss 子模块说明

## 子模块定位
- 文件存储抽象模块，支持本地与对象存储。

## 关键实现
- 自动装配入口：`FileAutoConfiguration`。
- 具体实现：`LocalFileAutoConfiguration`、`OssAutoConfiguration`。

## 实现流程
1. 根据配置选择本地存储或 OSS/S3 存储实现。
2. 业务模块通过统一文件服务接口上传/下载文件。
3. 返回标准化文件元数据，供业务表（如 `sys_file`）记录。
