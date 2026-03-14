#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

if ! command -v mvn >/dev/null 2>&1; then
  echo "未找到 mvn 命令，请先安装 Maven。"
  exit 1
fi

MODULES=(
  "luan-gateway"
  "luan-auth"
  "luan-takeaway/luan-takeaway-ai/luan-takeaway-ai-biz"
  "luan-upms/luan-upms-biz"
  "luan-takeaway/luan-takeaway-user/luan-takeaway-user-biz"
  "luan-takeaway/luan-takeaway-dish/luan-takeaway-dish-biz"
  "luan-takeaway/luan-takeaway-order/luan-takeaway-order-biz"
  "luan-takeaway/luan-takeaway-pay/luan-takeaway-pay-biz"
)

JARS=(
  "luan-gateway/target/luan-gateway-exec.jar"
  "luan-auth/target/luan-auth-exec.jar"
  "luan-takeaway/luan-takeaway-ai/luan-takeaway-ai-biz/target/luan-takeaway-ai-biz-exec.jar"
  "luan-upms/luan-upms-biz/target/luan-upms-biz-exec.jar"
  "luan-takeaway/luan-takeaway-user/luan-takeaway-user-biz/target/luan-takeaway-user-biz-exec.jar"
  "luan-takeaway/luan-takeaway-dish/luan-takeaway-dish-biz/target/luan-takeaway-dish-biz-exec.jar"
  "luan-takeaway/luan-takeaway-order/luan-takeaway-order-biz/target/luan-takeaway-order-biz-exec.jar"
  "luan-takeaway/luan-takeaway-pay/luan-takeaway-pay-biz/target/luan-takeaway-pay-biz-exec.jar"
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
