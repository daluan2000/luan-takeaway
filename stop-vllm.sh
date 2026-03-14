#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

PID_FILE="$ROOT_DIR/logs/vllm/vllm.pid"

if [[ ! -f "$PID_FILE" ]]; then
  echo "未找到 PID 文件，vLLM 可能未通过本脚本启动。"
  exit 0
fi

PID="$(cat "$PID_FILE")"
if [[ -z "$PID" ]]; then
  echo "PID 文件为空，已清理。"
  rm -f "$PID_FILE"
  exit 0
fi

if kill -0 "$PID" >/dev/null 2>&1; then
  kill "$PID"
  echo "已停止 vLLM (PID=$PID)"
else
  echo "进程不存在 (PID=$PID)，仅清理 PID 文件。"
fi

rm -f "$PID_FILE"
