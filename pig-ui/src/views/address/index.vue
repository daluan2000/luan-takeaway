<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card>
				<template #header>
					<div class="card-header">
						<span>我的地址</span>
						<el-button type="primary" @click="openCreate">手动新增地址</el-button>
					</div>
				</template>

				<div class="map-section">
					<div class="map-panel">
						<div ref="mapContainerRef" class="map-container"></div>
						<div class="map-tip">点击地图任意位置选择地址点</div>
					</div>

					<div class="map-info">
						<el-descriptions title="选点信息" :column="1" border size="small">
							<el-descriptions-item label="经度">{{ pickedAddress.longitude || '-' }}</el-descriptions-item>
							<el-descriptions-item label="纬度">{{ pickedAddress.latitude || '-' }}</el-descriptions-item>
							<el-descriptions-item label="省">{{ pickedAddress.province || '-' }}</el-descriptions-item>
							<el-descriptions-item label="市">{{ pickedAddress.city || '-' }}</el-descriptions-item>
							<el-descriptions-item label="区">{{ pickedAddress.district || '-' }}</el-descriptions-item>
							<el-descriptions-item label="详细地址">{{ pickedAddress.detailAddress || '-' }}</el-descriptions-item>
						</el-descriptions>

						<div class="map-info-actions">
							<el-button type="primary" :disabled="!pickedAddress.longitude || !pickedAddress.latitude" :loading="addingFromMap || resolvingAddress" @click="handleAddFromMap">
								地图选点新增
							</el-button>
						</div>
					</div>
				</div>

				<el-table :data="tableData" v-loading="loading" border>
					<el-table-column type="index" label="序号" width="70" />
					<el-table-column prop="province" label="省" min-width="110" show-overflow-tooltip />
					<el-table-column prop="city" label="市" min-width="110" show-overflow-tooltip />
					<el-table-column prop="district" label="区" min-width="110" show-overflow-tooltip />
					<el-table-column prop="detailAddress" label="详细地址" min-width="220" show-overflow-tooltip />
					<el-table-column prop="longitude" label="经度" min-width="120" show-overflow-tooltip />
					<el-table-column prop="latitude" label="纬度" min-width="120" show-overflow-tooltip />
					<el-table-column label="操作" width="180" fixed="right">
						<template #default="scope">
							<el-button type="primary" text @click="openEdit(scope.row)">编辑</el-button>
							<el-button type="primary" text @click="handleDelete(scope.row)">删除</el-button>
						</template>
					</el-table-column>
				</el-table>

			</el-card>
		</div>

		<el-dialog v-model="dialogVisible" :title="form.id ? '编辑地址' : '新增地址'" width="640px" destroy-on-close>
			<el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
				<el-row :gutter="12">
					<el-col :span="8">
						<el-form-item label="省" prop="province">
							<el-input v-model="form.province" maxlength="32" placeholder="请输入省" />
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="市" prop="city">
							<el-input v-model="form.city" maxlength="32" placeholder="请输入市" />
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="区" prop="district">
							<el-input v-model="form.district" maxlength="32" placeholder="请输入区" />
						</el-form-item>
					</el-col>
				</el-row>

				<el-form-item label="详细地址" prop="detailAddress">
					<el-input v-model="form.detailAddress" maxlength="255" placeholder="请输入详细地址" />
				</el-form-item>

				<el-row :gutter="12">
					<el-col :span="12">
						<el-form-item label="经度" prop="longitude">
							<el-input v-model="form.longitude" placeholder="如：116.397128" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="纬度" prop="latitude">
							<el-input v-model="form.latitude" placeholder="如：39.916527" />
						</el-form-item>
					</el-col>
				</el-row>
			</el-form>

			<template #footer>
				<el-button @click="dialogVisible = false">取消</el-button>
				<el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup lang="ts" name="addressIndex">
import { useMessage, useMessageBox } from '/@/hooks/message';
import { addAddress, delAddress, getAddress, listAddress, updateAddress } from '/@/api/takeaway/address';

