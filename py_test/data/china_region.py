from __future__ import annotations

import json
import math
import random
from pathlib import Path
from typing import Literal

# ---------------------------------------------------------------------------
# 1. 省市区三级数据（从前端静态资源复制，minified JSON）
# ---------------------------------------------------------------------------
_CHINA_REGION_JSON = Path(__file__).resolve().parent / "china_region.json"
_CHINA_REGION_DATA: list[dict] = []


def _load_region_data() -> list[dict]:
    global _CHINA_REGION_DATA
    if not _CHINA_REGION_DATA:
        with _CHINA_REGION_JSON.open(encoding="utf-8") as f:
            _CHINA_REGION_DATA = json.load(f)
    return _CHINA_REGION_DATA


# ---------------------------------------------------------------------------
# 2. 经纬度估算规则（简化版，不依赖外部 API）
#    中国经度范围：约 73°E ~ 136°E
#    中国纬度范围：约 18°N ~ 54°N
#    省 / 市 / 区中心点通过在对应矩形范围内随机采样近似
# ---------------------------------------------------------------------------

# 各省级行政区大致边界 [minLon, maxLon, minLat, maxLat]
_PROVINCE_BOUNDS: dict[str, list[float]] = {
    "北京市": [115.4, 117.6, 39.4, 41.1],
    "天津市": [116.6, 118.2, 38.5, 40.3],
    "河北省": [113.4, 120.0, 36.0, 42.8],
    "山西省": [110.1, 115.1, 35.0, 40.8],
    "内蒙古自治区": [97.0, 126.1, 37.4, 53.5],
    "辽宁省": [118.8, 125.5, 38.7, 43.5],
    "吉林省": [121.5, 131.3, 40.9, 46.3],
    "黑龙江省": [121.1, 135.1, 43.4, 53.8],
    "上海市": [120.8, 122.1, 30.7, 31.5],
    "江苏省": [116.3, 122.3, 30.8, 35.4],
    "浙江省": [118.0, 123.2, 27.0, 31.3],
    "安徽省": [114.9, 119.7, 29.4, 34.8],
    "福建省": [115.8, 120.6, 23.6, 28.5],
    "江西省": [113.8, 118.9, 24.3, 30.4],
    "山东省": [114.8, 122.8, 34.4, 38.6],
    "河南省": [110.3, 116.6, 31.4, 36.5],
    "湖北省": [108.4, 116.2, 29.0, 33.4],
    "湖南省": [108.8, 114.3, 24.6, 30.2],
    "广东省": [109.5, 117.3, 20.1, 25.7],
    "广西壮族自治区": [104.4, 112.1, 20.9, 26.5],
    "海南省": [108.6, 111.1, 18.1, 20.3],
    "重庆市": [105.3, 110.2, 28.1, 32.5],
    "四川省": [97.3, 108.5, 26.0, 34.5],
    "贵州省": [104.2, 109.6, 24.6, 29.5],
    "云南省": [97.5, 106.7, 21.1, 29.3],
    "西藏自治区": [78.2, 99.1, 26.7, 36.5],
    "陕西省": [105.4, 111.3, 31.7, 39.8],
    "甘肃省": [92.3, 108.8, 32.3, 42.9],
    "青海省": [89.4, 102.5, 31.6, 39.5],
    "宁夏回族自治区": [104.2, 107.7, 35.2, 39.5],
    "新疆维吾尔自治区": [73.3, 96.4, 34.3, 49.5],
    "台湾省": [119.2, 122.6, 21.8, 25.4],
    "香港特别行政区": [113.8, 114.6, 22.1, 22.6],
    "澳门特别行政区": [113.5, 113.6, 22.1, 22.3],
}

# 直辖市经纬度范围（key = "省|省"，city == province）
_MUNICIPALITY_BOUNDS: dict[str, list[float]] = {
    "北京市|北京市": [116.0, 116.7, 39.7, 40.2],
    "天津市|天津市": [116.6, 118.2, 38.5, 40.3],
    "上海市|上海市": [120.8, 122.0, 30.7, 31.5],
    "重庆市|重庆市": [105.8, 107.0, 29.0, 30.0],
}

