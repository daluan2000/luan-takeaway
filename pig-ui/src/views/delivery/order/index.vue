<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card v-loading="loading">
				<template #header>
					<div class="card-header">
						<span>配送接单</span>
						<el-button link type="primary" @click="loadData">刷新</el-button>
					</div>
				</template>

				<el-alert
					v-if="!riderReady"
					title="未获取到骑手当前位置或配送范围，请先维护骑手信息和地址经纬度"
					type="warning"
					show-icon
					:closable="false"
					class="mb20"
				/>

				<el-form :inline="true" :model="queryForm" @keyup.enter="loadData">
					<el-form-item label="骑手所在城市">
						<el-input v-model="displayRegion" disabled style="width: 220px" placeholder="未获取" />
					</el-form-item>
					<el-form-item label="配送范围(公里)">
						<el-input v-model="displayScope" disabled style="width: 180px" placeholder="未设置" />
					</el-form-item>
					<el-form-item>
						<el-button type="primary" icon="Search" @click="loadData">查询</el-button>
					</el-form-item>
				</el-form>

				<el-table
					:data="pageRows"
					v-loading="loading"
					border
					style="width: 100%"
					:cell-style="tableStyle.cellStyle"
					:header-cell-style="tableStyle.headerCellStyle"
				>
					<el-table-column type="index" label="序号" width="70" />
					<el-table-column prop="orderNo" label="订单号" min-width="220" show-overflow-tooltip />
					<el-table-column label="客户" min-width="140" show-overflow-tooltip>
						<template #default="scope">
							{{ scope.row.customerName || '-' }}
						</template>
					</el-table-column>
					<el-table-column label="商家" min-width="160" show-overflow-tooltip>
						<template #default="scope">
							{{ scope.row.merchantName || '-' }}
						</template>
					</el-table-column>
					<el-table-column label="配送地址" min-width="220" show-overflow-tooltip>
						<template #default="scope">
							{{ formatAddress(scope.row.customerAddress) }}
						</template>
					</el-table-column>
					<el-table-column label="距离骑手(公里)" min-width="130">
						<template #default="scope">
							{{ scope.row.distanceKmText }}
						</template>
					</el-table-column>
					<el-table-column label="订单金额(元)" min-width="130">
						<template #default="scope">
							{{ formatMoney(scope.row.totalAmount) }}
						</template>
					</el-table-column>
					<el-table-column label="应付金额(元)" min-width="130">
						<template #default="scope">
							{{ formatMoney(scope.row.payAmount) }}
						</template>
					</el-table-column>
					<el-table-column label="订单状态" min-width="140">
						<template #default="scope">
							<el-tag type="info">{{ getStatusLabel(scope.row.orderStatus) }}</el-tag>
						</template>
					</el-table-column>
					<el-table-column prop="createTime" label="下单时间" min-width="180" show-overflow-tooltip />
					<el-table-column label="操作" width="160" fixed="right">
						<template #default="scope">
							<el-button
								text
								type="primary"
								:loading="acceptingId === String(scope.row.id)"
								@click="handleAccept(scope.row)"
							>
								接单
							</el-button>
						</template>
					</el-table-column>
				</el-table>

				<pagination
					v-bind="pagination"
					@current-change="currentChangeHandle"
					@size-change="sizeChangeHandle"
				/>
			</el-card>
		</div>
	</div>
</template>

<script setup lang="ts" name="deliveryOrderIndex">
import { useMessage, useMessageBox } from '/@/hooks/message';
import { listAddress } from '/@/api/takeaway/address';
import { currentRider } from '/@/api/takeaway/delivery';
import { deliveryStartOrder, pageList as pageOrderList } from '/@/api/takeaway/order';
import { useUserInfo } from '/@/stores/userInfo';
import type { Pagination, TableStyle } from '/@/hooks/table';

interface AddressItem {
	id?: string | number;
	province?: string;
	city?: string;
	district?: string;
	detailAddress?: string;
	longitude?: number | string;
	latitude?: number | string;
}

interface RiderInfo {
	id?: string | number;
	userId?: string | number;
	realName?: string;
	deliveryScopeKm?: number | string;
	onlineStatus?: string;
	employmentStatus?: string;
	noExist?: boolean;
}