interface AddressForm {
	id?: number;
	province: string;
	city: string;
	district: string;
	detailAddress: string;
	longitude: string;
	latitude: string;
}

interface PickedAddress {
	province: string;
	city: string;
	district: string;
	detailAddress: string;
	longitude: string;
	latitude: string;
}

type LeafletGlobal = {
	map: (el: HTMLElement) => any;
	tileLayer: (url: string, options?: Record<string, unknown>) => { addTo: (map: any) => void };
	marker: (latLng: [number, number]) => {
		addTo: (map: any) => any;
		setLatLng: (latLng: [number, number]) => void;
	};
};

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const tableData = ref<any[]>([]);
const formRef = ref();
const mapContainerRef = ref<HTMLElement>();
const resolvingAddress = ref(false);
const addingFromMap = ref(false);

const form = reactive<AddressForm>({
	id: undefined,
	province: '',
	city: '',
	district: '',
	detailAddress: '',
	longitude: '',
	latitude: '',
});

const pickedAddress = reactive<PickedAddress>({
	province: '',
	city: '',
	district: '',
	detailAddress: '',
	longitude: '',
	latitude: '',
});

let leafletMap: any = null;
let leafletMarker: any = null;
let leafletLoader: Promise<void> | null = null;

const rules = reactive({
	province: [{ required: true, message: '请输入省', trigger: 'blur' }],
	city: [{ required: true, message: '请输入市', trigger: 'blur' }],
	district: [{ required: true, message: '请输入区', trigger: 'blur' }],
	detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
	longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }],
	latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
});

const resetForm = () => {
	form.id = undefined;
	form.province = '';
	form.city = '';
	form.district = '';
	form.detailAddress = '';
	form.longitude = '';
	form.latitude = '';
};

const loadList = async () => {
	loading.value = true;
	try {
		const res = await listAddress();
		tableData.value = res?.data || [];
	} finally {
		loading.value = false;
	}
};

const loadStyle = (href: string, id: string) => {
	if (document.getElementById(id)) return;
	const link = document.createElement('link');
	link.id = id;
	link.rel = 'stylesheet';
	link.href = href;
	document.head.appendChild(link);
};

const loadScript = (src: string, id: string) => {
	if (document.getElementById(id)) return Promise.resolve();
	return new Promise<void>((resolve, reject) => {
		const script = document.createElement('script');
		script.id = id;
		script.src = src;
		script.async = true;
		script.onload = () => resolve();
		script.onerror = () => reject(new Error('地图脚本加载失败'));
		document.head.appendChild(script);
	});
};

const ensureLeaflet = async () => {
	if (leafletLoader) return leafletLoader;

	loadStyle('https://unpkg.com/leaflet@1.9.4/dist/leaflet.css', 'leaflet-style');
	leafletLoader = loadScript('https://unpkg.com/leaflet@1.9.4/dist/leaflet.js', 'leaflet-script');
	return leafletLoader;
};

const toAddressValue = (value?: string) => value?.trim() || '';

const MUNICIPALITIES = ['北京市', '上海市', '天津市', '重庆市'];

const REGION_CODE_TO_NAME: Record<string, string> = {
	'CN-BJ': '北京市',
	'CN-SH': '上海市',
	'CN-TJ': '天津市',
	'CN-CQ': '重庆市',
	'CN-HE': '河北省',
	'CN-SX': '山西省',
	'CN-NM': '内蒙古自治区',
	'CN-LN': '辽宁省',
	'CN-JL': '吉林省',
	'CN-HL': '黑龙江省',
	'CN-JS': '江苏省',
	'CN-ZJ': '浙江省',
	'CN-AH': '安徽省',
	'CN-FJ': '福建省',
	'CN-JX': '江西省',
	'CN-SD': '山东省',
	'CN-HA': '河南省',
	'CN-HB': '湖北省',
	'CN-HN': '湖南省',
	'CN-GD': '广东省',
	'CN-GX': '广西壮族自治区',
	'CN-HI': '海南省',
	'CN-SC': '四川省',
	'CN-GZ': '贵州省',
	'CN-YN': '云南省',
	'CN-XZ': '西藏自治区',
	'CN-SN': '陕西省',
	'CN-GS': '甘肃省',
	'CN-QH': '青海省',
	'CN-NX': '宁夏回族自治区',
	'CN-XJ': '新疆维吾尔自治区',
	'CN-HK': '香港特别行政区',
	'CN-MO': '澳门特别行政区',
	'CN-TW': '台湾省',
};

