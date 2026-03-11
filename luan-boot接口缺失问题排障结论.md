# pig-boot 接口缺失问题排障结论

## 结论
本次问题**不是业务代码逻辑错误**，而是本机构建产物形态导致的运行时装配异常。

## 错误原因
- 本机 `~/.m2` 中部分模块（如 `pig-auth`、`pig-upms-biz`）被安装成了可执行 Fat Jar（类位于 `BOOT-INF/classes`）。
- 这些模块被 `pig-boot` 作为依赖引入时，Spring 组件扫描无法按普通依赖方式发现其中的 Controller。
- 直接表现为：
  - Swagger 页面可打开，但 `/admin/v3/api-docs` 返回 `paths: {}`。
  - 业务接口缺失（如 `/admin/code/image` 返回 404）。
  - 前端请求大量失败（后端路由未注册）。

## 已执行修复
1. **固化 Maven 打包策略（根治）**
   - 在父工程 `spring-boot-maven-plugin` 中配置 `classifier=exec`。
   - 结果：
     - 主产物保留为普通 Thin Jar（供模块依赖）。
     - 可执行包改为 `*-exec.jar`（供运行/容器启动）。

2. **同步容器与脚本引用**
   - 将后端各模块 Dockerfile 的 `JAR_FILE` 从 `target/*.jar` 调整为 `target/*-exec.jar`。
   - 将 `build-docker-jars.sh` 中产物校验路径同步为 `*-exec.jar`。

3. **本机产物重建与验证**
   - 重新 clean/install 后，本地依赖模块恢复为 Thin Jar 形态。
   - `pig-boot` 重新打包后同时生成：
     - `pig-boot.jar`（普通依赖包）
     - `pig-boot-exec.jar`（可执行包）
   - 启动验证通过：
     - `/admin/v3/api-docs` 恢复接口内容（非空）。
     - `/admin/code/image` 返回 200。

## 后续建议
- 本地/CI 统一使用该打包策略，避免 Fat Jar 作为依赖安装进仓库。
- 运行时统一使用 `*-exec.jar`，依赖解析统一使用主产物（非 exec）。