# 市一级更精细的范围（key 格式 "省|市"，若无精确数据则回退省级范围）
_CITY_BOUNDS: dict[str, list[float]] = {
    "广东省|广州市": [112.7, 114.1, 22.8, 23.7],
    "广东省|深圳市": [113.7, 114.6, 22.4, 22.9],
    "广东省|佛山市": [112.6, 113.3, 22.7, 23.3],
    "广东省|东莞市": [113.5, 114.0, 22.8, 23.2],
    "广东省|惠州市": [113.7, 115.0, 22.5, 23.6],
    "广东省|珠海市": [113.0, 113.8, 21.8, 22.4],
    "广东省|中山市": [113.0, 113.7, 22.4, 22.8],
    "广东省|江门市": [112.0, 113.4, 21.8, 22.8],
    "广东省|汕头市": [116.0, 117.3, 23.1, 23.7],
    "广东省|湛江市": [109.8, 110.9, 20.8, 21.8],
    "广东省|肇庆市": [112.0, 113.0, 23.0, 23.8],
    "广东省|茂名市": [110.5, 111.5, 21.4, 22.1],
    "广东省|梅州市": [115.3, 116.8, 23.5, 24.8],
    "广东省|汕尾市": [114.7, 116.0, 22.4, 23.4],
    "广东省|河源市": [113.8, 115.3, 23.2, 24.4],
    "广东省|阳江市": [111.5, 112.5, 21.5, 22.4],
    "广东省|清远市": [112.3, 113.7, 23.3, 24.8],
    "广东省|韶关市": [112.8, 114.5, 24.5, 25.5],
    "广东省|揭阳市": [115.5, 116.8, 22.8, 23.8],
    "广东省|潮州市": [116.5, 117.2, 23.4, 24.2],
    "广东省|云浮市": [111.5, 112.5, 22.5, 23.3],
    "浙江省|杭州市": [118.8, 120.6, 29.9, 30.5],
    "浙江省|宁波市": [120.8, 122.2, 29.6, 30.2],
    "浙江省|温州市": [120.2, 121.3, 27.8, 28.3],
    "浙江省|嘉兴市": [120.3, 121.3, 30.5, 31.0],
    "浙江省|湖州市": [119.5, 120.5, 30.5, 31.2],
    "浙江省|绍兴市": [120.1, 121.3, 29.7, 30.3],
    "浙江省|金华市": [119.3, 120.0, 28.8, 29.6],
    "浙江省|衢州市": [118.3, 119.5, 28.5, 29.3],
    "浙江省|舟山市": [121.5, 123.0, 29.7, 30.5],
    "浙江省|台州市": [120.5, 121.7, 28.2, 29.0],
    "浙江省|丽水市": [119.3, 120.2, 27.8, 28.8],
    "江苏省|南京市": [118.4, 119.2, 31.8, 32.4],
    "江苏省|苏州市": [120.2, 121.3, 31.1, 31.7],
    "江苏省|无锡市": [119.8, 120.5, 31.2, 31.8],
    "江苏省|常州市": [119.5, 120.2, 31.4, 32.0],
    "江苏省|镇江市": [119.2, 120.0, 31.5, 32.2],
    "江苏省|南通市": [120.3, 121.5, 31.7, 32.4],
    "江苏省|扬州市": [119.0, 120.0, 32.1, 33.0],
    "江苏省|盐城市": [119.5, 120.9, 32.8, 34.0],
    "江苏省|淮安市": [118.5, 119.5, 32.8, 34.0],
    "江苏省|连云港市": [118.5, 119.8, 33.8, 35.0],
    "江苏省|徐州市": [116.5, 118.0, 33.5, 34.8],
    "江苏省|泰州市": [119.5, 120.5, 31.9, 32.8],
    "江苏省|宿迁市": [117.5, 119.0, 33.2, 34.4],
    "四川省|成都市": [103.4, 104.6, 30.3, 31.0],
    "湖北省|武汉市": [113.7, 115.0, 29.9, 31.0],
    "湖南省|长沙市": [111.9, 113.3, 27.9, 28.5],
    "河南省|郑州市": [112.4, 114.0, 34.4, 35.0],
    "山东省|济南市": [116.3, 117.3, 36.2, 37.0],
    "山东省|青岛市": [119.8, 121.0, 35.8, 36.8],
    "福建省|福州市": [118.8, 119.6, 25.8, 26.5],
    "福建省|厦门市": [117.8, 118.4, 24.3, 24.8],
}


# 直辖市：北京/天津/上海/重庆 区直接在省下，没有地级市一层
_MUNICIPALITIES: frozenset[str] = frozenset({
    "北京市", "天津市", "上海市", "重庆市",
})


# ---------------------------------------------------------------------------
# 3. 核心 API
# ---------------------------------------------------------------------------