const pickFirstNonEmpty = (...values: Array<string | undefined>) => {
	for (const value of values) {
		const normalized = toAddressValue(value);
		if (normalized) return normalized;
	}
	return '';
};

const normalizeRegionName = (value?: string) => {
	const normalized = toAddressValue(value);
	if (!normalized) return '';
	return REGION_CODE_TO_NAME[normalized.toUpperCase()] || normalized;
};

const extractProvinceFromText = (text: string) => {
	const source = toAddressValue(text);
	if (!source) return '';

	const municipalityMatch = source.match(/(北京市|上海市|天津市|重庆市)/);
	if (municipalityMatch?.[1]) return municipalityMatch[1];

	const provinceLikeMatch = source.match(/([^,，\s]+(?:省|自治区|特别行政区))/);
	return provinceLikeMatch?.[1] || '';
};

const parseGeoAddress = (raw: any) => {
	const addr = raw?.address || {};
	const detailAddress = toAddressValue(raw?.display_name);
	const city = normalizeRegionName(pickFirstNonEmpty(addr.city, addr.town, addr.municipality, addr.county, addr.state_district, addr.state));
	const district = pickFirstNonEmpty(addr.city_district, addr.suburb, addr.county, addr.quarter, addr.borough, addr.neighbourhood);

	let province = normalizeRegionName(pickFirstNonEmpty(addr.state, addr.province, addr.region, addr['ISO3166-2-lvl4']));
	if (!province) {
		province = extractProvinceFromText(detailAddress);
	}
	if (!province && MUNICIPALITIES.includes(city)) {
		province = city;
	}

	const normalizedCity = city || (MUNICIPALITIES.includes(province) ? province : '');

	return {
		province: province || '未知省',
		city: normalizedCity || '未知市',
		district: district || '未知区',
		detailAddress: detailAddress || '地图选点地址',
	};
};

