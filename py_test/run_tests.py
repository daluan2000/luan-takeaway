from __future__ import annotations

import argparse
import subprocess
import sys


def main() -> int:
    parser = argparse.ArgumentParser(description="API 自动化测试启动器")
    parser.add_argument(
        "--mode",
        choices=["monolith", "microservice"],
        default="microservice",
        help="后端运行模式",
    )
    parser.add_argument(
        "pytest_args",
        nargs="*",
        help="透传给 pytest 的参数，例如: -k login -q",
    )
    args = parser.parse_args()

    cmd = [sys.executable, "-m", "pytest", "--mode", args.mode, *args.pytest_args]
    return subprocess.call(cmd)


if __name__ == "__main__":
    raise SystemExit(main())