class ChinaRegion:
    """
    省市区三级随机选择 + 指定经纬度范围内的坐标生成。
    支持用户手动指定经纬度上下限（在 init 时传入，或在 generate() 时覆盖）。
    """

    def __init__(
        self,
        *,
        province: str | None = None,
        city: str | None = None,
        district: str | None = None,
        lon_min: float | None = None,
        lon_max: float | None = None,
        lat_min: float | None = None,
        lat_max: float | None = None,
    ) -> None:
        self._province: str | None = province
        self._city: str | None = city
        self._district: str | None = district
        self.lon_min = lon_min
        self.lon_max = lon_max
        self.lat_min = lat_min
        self.lat_max = lat_max

    # ---- 静态工厂方法 ----

    @staticmethod
    def random_full(
        *,
        lon_min: float | None = None,
        lon_max: float | None = None,
        lat_min: float | None = None,
        lat_max: float | None = None,
    ) -> ChinaRegion:
        """从全国随机选取一个省-市-区的完整地址。"""
        data = _load_region_data()
        provinces = data
        province_node = random.choice(provinces)
        province = province_node["label"]

        # 直辖市（北京/天津/上海/重庆）：区直接在省下面，市辖区挂在省下
        if province in _MUNICIPALITIES:
            districts = province_node.get("children") or []
            # districts[0] = {"label": "市辖区", "children": [{"label": "东城区"}, ...]}
            if districts and districts[0].get("children"):
                district = random.choice(districts[0]["children"])["label"]
            elif districts:
                district = districts[0]["label"]
            else:
                district = ""
            return ChinaRegion(
                province=province,
                city=province,  # 直辖市：city == province
                district=district,
                lon_min=lon_min,
                lon_max=lon_max,
                lat_min=lat_min,
                lat_max=lat_max,
            )

        cities = province_node.get("children") or []
        if not cities:
            city = province
            districts = []
        else:
            city_node = random.choice(cities)
            city = city_node["label"]
            districts = city_node.get("children") or []

        district = random.choice(districts)["label"] if districts else ""

        return ChinaRegion(
            province=province,
            city=city,
            district=district,
            lon_min=lon_min,
            lon_max=lon_max,
            lat_min=lat_min,
            lat_max=lat_max,
        )

    @staticmethod
    def random_province(province: str) -> ChinaRegion:
        """在指定省内随机选市和区。"""
        data = _load_region_data()
        province_node = next(
            (n for n in data if n["label"] == province), None
        )
        if province_node is None:
            raise ValueError(f"未找到省份: {province}")

        # 直辖市：区直接在省下，city == province
        if province in _MUNICIPALITIES:
            districts = province_node.get("children") or []
            if districts and districts[0].get("children"):
                district = random.choice(districts[0]["children"])["label"]
            else:
                district = random.choice(districts)["label"] if districts else ""
            return ChinaRegion(province=province, city=province, district=district)

        cities = province_node.get("children") or []
        if not cities:
            return ChinaRegion(province=province, city=province, district="")

        city_node = random.choice(cities)
        city = city_node["label"]
        districts = city_node.get("children") or []
        district = random.choice(districts)["label"] if districts else ""

        return ChinaRegion(province=province, city=city, district=district)

    @staticmethod
    def by_province_city(
        province: str,
        city: str,
        *,
        lon_min: float | None = None,
        lon_max: float | None = None,
        lat_min: float | None = None,
        lat_max: float | None = None,
    ) -> ChinaRegion:
        """指定省市，在对应市辖区/县内随机选一个区。"""
        data = _load_region_data()
        province_node = next(
            (n for n in data if n["label"] == province), None
        )
        if province_node is None:
            raise ValueError(f"未找到省份: {province}")

        # 直辖市：区直接在省下，无城市层，city == province
        if province in _MUNICIPALITIES:
            districts = province_node.get("children") or []
            if districts and districts[0].get("children"):
                district = random.choice(districts[0]["children"])["label"]
            elif districts:
                district = districts[0]["label"]
            else:
                district = ""
            return ChinaRegion(
                province=province,
                city=province,
                district=district,
                lon_min=lon_min,
                lon_max=lon_max,
                lat_min=lat_min,
                lat_max=lat_max,
            )

        cities = province_node.get("children") or []
        city_node = next(
            (c for c in cities if c["label"] == city), None
        )
        if city_node is None:
            raise ValueError(f"在 {province} 下未找到市: {city}")

        districts = city_node.get("children") or []
        district = random.choice(districts)["label"] if districts else ""

        return ChinaRegion(
            province=province,
            city=city,
            district=district,
            lon_min=lon_min,
            lon_max=lon_max,
            lat_min=lat_min,
            lat_max=lat_max,
        )

    # ---- 生成结果 ----

    def generate(self) -> dict:
        """
        返回地址字典，可直接作为 WmAddress / POST /user/address 的请求体。
        经纬度使用指定范围或根据省/市自动推断。
        """
        self._resolve_bounds()

        longitude = round(
            random.uniform(self.lon_min, self.lon_max), 6
        )
        latitude = round(
            random.uniform(self.lat_min, self.lat_max), 6
        )

        return {
            "province": self._province or "未知省",
            "city": self._city or "未知市",
            "district": self._district or "未知区",
            "detailAddress": self._random_detail(),
            "longitude": f"{longitude:.6f}",
            "latitude": f"{latitude:.6f}",
        }

    # ---- 私有方法 ----

    def _resolve_bounds(self) -> None:
        """
        经纬度范围推断与默认值填充。

        规则：
        - 用户显式指定的参数 → 直接使用
        - 未指定的参数 → 尝试按省/市/区推断；若推断不到 → 兜底北京市市辖区
        """
        lon_min, lon_max = self.lon_min, self.lon_max
        lat_min, lat_max = self.lat_min, self.lat_max

        key_city = (
            f"{self._province}|{self._city}"
            if self._province and self._city
            else ""
        )
        city_bounds = _CITY_BOUNDS.get(key_city)

        if city_bounds:
            lon_min = lon_min or city_bounds[0]
            lon_max = lon_max or city_bounds[1]
            lat_min = lat_min or city_bounds[2]
            lat_max = lat_max or city_bounds[3]
        elif self._province:
            prov_bounds = _PROVINCE_BOUNDS.get(self._province)
            if prov_bounds:
                lon_min = lon_min or prov_bounds[0]
                lon_max = lon_max or prov_bounds[1]
                lat_min = lat_min or prov_bounds[2]
                lat_max = lat_max or prov_bounds[3]

        # 兜底：未确定的参数全用北京市市辖区
        self.lon_min = lon_min or 116.0
        self.lon_max = lon_max or 116.7
        self.lat_min = lat_min or 39.7
        self.lat_max = lat_max or 40.2

    @staticmethod
    def _random_detail() -> str:
        """生成一个仿真的详细地址。"""
        streets = [
            "人民路88号",
            "中山北路123号",
            "建设路456号",
            "解放东路789号",
            "新城大道1号",
            "科技园A座",
            "步行街B区",
            "商业中心C栋",
            "工业园12号",
            "大学城东区",
            "美食街34号",
            "文化广场南侧",
            "体育馆旁",
            "医院对面",
            "火车站西广场",
        ]
        return random.choice(streets)


