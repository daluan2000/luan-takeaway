from __future__ import annotations

import json
from typing import TYPE_CHECKING

import pytest

from core.client import ApiClient

if TYPE_CHECKING:
    from core.settings import TestSettings

_MINI_PNG_BYTES = (
    b"\x89PNG\r\n\x1a\n"
    b"\x00\x00\x00\rIHDR"
    b"\x00\x00\x00\x01\x00\x00\x00\x01\x08\x06\x00\x00\x00\x1f\x15\xc4\x89"
    b"\x00\x00\x00\x0bIDATx\x9cc\x00\x01\x00\x00\x05\x00\x01\r\n-\xb4"
    b"\x00\x00\x00\x00IEND\xaeB`\x82"
)


def _media_path_from_upload(upload_path: str, suffix: str) -> str:
    base = upload_path.rsplit("/", 1)[0]
    return f"{base}{suffix}"


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


def _json_body(response) -> dict:
    try:
        payload = response.json()
    except ValueError as exc:  # pragma: no cover - 仅在返回非 JSON 时触发
        raise AssertionError(f"响应不是 JSON: status={response.status_code}, body={response.text}") from exc

    assert isinstance(payload, dict), f"响应不是 JSON 对象: {payload}"
    return payload


def _upload_tiny_png(api_client: ApiClient, settings: "TestSettings") -> dict:
    response = api_client.request(
        "POST",
        settings.media_upload_path,
        files={"file": ("tiny.png", _MINI_PNG_BYTES, "image/png")},
    )

    if response.status_code in {401, 403}:
        body = _json_body(response)
        pytest.skip(f"当前账号无上传权限或鉴权失败，跳过用例: {json.dumps(body, ensure_ascii=False)}")

    assert response.status_code == 200, response.text
    body = _json_body(response)
    assert body.get("code") == 0, f"上传失败: {json.dumps(body, ensure_ascii=False)}"

    data = body.get("data")
    assert isinstance(data, dict), f"上传成功但 data 非对象: {body}"
    assert data.get("id"), f"缺少图片 id: {body}"
    return data


def test_media_image_upload(api_client: ApiClient, settings: "TestSettings") -> None:
    if not settings.media_upload_path:
        pytest.skip("未配置 media_upload_path")

    data = _upload_tiny_png(api_client, settings)
    assert data.get("id"), f"缺少图片 id: {data}"
    assert data.get("originName") == "tiny.png", f"originName 不符合预期: {data}"
    assert data.get("contentType") == "image/png", f"contentType 不符合预期: {data}"
    assert str(data.get("objectKey") or "").startswith("images/"), f"objectKey 不符合预期: {data}"
    assert str(data.get("viewUrl") or "").startswith("/media/files/object/") \
          or str(data.get("viewUrl") or "").startswith("/admin/media/files/object/"), f"viewUrl 不符合预期: {data}"


def test_media_image_list(api_client: ApiClient, settings: "TestSettings") -> None:
    if not settings.media_upload_path:
        pytest.skip("未配置 media_upload_path")

    _upload_tiny_png(api_client, settings)

    list_path = _media_path_from_upload(settings.media_upload_path, "/page")
    response = api_client.get(list_path, params={"current": 1, "size": 10})

    if response.status_code in {401, 403}:
        body = _json_body(response)
        pytest.skip(f"当前账号无列表权限或鉴权失败，跳过用例: {json.dumps(body, ensure_ascii=False)}")

    assert response.status_code == 200, response.text
    body = _json_body(response)
    assert body.get("code") == 0, f"列表查询失败: {json.dumps(body, ensure_ascii=False)}"

    data = body.get("data")
    assert isinstance(data, dict), f"列表 data 非对象: {body}"
    records = data.get("records")
    assert isinstance(records, list), f"列表 records 非数组: {body}"
    assert len(records) >= 1, f"列表无数据: {body}"


def test_media_image_download(api_client: ApiClient, settings: "TestSettings") -> None:
    if not settings.media_upload_path:
        pytest.skip("未配置 media_upload_path")

    uploaded = _upload_tiny_png(api_client, settings)
    file_id = uploaded["id"]

    direct_download_path = _media_path_from_upload(settings.media_upload_path, f"/{file_id}/download")
    file_resp = api_client.get(direct_download_path)
    if file_resp.status_code == 404 and settings.mode == "monolith" and direct_download_path.startswith("/media/"):
        file_resp = api_client.get(f"/admin{direct_download_path}")

    if file_resp.status_code == 200:
        assert file_resp.content[:8] == b"\x89PNG\r\n\x1a\n", "下载内容不是 PNG 文件"
        return

    download_url_path = _media_path_from_upload(settings.media_upload_path, f"/{file_id}/download-url")
    url_resp = api_client.get(download_url_path)

    if url_resp.status_code in {401, 403}:
        body = _json_body(url_resp)
        pytest.skip(f"当前账号无下载权限或鉴权失败，跳过用例: {json.dumps(body, ensure_ascii=False)}")

    assert url_resp.status_code == 200, url_resp.text
    url_body = _json_body(url_resp)
    assert url_body.get("code") == 0, f"获取下载地址失败: {json.dumps(url_body, ensure_ascii=False)}"

    download_path = str(url_body.get("data") or "")
    assert download_path.startswith("/"), f"下载地址不合法: {url_body}"

    file_resp = api_client.get(download_path)
    if file_resp.status_code == 404 and settings.mode == "monolith" and download_path.startswith("/media/"):
        file_resp = api_client.get(f"/admin{download_path}")

    assert file_resp.status_code == 200, f"下载失败: status={file_resp.status_code}, body={file_resp.text}"
    assert file_resp.content[:8] == b"\x89PNG\r\n\x1a\n", "下载内容不是 PNG 文件"




