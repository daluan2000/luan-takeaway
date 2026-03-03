from __future__ import annotations

import pytest

from core.client import ApiClient
from core.settings import TestSettings, load_settings


def pytest_addoption(parser: pytest.Parser) -> None:
    parser.addoption(
        "--mode",
        action="store",
        choices=["monolith", "microservice"],
        default=None,
        help="后端运行模式：monolith 或 microservice",
    )


@pytest.fixture(scope="session")
def settings(pytestconfig: pytest.Config) -> TestSettings:
    mode = pytestconfig.getoption("mode")
    return load_settings(mode_override=mode)


@pytest.fixture(scope="session")
def token_payload(settings: TestSettings) -> dict:
    if not settings.auth_ready:
        pytest.skip("请先在 py_test/.env 配置 API_USERNAME / API_PASSWORD / API_CLIENT_CREDENTIAL")

    client = ApiClient(base_url=settings.base_url, timeout=settings.timeout)
    payload = client.login_password(
        login_path=settings.login_path,
        username=settings.username,
        password=settings.password,
        client_credential=settings.client_credential,
        password_encrypt_key=settings.password_encrypt_key,
        password_encrypt_enabled=settings.password_encrypt_enabled,
    )
    return payload


@pytest.fixture(scope="session")
def api_client(settings: TestSettings, token_payload: dict) -> ApiClient:
    client = ApiClient(base_url=settings.base_url, timeout=settings.timeout)
    access_token = token_payload["access_token"]
    client.set_bearer_token(access_token)
    return client
