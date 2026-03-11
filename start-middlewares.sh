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

echo "清理中间件容器及卷(不复用卷): luan-mysql, luan-redis, luan-register"
docker compose rm -fsv luan-mysql luan-redis luan-register >/dev/null 2>&1 || true

echo "启动中间件服务(复用已有镜像，不复用卷，自动清理旧前缀容器): luan-mysql, luan-redis, luan-register"
docker compose up -d --force-recreate --remove-orphans luan-mysql luan-redis luan-register

echo
echo "中间件已启动，查看状态:"
echo "docker compose ps luan-mysql luan-redis luan-register"
