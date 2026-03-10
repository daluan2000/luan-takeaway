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
  "pig-takeaway/pig-takeaway-user/pig-takeaway-user-biz"
  "pig-takeaway/pig-takeaway-dish/pig-takeaway-dish-biz"
  "pig-takeaway/pig-takeaway-order/pig-takeaway-order-biz"
  "pig-takeaway/pig-takeaway-pay/pig-takeaway-pay-biz"
)

JARS=(
  "pig-gateway/target/pig-gateway-exec.jar"
  "pig-auth/target/pig-auth-exec.jar"
  "pig-upms/pig-upms-biz/target/pig-upms-biz-exec.jar"
  "pig-visual/pig-monitor/target/pig-monitor-exec.jar"
  "pig-takeaway/pig-takeaway-user/pig-takeaway-user-biz/target/pig-takeaway-user-biz-exec.jar"
  "pig-takeaway/pig-takeaway-dish/pig-takeaway-dish-biz/target/pig-takeaway-dish-biz-exec.jar"
  "pig-takeaway/pig-takeaway-order/pig-takeaway-order-biz/target/pig-takeaway-order-biz-exec.jar"
  "pig-takeaway/pig-takeaway-pay/pig-takeaway-pay-biz/target/pig-takeaway-pay-biz-exec.jar"
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