interface OrderRow {
	id?: string | number;
	orderNo?: string;
	customerName?: string;
	merchantName?: string;
	totalAmount?: number;
	payAmount?: number;
	orderStatus?: string;
	createTime?: string;
	customerAddress?: AddressItem;
	distanceKm?: number;
	distanceKmText?: string;
}

const ORDER_STATUS = {
	MERCHANT_ACCEPTED: '2',
} as const;

const STATUS_LABEL_MAP: Record<string, string> = {
	'0': '待支付',
	'1': '已支付',
	'2': '商家已接单',
	'3': '配送中',
	'4': '已完成',
	'5': '已取消',
};

const MUNICIPALITIES = ['北京市', '上海市', '天津市', '重庆市'];

const loading = ref(false);
const acceptingId = ref('');
const riderReady = ref(false);
const riderUserId = ref<number | undefined>();
const riderScopeKm = ref<number>(0);
const riderAddress = ref<AddressItem | null>(null);
const allRows = ref<OrderRow[]>([]);

const queryForm = reactive({});

const pagination = reactive<Pagination>({
	current: 1,
	size: 10,
	total: 0,
	pageSizes: [10, 20, 50, 100],
	layout: 'total, sizes, prev, pager, next, jumper',
});

const tableStyle: TableStyle = {
	cellStyle: { textAlign: 'center' },
	headerCellStyle: {
		textAlign: 'center',
		background: 'var(--el-table-row-hover-bg-color)',
		color: 'var(--el-text-color-primary)',
	},
	rowStyle: { textAlign: 'center' },
};

const displayRegion = computed(() => resolveDisplayRegion(riderAddress.value || undefined));

const displayScope = computed(() => {
	if (!riderScopeKm.value || Number.isNaN(riderScopeKm.value)) {
		return '';
	}
	return riderScopeKm.value.toFixed(2);
});

const pageRows = computed(() => {
	const current = Number(pagination.current || 1);
	const size = Number(pagination.size || 10);
	const start = (current - 1) * size;
	return allRows.value.slice(start, start + size);
});

const normalizeId = (value: unknown): string | undefined => {
	if (value === null || value === undefined || value === '') {
		return undefined;
	}
	return String(value);
};

const toNumber = (value: unknown): number | undefined => {
	const num = Number(value);
	return Number.isNaN(num) ? undefined : num;
};

const resolveDisplayRegion = (address?: AddressItem | null) => {
	if (!address) return '';
	const province = String(address.province || '').trim();
	const city = String(address.city || '').trim();
	if (MUNICIPALITIES.includes(province)) {
		return province;
	}
	return city;
};

const calcDistanceKm = (from: AddressItem, to: AddressItem) => {
	const fromLng = Number(from.longitude);
	const fromLat = Number(from.latitude);
	const toLng = Number(to.longitude);
	const toLat = Number(to.latitude);
	if ([fromLng, fromLat, toLng, toLat].some((item) => Number.isNaN(item))) {
		return undefined;
	}
	const avgLatRad = ((fromLat + toLat) / 2) * (Math.PI / 180);
	const dxKm = (toLng - fromLng) * 111 * Math.cos(avgLatRad);
	const dyKm = (toLat - fromLat) * 111;
	return Math.sqrt(dxKm * dxKm + dyKm * dyKm);
};

const formatAddress = (address?: AddressItem) => {
	if (!address) return '-';
	const text = [address.province, address.city, address.district, address.detailAddress].filter(Boolean).join('');
	return text || '-';
};

const formatMoney = (value: unknown) => {
	const amount = Number(value ?? 0);
	if (Number.isNaN(amount)) {
		return '0.00';
	}
	return amount.toFixed(2);
};

const getStatusLabel = (status: string) => STATUS_LABEL_MAP[status] || `未知状态(${status ?? '-'})`;

const currentChangeHandle = (val: number) => {
	pagination.current = val;
};

const sizeChangeHandle = (val: number) => {
	pagination.size = val;
	pagination.current = 1;
};

const getCurrentRiderUserId = () => {
	const currentUserId = Number(useUserInfo().userInfos?.user?.userId);
	if (!Number.isNaN(currentUserId) && currentUserId > 0) {
		return currentUserId;
	}
	return undefined;
};