def generate_address(
    province: str | None = None,
    city: str | None = None,
    district: str | None = None,
    lon_min: float | None = None,
    lon_max: float | None = None,
    lat_min: float | None = None,
    lat_max: float | None = None,
) -> dict:
    """
    快速函数：生成一条地址数据（省市区 + 详细地址 + 经纬度）。

    - 若只传 province：仅在该省内随机选市区
    - 若传 province + city：仅在该市内随机选区
    - 若传 province + city + district：直接使用，不再随机
    - lon_min / lon_max / lat_min / lat_max：手动覆盖经纬度范围

    示例：
        generate_address("广东省", "广州市")
        generate_address("广东省", lon_min=113.0, lon_max=114.0, lat_min=22.8, lat_max=23.5)

    默认行为（不传任何参数）：使用北京市，city = province，district 从市辖区下随机。
    """
    if province and city and district:
        region = ChinaRegion(
            province=province, city=city, district=district,
            lon_min=lon_min, lon_max=lon_max, lat_min=lat_min, lat_max=lat_max,
        )
    elif province and city:
        region = ChinaRegion.by_province_city(
            province, city,
            lon_min=lon_min, lon_max=lon_max, lat_min=lat_min, lat_max=lat_max,
        )
    elif province:
        region = ChinaRegion.random_province(province)
    else:
        # 默认北京市市辖区
        region = ChinaRegion.by_province_city(
            "北京市", "市辖区",
            lon_min=lon_min, lon_max=lon_max, lat_min=lat_min, lat_max=lat_max,
        )
    return region.generate()
