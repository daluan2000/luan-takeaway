from __future__ import annotations

import base64
from typing import Any

import requests
from Crypto.Cipher import AES


class ApiClient:
    def __init__(self, base_url: str, timeout: float = 10, business_path: str = "") -> None:
        if not base_url:
            raise ValueError("base_url 不能为空")
        self.base_url = base_url.rstrip("/")
        self.business_path = business_path
        self.timeout = timeout
        self.session = requests.Session()
        self._username_for_login = ""
        self._client_credential_for_login = ""
        self._password_encrypt_key: str | None = None
        self._password_encrypt_enabled = True

    def _bp(self, path: str) -> str:
        """拼接 business_path 前缀，例如 /user/address -> /takeaway/user/address"""
        if not path.startswith("/"):
            path = f"/{path}"
        return f"{self.business_path}{path}"

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

    def post(self, path: str, **kwargs: Any) -> requests.Response:
        return self.request("POST", path, **kwargs)

    def put(self, path: str, **kwargs: Any) -> requests.Response:
        return self.request("PUT", path, **kwargs)

    def delete(self, path: str, **kwargs: Any) -> requests.Response:
        return self.request("DELETE", path, **kwargs)

    # -------------------------------------------------------------------------
    # 认证 & 注册
    # -------------------------------------------------------------------------

    def register_user(
        self,
        username: str,
        password: str,
        phone: str,
        role_code: str | None = None,
    ) -> dict[str, Any]:
        """注册用户，返回 {userId, username}"""
        payload: dict[str, Any] = {
            "username": username,
            "password": password,
            "phone": phone,
        }
        if role_code:
            payload["roleCode"] = role_code
        resp = self.post("/admin/register/user", json=payload)
        resp.raise_for_status()
        body = resp.json()
        return body

    # -------------------------------------------------------------------------
    # OAuth2 登录（以指定用户身份）
    # -------------------------------------------------------------------------

    def login_as(self, password: str, login_path: str = "/auth/oauth2/token") -> dict[str, Any]:
        """以当前用户名和指定密码登录，返回完整 token payload（含 access_token）。"""
        return self.login_password(
            login_path=login_path,
            username=self._username_for_login,
            password=password,
            client_credential=self._client_credential_for_login,
            password_encrypt_key=self._password_encrypt_key,
            password_encrypt_enabled=self._password_encrypt_enabled,
        )

    # -------------------------------------------------------------------------
    # 地址（/takeaway/user/address）
    # -------------------------------------------------------------------------

    def create_address(self, address: dict) -> dict[str, Any]:
        """
        创建当前登录用户的地址。
        address 格式: {province, city, district, detailAddress, longitude, latitude}
        返回创建的地址（含 id）。
        """
        resp = self.post(self._bp("/user/address"), json=address)
        resp.raise_for_status()
        body = resp.json()
        return body.get("data", body)

    # -------------------------------------------------------------------------
    # 商家扩展（/takeaway/user/merchant/apply）
    # -------------------------------------------------------------------------

    def create_merchant(
        self,
        merchant_name: str,
        contact_name: str,
        store_address_id: int | str,
    ) -> dict[str, Any]:
        """
        为当前登录用户申请商家扩展信息。
        返回商家信息（含 id, userId）。
        """
        resp = self.post(self._bp("/user/merchant/apply"), json={
            "merchantName": merchant_name,
            "contactName": contact_name,
            "storeAddressId": int(store_address_id),
        })
        resp.raise_for_status()
        body = resp.json()
        return body.get("data", body)

    def get_current_merchant(self) -> dict[str, Any]:
        """获取当前登录用户的商家信息（单个）。"""
        resp = self.get(self._bp("/user/merchant/current"))
        resp.raise_for_status()
        body = resp.json()
        return body.get("data", body)

    def list_merchants(self, page: int = 1, size: int = 10) -> dict[str, Any]:
        """分页查询商家列表。"""
        resp = self.get(self._bp("/user/merchant/page"), params={"current": page, "size": size})
        resp.raise_for_status()
        body = resp.json()
        return body.get("data", body)

    # -------------------------------------------------------------------------
    # 客户扩展（/takeaway/user/customer）
    # -------------------------------------------------------------------------

    def create_customer(
        self,
        real_name: str,
        default_address_id: int | str,
    ) -> dict[str, Any]:
        """
        为当前登录用户创建客户扩展信息。
        返回客户信息（含 id, userId）。
        """
        resp = self.post(self._bp("/user/customer"), json={
            "realName": real_name,
            "defaultAddressId": int(default_address_id),
        })
        resp.raise_for_status()
        body = resp.json()
        return body.get("data", body)

    def get_current_customer(self) -> dict[str, Any]:
        """获取当前登录用户的客户信息。"""
        resp = self.get(self._bp("/user/customer/current"))
        resp.raise_for_status()
        body = resp.json()
        return body.get("data", body)

    # -------------------------------------------------------------------------
    # 骑手扩展（/takeaway/user/delivery/rider）
    # -------------------------------------------------------------------------

    def create_delivery_rider(
        self,
        real_name: str,
        delivery_scope_km: float = 5.0,
    ) -> dict[str, Any]:
        """
        为当前登录用户创建骑手扩展信息。
        返回骑手信息（含 id, userId）。
        """
        resp = self.post(self._bp("/user/delivery/rider"), json={
            "realName": real_name,
            "deliveryScopeKm": delivery_scope_km,
        })
        resp.raise_for_status()
        body = resp.json()
        return body.get("data", body)

    def get_current_rider(self) -> dict[str, Any]:
        """获取当前登录用户的骑手信息。"""
        resp = self.get(self._bp("/user/delivery/rider/current"))
        resp.raise_for_status()
        body = resp.json()
        return body.get("data", body)

    # -------------------------------------------------------------------------
    # 菜品（/takeaway/dish）
    # -------------------------------------------------------------------------

    def create_dish(
        self,
        merchant_user_id: int,
        dish_name: str,
        price: float,
        stock: int,
        dish_image: str = "https://via.placeholder.com/400x300?text=Dish",
        dish_desc: str = "",
        sale_status: str = "1",
        auto_generate_knowledge: bool = False,
    ) -> dict[str, Any]:
        """
        创建菜品。
        返回 {success: bool}。
        """
        resp = self.post(self._bp("/dish"), json={
            "merchantUserId": merchant_user_id,
            "dishName": dish_name,
            "dishDesc": dish_desc,
            "price": price,
            "stock": stock,
            "dishImage": dish_image,
            "saleStatus": sale_status,
            "autoGenerateKnowledge": auto_generate_knowledge,
        })
        resp.raise_for_status()
        body = resp.json()
        return body

    # -------------------------------------------------------------------------
    # 内部辅助
    # -------------------------------------------------------------------------

    def set_login_params(
        self,
        username: str,
        client_credential: str,
        password_encrypt_key: str | None,
        password_encrypt_enabled: bool,
    ) -> None:
        self._username_for_login = username
        self._client_credential_for_login = client_credential
        self._password_encrypt_key = password_encrypt_key
        self._password_encrypt_enabled = password_encrypt_enabled
