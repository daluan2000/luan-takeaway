#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

MODEL_PATH="${VLLM_MODEL_PATH:-/home/luan/workplace/LLM_CTA/models/Qwen3.5-4B}"
MODEL_NAME="${VLLM_MODEL_NAME:-qwen3.5-4b}"
HOST="${VLLM_HOST:-0.0.0.0}"
PORT="${VLLM_PORT:-8000}"
DTYPE="${VLLM_DTYPE:-auto}"
TENSOR_PARALLEL_SIZE="${VLLM_TP_SIZE:-1}"
MAX_MODEL_LEN="${VLLM_MAX_MODEL_LEN:-4096}"
ENFORCE_EAGER="${VLLM_ENFORCE_EAGER:-true}"

LOG_DIR="$ROOT_DIR/logs/vllm"
PID_FILE="$LOG_DIR/vllm.pid"
LOG_FILE="$LOG_DIR/vllm.log"

mkdir -p "$LOG_DIR"

if [[ ! -d "$MODEL_PATH" ]]; then
  echo "模型目录不存在: $MODEL_PATH"
  exit 1
fi

if ! command -v python >/dev/null 2>&1; then
  echo "未找到 python 命令，请先安装 Python。"
  exit 1
fi

if ! python -c "import vllm" >/dev/null 2>&1; then
  echo "当前 Python 环境未安装 vllm，请先执行: pip install vllm"
  exit 1
fi

if [[ -f "$PID_FILE" ]]; then
  OLD_PID="$(cat "$PID_FILE")"
  if [[ -n "$OLD_PID" ]] && kill -0 "$OLD_PID" >/dev/null 2>&1; then
    echo "vLLM 已在运行 (PID=$OLD_PID)，日志: $LOG_FILE"
    exit 0
  fi
  rm -f "$PID_FILE"
fi

echo "启动 vLLM..."
echo "MODEL_PATH=$MODEL_PATH"
echo "MODEL_NAME=$MODEL_NAME"
echo "LISTEN=$HOST:$PORT"

ARGS=(
  -m vllm.entrypoints.openai.api_server
  --model "$MODEL_PATH"
  --served-model-name "$MODEL_NAME"
  --host "$HOST"
  --port "$PORT"
  --dtype "$DTYPE"
  --tensor-parallel-size "$TENSOR_PARALLEL_SIZE"
  --max-model-len "$MAX_MODEL_LEN"
)

# Qwen3.5 在部分环境下会在 cudagraph 捕获阶段失败，默认启用 eager 以提升稳定性。
if [[ "${ENFORCE_EAGER,,}" == "true" ]]; then
  ARGS+=(--enforce-eager)
fi

nohup python "${ARGS[@]}" >"$LOG_FILE" 2>&1 &

NEW_PID=$!
echo "$NEW_PID" > "$PID_FILE"

sleep 2
if ! kill -0 "$NEW_PID" >/dev/null 2>&1; then
  echo "vLLM 启动失败，请检查日志: $LOG_FILE"
  exit 1
fi

echo "vLLM 已启动 (PID=$NEW_PID)"
echo "OpenAI 兼容地址: http://127.0.0.1:$PORT/v1"
echo "日志文件: $LOG_FILE"
