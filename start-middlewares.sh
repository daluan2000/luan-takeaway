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

echo "清理中间件容器及卷(不复用卷): pig-mysql, pig-redis, pig-register"
docker compose rm -fsv pig-mysql pig-redis pig-register >/dev/null 2>&1 || true

echo "启动中间件服务(复用已有镜像，不复用卷): pig-mysql, pig-redis, pig-register"
docker compose up -d --force-recreate pig-mysql pig-redis pig-register

echo
echo "中间件已启动，查看状态:"
echo "docker compose ps pig-mysql pig-redis pig-register"
