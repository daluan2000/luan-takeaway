#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "未找到 docker 命令，请先安装 Docker。"
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "当前环境不支持 docker compose，请先安装 Docker Compose 插件。"
  exit 1
fi

if [[ ! -x "$ROOT_DIR/build-docker-jars.sh" ]]; then
  echo "缺少可执行脚本 build-docker-jars.sh，请先确认该脚本存在并具备执行权限。"
  exit 1
fi

echo "先打包所有 Java 微服务 Jar..."
"$ROOT_DIR/build-docker-jars.sh"

echo
echo "等待 Nacos 就绪..."
MAX_RETRIES=60
RETRY_INTERVAL=3

for ((attempt = 1; attempt <= MAX_RETRIES; attempt++)); do
  if curl -fsS "http://127.0.0.1:8848/nacos/actuator/health" >/dev/null 2>&1; then
    echo "Nacos 已就绪。"
    break
  fi

  if [[ "$attempt" -eq "$MAX_RETRIES" ]]; then
    echo "等待 Nacos 超时，请检查: docker compose logs --tail 200 pig-register"
    exit 1
  fi

  sleep "$RETRY_INTERVAL"
done

echo
echo "启动 Java 微服务(每次强制重建容器): pig-gateway, pig-auth, pig-upms, pig-monitor, pig-codegen, pig-quartz, pig-takeaway-merchant, pig-takeaway-dish, pig-takeaway-order, pig-takeaway-pay, pig-takeaway-delivery"
docker compose up -d --build --force-recreate --no-deps pig-gateway pig-auth pig-upms pig-monitor pig-codegen pig-quartz pig-takeaway-merchant pig-takeaway-dish pig-takeaway-order pig-takeaway-pay pig-takeaway-delivery

echo
echo "Java 微服务已启动，查看状态:"
echo "docker compose ps pig-gateway pig-auth pig-upms pig-monitor pig-codegen pig-quartz pig-takeaway-merchant pig-takeaway-dish pig-takeaway-order pig-takeaway-pay pig-takeaway-delivery"
