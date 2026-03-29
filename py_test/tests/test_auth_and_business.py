from __future__ import annotations

import json

from core.client import ApiClient
from core.settings import TestSettings


def _looks_unauthorized(body: dict) -> bool:
    code = body.get("code")
    msg = str(body.get("msg") or body.get("message") or "").lower()

    if str(code) in {"401", "403"}:
        return True
    if "unauthor" in msg:
        return True
    if "invalid" in msg and "token" in msg:
        return True
    return False

def test_login_should_return_token(merchant_clients: list[ApiClient]) -> None:
    client = merchant_clients[0]
    assert hasattr(client, "session") and client.session.headers.get("Authorization", "").startswith("Bearer "), \
        "merchant_clients[0] 应已设置 Bearer token"


def test_user_info_with_token(merchant_clients: list[ApiClient], settings: TestSettings) -> None:
    client = merchant_clients[0]
    response = client.get(settings.user_info_path)
    assert response.status_code == 200, response.text

    body = response.json()
    assert isinstance(body, dict), f"响应不是 JSON 对象: {body}"
    assert not _looks_unauthorized(body), f"疑似鉴权失败: {json.dumps(body, ensure_ascii=False)}"
