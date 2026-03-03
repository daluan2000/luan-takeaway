from __future__ import annotations

import base64
from typing import Any

import requests
from Crypto.Cipher import AES


class ApiClient:
    def __init__(self, base_url: str, timeout: float = 10) -> None:
        if not base_url:
            raise ValueError("base_url 不能为空")
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self.session = requests.Session()

    def _url(self, path: str) -> str:
        if path.startswith("http://") or path.startswith("https://"):
            return path
        if not path.startswith("/"):
            path = f"/{path}"
        return f"{self.base_url}{path}"

    def set_bearer_token(self, access_token: str) -> None:
        self.session.headers.update({"Authorization": f"Bearer {access_token}"})

    def login_password(
        self,
        login_path: str,
        username: str,
        password: str,
        client_credential: str,
        scope: str = "server",
        password_encrypt_key: str | None = None,
        password_encrypt_enabled: bool = True,
    ) -> dict[str, Any]:
        basic = base64.b64encode(client_credential.encode("utf-8")).decode("utf-8")
        headers = {
            "Authorization": f"Basic {basic}",
            "Content-Type": "application/x-www-form-urlencoded",
        }
        data = {
            "grant_type": "password",
            "scope": scope,
            "username": username,
            "password": self._encode_password(password, password_encrypt_key, password_encrypt_enabled),
        }

        response = self.session.post(
            self._url(login_path),
            data=data,
            headers=headers,
            timeout=self.timeout,
        )
        response.raise_for_status()
        payload = response.json()

        access_token = payload.get("access_token")
        if not access_token:
            raise RuntimeError(f"登录成功但未返回 access_token: {payload}")

        self.set_bearer_token(access_token)
        return payload

    @staticmethod
    def _encode_password(password: str, password_encrypt_key: str | None, password_encrypt_enabled: bool) -> str:
        if not password_encrypt_enabled:
            return password

        key = (password_encrypt_key or "").encode("utf-8")
        if len(key) != 16:
            return password

        cipher = AES.new(key, AES.MODE_CFB, iv=key, segment_size=128)
        encrypted = cipher.encrypt(password.encode("utf-8"))
        return base64.b64encode(encrypted).decode("utf-8")

    def request(self, method: str, path: str, **kwargs: Any) -> requests.Response:
        kwargs.setdefault("timeout", self.timeout)
        return self.session.request(method=method, url=self._url(path), **kwargs)

    def get(self, path: str, **kwargs: Any) -> requests.Response:
        return self.request("GET", path, **kwargs)
