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

FOLLOW=true
TAIL="200"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-follow)
      FOLLOW=false
      shift
      ;;
    --tail)
      if [[ $# -lt 2 ]]; then
        echo "参数错误: --tail 需要一个数字值，例如 --tail 500"
        exit 1
      fi
      TAIL="$2"
      shift 2
      ;;
    *)
      echo "不支持的参数: $1"
      echo "用法: ./logs-java-services.sh [--no-follow] [--tail N]"
      exit 1
      ;;
  esac
done

SERVICES=("pig-gateway" "pig-auth" "pig-upms" "pig-monitor" "pig-codegen" "pig-quartz")

echo "查看 Java 微服务日志: ${SERVICES[*]}"
if [[ "$FOLLOW" == true ]]; then
  docker compose logs -f --tail "$TAIL" "${SERVICES[@]}"
else
  docker compose logs --tail "$TAIL" "${SERVICES[@]}"
fi
