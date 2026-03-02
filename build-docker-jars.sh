#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

if ! command -v mvn >/dev/null 2>&1; then
  echo "未找到 mvn 命令，请先安装 Maven。"
  exit 1
fi

MODULES=(
  "pig-gateway"
  "pig-auth"
  "pig-upms/pig-upms-biz"
  "pig-visual/pig-monitor"
  "pig-visual/pig-codegen"
  "pig-visual/pig-quartz"
)

JARS=(
  "pig-gateway/target/pig-gateway.jar"
  "pig-auth/target/pig-auth.jar"
  "pig-upms/pig-upms-biz/target/pig-upms-biz.jar"
  "pig-visual/pig-monitor/target/pig-monitor.jar"
  "pig-visual/pig-codegen/target/pig-codegen.jar"
  "pig-visual/pig-quartz/target/pig-quartz.jar"
)

PL="$(IFS=,; echo "${MODULES[*]}")"

echo "开始打包 root docker-compose 所需 Jar..."
echo "模块: $PL"

mvn -DskipTests clean package -pl "$PL" -am

echo
echo "校验 Jar 产物:"

for i in "${!JARS[@]}"; do
  jar_path="${JARS[$i]}"
  if [[ ! -f "$jar_path" ]]; then
    echo "缺少 Jar: $jar_path"
    exit 1
  fi

  jar_size="$(du -h "$jar_path" | awk '{print $1}')"
  echo "- $jar_path ($jar_size)"
done

echo
echo "Jar 产物已就绪，可直接执行:"
echo "docker compose up -d --build"