const reverseGeocode = async (latitude: number, longitude: number) => {
	resolvingAddress.value = true;
	try {
		const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&accept-language=zh-CN&lat=${latitude}&lon=${longitude}`;
		const res = await fetch(url, {
			headers: {
				'Accept-Language': 'zh-CN,zh;q=0.9',
			},
		});

		if (!res.ok) {
			throw new Error('逆地理解析失败');
		}

		const data = await res.json();
		const parsed = parseGeoAddress(data);
		pickedAddress.province = parsed.province;
		pickedAddress.city = parsed.city;
		pickedAddress.district = parsed.district;
		pickedAddress.detailAddress = parsed.detailAddress;
	} catch {
		pickedAddress.province = '未知省';
		pickedAddress.city = '未知市';
		pickedAddress.district = '未知区';
		pickedAddress.detailAddress = '地图选点地址';
		useMessage().warning('地址解析失败，已使用默认地址信息');
	} finally {
		resolvingAddress.value = false;
	}
};

const handleMapClick = async (latitude: number, longitude: number) => {
	pickedAddress.longitude = longitude.toFixed(6);
	pickedAddress.latitude = latitude.toFixed(6);

	if (!leafletMarker) {
		const L = (window as any).L as LeafletGlobal;
		leafletMarker = L.marker([latitude, longitude]).addTo(leafletMap);
	} else {
		leafletMarker.setLatLng([latitude, longitude]);
	}

	await reverseGeocode(latitude, longitude);
};

const initMap = async () => {
	if (!mapContainerRef.value || leafletMap) return;

	try {
		await ensureLeaflet();
		const L = (window as any).L as LeafletGlobal;
		leafletMap = L.map(mapContainerRef.value);
		L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
			maxZoom: 18,
			attribution: '&copy; OpenStreetMap contributors',
		}).addTo(leafletMap);
		leafletMap.setView([39.9042, 116.4074], 11);

		leafletMap.on('click', async (event: any) => {
			const { lat, lng } = event?.latlng || {};
			if (typeof lat !== 'number' || typeof lng !== 'number') return;
			await handleMapClick(lat, lng);
		});
	} catch (error: any) {
		useMessage().error(error?.message || '地图初始化失败');
	}
};

const openCreate = () => {
	resetForm();
	dialogVisible.value = true;
};

const openEdit = async (row: any) => {
	const res = await getAddress(row.id);
	const data = res?.data || row;
	form.id = data.id;
	form.province = data.province || '';
	form.city = data.city || '';
	form.district = data.district || '';
	form.detailAddress = data.detailAddress || '';
	form.longitude = data.longitude ? String(data.longitude) : '';
	form.latitude = data.latitude ? String(data.latitude) : '';
	dialogVisible.value = true;
};

const buildPayload = () => {
	const payload: any = {
		province: form.province,
		city: form.city,
		district: form.district,
		detailAddress: form.detailAddress,
		longitude: form.longitude,
		latitude: form.latitude,
	};
	if (form.id) {
		payload.id = form.id;
	}
	return payload;
};

const handleSubmit = async () => {
	await formRef.value.validate(async (valid: boolean) => {
		if (!valid) return;

		submitting.value = true;
		try {
			const payload = buildPayload();
			if (form.id) {
				await updateAddress(payload);
				useMessage().success('修改成功');
			} else {
				await addAddress(payload);
				useMessage().success('新增成功');
			}
			dialogVisible.value = false;
			await loadList();
		} catch (error: any) {
			useMessage().error(error?.msg || '提交失败');
		} finally {
			submitting.value = false;
		}
	});
};

const handleDelete = async (row: any) => {
	try {
		await useMessageBox().confirm('确认删除该地址吗？');
	} catch {
		return;
	}

	try {
		await delAddress(row.id);
		useMessage().success('删除成功');
		await loadList();
	} catch (error: any) {
		useMessage().error(error?.msg || '删除失败');
	}
};

const handleAddFromMap = async () => {
	if (!pickedAddress.longitude || !pickedAddress.latitude) {
		useMessage().warning('请先在地图上选择一个点');
		return;
	}

	addingFromMap.value = true;
	try {
		await addAddress({
			province: pickedAddress.province || '未知省',
			city: pickedAddress.city || '未知市',
			district: pickedAddress.district || '未知区',
			detailAddress: pickedAddress.detailAddress || '地图选点地址',
			longitude: pickedAddress.longitude,
			latitude: pickedAddress.latitude,
		});
		useMessage().success('地图选点地址新增成功');
		await loadList();
	} catch (error: any) {
		useMessage().error(error?.msg || '新增失败');
	} finally {
		addingFromMap.value = false;
	}
};

onMounted(() => {
	loadList();
	nextTick(() => {
		initMap();
	});
});

onBeforeUnmount(() => {
	if (leafletMap) {
		leafletMap.remove();
		leafletMap = null;
		leafletMarker = null;
	}
});
</script>

<style scoped>
.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.map-section {
	display: flex;
	gap: 16px;
	align-items: stretch;
}

.map-panel {
	flex: 1;
	min-width: 0;
}

.map-container {
	height: 380px;
	width: 100%;
	border-radius: 8px;
	overflow: hidden;
	border: 1px solid var(--el-border-color);
}

.map-tip {
	margin-top: 10px;
	font-size: 13px;
	color: var(--el-text-color-secondary);
}

.map-info {
	width: 360px;
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.map-info-actions {
	display: flex;
	justify-content: flex-end;
}

@media (max-width: 1200px) {
	.map-section {
		flex-direction: column;
	}

	.map-info {
		width: 100%;
	}
}
</style>
