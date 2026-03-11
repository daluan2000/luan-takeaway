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

echo "停止 Java 微服务: pig-gateway, pig-auth, pig-upms, pig-takeaway-user, pig-takeaway-dish, pig-takeaway-order, pig-takeaway-pay"
docker compose stop pig-gateway pig-auth pig-upms pig-takeaway-user pig-takeaway-dish pig-takeaway-order pig-takeaway-pay

echo
echo "Java 微服务已停止，查看状态:"
echo "docker compose ps pig-gateway pig-auth pig-upms pig-takeaway-user pig-takeaway-dish pig-takeaway-order pig-takeaway-pay"
