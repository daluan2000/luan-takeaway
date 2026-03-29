from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path

import yaml
from dotenv import load_dotenv


# 角色编码（对应 sys_role.role_code）
ROLE_MERCHANT_CODE = "ROLE_MERCHANT"
ROLE_CUSTOMER_CODE = "ROLE_CUSTOMER"
ROLE_DELIVERY_CODE = "ROLE_DELIVERY"


@dataclass(frozen=True)
class ModeConfig:
    base_url: str
    business_path: str  # 业务接口统一前缀，monolith=/admin, microservice=/takeaway
    login_path: str
    user_info_path: str


@dataclass(frozen=True)
class TestSettings:
    mode: str
    mode_config: ModeConfig
    client_credential: str
    timeout: float
    password_encrypt_key: str
    password_encrypt_enabled: bool

    @property
    def base_url(self) -> str:
        return self.mode_config.base_url

    @property
    def business_path(self) -> str:
        return self.mode_config.business_path

    @property
    def login_path(self) -> str:
        return self.mode_config.login_path

    @property
    def user_info_path(self) -> str:
        return self.mode_config.user_info_path

    @property
    def auth_ready(self) -> bool:
        if not self.client_credential:
            return False
        return ":" in self.client_credential


def _env_bool(name: str, default: bool) -> bool:
    raw = os.getenv(name)
    if raw is None:
        return default

    value = raw.strip().lower()
    return value in {"1", "true", "yes", "on"}


def load_settings(mode_override: str | None = None) -> TestSettings:
    root_dir = Path(__file__).resolve().parent.parent
    env_path = root_dir / ".env"
    yaml_path = root_dir / "config.yaml"

    if env_path.exists():
        load_dotenv(env_path)

    with yaml_path.open("r", encoding="utf-8") as fp:
        raw_cfg = yaml.safe_load(fp) or {}

    modes = raw_cfg.get("modes", {})
    mode = mode_override or os.getenv("API_MODE", "microservice")
    if mode not in modes:
        available = ", ".join(sorted(modes.keys()))
        raise ValueError(f"未知 mode={mode}，可选值: {available}")

    mode_cfg = modes[mode]
    timeout = float(os.getenv("API_TIMEOUT", "10"))
    password_encrypt_key = os.getenv("API_PWD_ENC_KEY", "thanks,pig4cloud").strip()
    password_encrypt_enabled = _env_bool("API_PWD_ENCRYPT_ENABLED", True)

    return TestSettings(
        mode=mode,
        mode_config=ModeConfig(
            base_url=str(mode_cfg.get("base_url", "")).rstrip("/"),
            business_path=str(mode_cfg.get("business_path", "")),
            login_path=str(mode_cfg.get("login_path", "")),
            user_info_path=str(mode_cfg.get("user_info_path", "")),
        ),
        client_credential=os.getenv("API_CLIENT_CREDENTIAL", "").strip(),
        timeout=timeout,
        password_encrypt_key=password_encrypt_key,
        password_encrypt_enabled=password_encrypt_enabled,
    )
