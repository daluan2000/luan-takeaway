from __future__ import annotations

import argparse
import subprocess
import sys


def main() -> int:
    parser = argparse.ArgumentParser(description="API 自动化测试启动器")
    parser.add_argument(
        "--mode",
        choices=["monolith", "microservice"],
        default=None,
        help="后端运行模式（默认从 .env 的 API_MODE 读取）",
    )
    parser.add_argument(
        "pytest_args",
        nargs="*",
        help="透传给 pytest 的参数，例如: -k login -q",
    )
    args = parser.parse_args()

    cmd = [sys.executable, "-m", "pytest"]
    cmd.append("--fresh-seed")  # 每次跑都重新注册用户、重新拿 token
    if args.mode is not None:
        cmd.extend(["--mode", args.mode])
    cmd.extend(args.pytest_args)
    return subprocess.call(cmd)


if __name__ == "__main__":
    raise SystemExit(main())