const pickRiderLocation = (addresses: AddressItem[]) => {
	// 优先使用有经纬度且有城市信息的地址作为骑手当前位置
	const valid = addresses.find((item) => {
		const lng = toNumber(item.longitude);
		const lat = toNumber(item.latitude);
		return !!resolveDisplayRegion(item) && lng !== undefined && lat !== undefined;
	});
	if (valid) {
		return valid;
	}
	return addresses.find((item) => !!resolveDisplayRegion(item)) || null;
};

const prepareRiderContext = async () => {
	const riderRes = await currentRider();
	const rider: RiderInfo = riderRes?.data || {};
	if (rider.noExist || !rider.id) {
		throw new Error('当前用户暂无骑手扩展信息');
	}
	if (rider.onlineStatus !== '1') {
		throw new Error('当前骑手未在线，无法接单');
	}
	if (rider.employmentStatus !== '1') {
		throw new Error('当前骑手非在职状态，无法接单');
	}

	const scopeKm = Number(rider.deliveryScopeKm);
	if (Number.isNaN(scopeKm) || scopeKm <= 0) {
		throw new Error('请先设置有效的配送范围');
	}

	const addrRes = await listAddress();
	const addresses: AddressItem[] = addrRes?.data || [];
	const location = pickRiderLocation(addresses);
	if (!location) {
		throw new Error('未获取到骑手所在城市，请先补充地址信息');
	}
	if (toNumber(location.longitude) === undefined || toNumber(location.latitude) === undefined) {
		throw new Error('骑手地址缺少经纬度，无法计算配送距离');
	}

	riderUserId.value = toNumber(rider.userId) || getCurrentRiderUserId();
	riderScopeKm.value = scopeKm;
	riderAddress.value = location;
	riderReady.value = !!riderUserId.value;
	if (!riderReady.value) {
		throw new Error('未获取到当前骑手身份，无法接单');
	}
};

const loadData = async () => {
	loading.value = true;
	allRows.value = [];
	pagination.current = 1;
	pagination.total = 0;
	riderReady.value = false;
	try {
		await prepareRiderContext();
		const riderRegion = resolveDisplayRegion(riderAddress.value || undefined);
		const riderPosition = riderAddress.value as AddressItem;
		const scopeLimit = riderScopeKm.value;

		const res = await pageOrderList({
			current: 1,
			size: 500,
			status: ORDER_STATUS.MERCHANT_ACCEPTED,
		});
		const records: OrderRow[] = res?.data?.records || [];

        // 在前端进行距离筛选
		const matched = records
			.map((row) => {
				const customerAddress = row.customerAddress;
				if (!customerAddress) {
					return null;
				}
				const orderRegion = resolveDisplayRegion(customerAddress);
				if (!orderRegion || orderRegion !== riderRegion) {
					return null;
				}
				const distanceKm = calcDistanceKm(riderPosition, customerAddress);
				if (distanceKm === undefined || distanceKm >= scopeLimit) {
					return null;
				}
				return {
					...row,
					distanceKm,
					distanceKmText: distanceKm.toFixed(2),
				};
			})
			.filter(Boolean) as OrderRow[];

		allRows.value = matched;
		pagination.total = matched.length;
	} catch (error: any) {
		useMessage().warning(error?.message || error?.msg || '加载失败');
	} finally {
		loading.value = false;
	}
};

const handleAccept = async (row: OrderRow) => {
	const orderId = normalizeId(row?.id);
	if (!orderId || !riderUserId.value) {
		useMessage().warning('订单或骑手信息缺失，无法接单');
		return;
	}
	try {
		await useMessageBox().confirm(`确认接单「${row.orderNo || row.id}」吗？`);
	} catch {
		return;
	}

	acceptingId.value = orderId;
	try {
		await deliveryStartOrder(orderId, riderUserId.value);
		useMessage().success('接单成功，已开始配送');
		await loadData();
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '接单失败');
	} finally {
		acceptingId.value = '';
	}
};

onMounted(async () => {
	await loadData();
});
</script>

<style scoped>
.card-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
}
</style>
