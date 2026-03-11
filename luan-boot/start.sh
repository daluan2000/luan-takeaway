#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "[1/3] 在项目根目录执行 mvn clean install（跳过测试）..."
cd "$ROOT_DIR"
mvn clean install -DskipTests

echo "[2/3] 回到 luan-boot 执行 mvn clean install（跳过测试）..."
cd "$SCRIPT_DIR"
mvn clean install -DskipTests

echo "[3/3] 启动 luan-boot（spring-boot:run，跳过测试）..."
mvn -f pom.xml spring-boot:run -DskipTests

