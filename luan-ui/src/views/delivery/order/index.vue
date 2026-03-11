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
					title="未获取到骑手身份或配送范围，请先维护骑手信息"
					type="warning"
					show-icon
					:closable="false"
					class="mb20"
				/>

				<el-form :inline="true" :model="queryForm" @keyup.enter="loadData">
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
					<el-table-column prop="orderNo" label="订单号" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.orderNo" show-overflow-tooltip />
					<el-table-column label="客户" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.customerName" show-overflow-tooltip>
						<template #default="scope">
							{{ scope.row.customerName || '-' }}
						</template>
					</el-table-column>
					<el-table-column label="商家" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.merchantName" show-overflow-tooltip>
						<template #default="scope">
							{{ scope.row.merchantName || '-' }}
						</template>
					</el-table-column>
					<el-table-column label="配送地址" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.deliveryAddress" show-overflow-tooltip>
						<template #default="scope">
							{{ formatAddress(scope.row.customerAddress) }}
						</template>
					</el-table-column>
					<el-table-column label="配送距离" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.distanceKm">
						<template #default="scope">
							{{ scope.row.distanceKmText }} km
						</template>
					</el-table-column>
					<el-table-column label="订单金额(元)" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.totalAmount">
						<template #default="scope">
							{{ formatMoney(scope.row.totalAmount) }}
						</template>
					</el-table-column>
					<el-table-column label="应付金额(元)" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.payAmount">
						<template #default="scope">
							{{ formatMoney(scope.row.payAmount) }}
						</template>
					</el-table-column>
					<el-table-column label="订单状态" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.orderStatus">
						<template #default="scope">
							<el-tag type="info">{{ getStatusLabel(scope.row.orderStatus) }}</el-tag>
						</template>
					</el-table-column>
					<el-table-column label="菜品明细" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.orderItems" show-overflow-tooltip>
						<template #default="scope">
							{{ formatOrderItems(scope.row.orderItems) }}
						</template>
					</el-table-column>
					<el-table-column label="时间" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.timeInfo">
						<template #default="scope">
							<el-tooltip placement="top-start" :show-after="200">
								<template #content>
									<div class="time-tooltip-content">{{ getOrderTimeText(scope.row) }}</div>
								</template>
								<div
									class="time-cell"
									:class="{ 'is-expanded': isTimeExpanded(scope.row) }"
									@click="toggleTimeExpanded(scope.row)"
								>
									{{ getOrderTimeText(scope.row) }}
								</div>
							</el-tooltip>
						</template>
					</el-table-column>
					<el-table-column prop="remark" label="备注" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.remark" show-overflow-tooltip />
					<el-table-column label="操作" :width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.deliveryOrderActions" fixed="right">
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
import { currentRider } from '/@/api/takeaway/delivery';
import { deliveryStartOrder, pageList as pageOrderList } from '/@/api/takeaway/order';
import { useUserInfo } from '/@/stores/userInfo';
import type { Pagination, TableStyle } from '/@/hooks/table';
import { TAKEAWAY_ORDER_TABLE_COL_WIDTH } from '/@/constants/takeawayOrderTable';
import { getOrderTimelineText } from '/@/utils/takeawayOrderTime';

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
	payTime?: string;
	merchantAcceptTime?: string;
	deliveryStartTime?: string;
	finishTime?: string;
	remark?: string;
	customerAddress?: AddressItem;
	merchantAddress?: AddressItem;
	orderItems?: Array<{ dishName?: string; quantity?: number }>;
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

const loading = ref(false);
const acceptingId = ref('');
const riderReady = ref(false);
const riderUserId = ref<number | undefined>();
const riderScopeKm = ref<number>(0);
const allRows = ref<OrderRow[]>([]);
const expandedTimeMap = reactive<Record<string, boolean>>({});

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

const getOrderTimeText = (row: Record<string, unknown>) => getOrderTimelineText(row);

const getRowKey = (row: Record<string, unknown>) => {
	return normalizeId(row.id) || normalizeId(row.orderNo) || '';
};

const isTimeExpanded = (row: Record<string, unknown>) => {
	const key = getRowKey(row);
	return key ? !!expandedTimeMap[key] : false;
};

const toggleTimeExpanded = (row: Record<string, unknown>) => {
	const key = getRowKey(row);
	if (!key) return;
	expandedTimeMap[key] = !expandedTimeMap[key];
};

const toNumber = (value: unknown): number | undefined => {
	const num = Number(value);
	return Number.isNaN(num) ? undefined : num;
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

const formatOrderItems = (items: Array<{ dishName?: string; quantity?: number }> | undefined) => {
	if (!items?.length) {
		return '-';
	}
	return items
		.map((item) => `${item.dishName || '未知菜品'}x${Number(item.quantity || 0)}`)
		.join(', ');
};

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

	riderUserId.value = toNumber(rider.userId) || getCurrentRiderUserId();
	riderScopeKm.value = scopeKm;
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
		const scopeLimit = riderScopeKm.value;

		const res = await pageOrderList({
			current: 1,
			size: 500,
			status: ORDER_STATUS.MERCHANT_ACCEPTED,
		});
		const records: OrderRow[] = res?.data?.records || [];

		// 在前端进行距离筛选：商家地址到客户地址距离 < 骑手配送范围
		const matched = records
			.map((row) => {
				const customerAddress = row.customerAddress;
				const merchantAddress = row.merchantAddress;
				if (!customerAddress || !merchantAddress) {
					return null;
				}
				const distanceKm = calcDistanceKm(merchantAddress, customerAddress);
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

.time-cell {
	line-height: 1.5;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	cursor: pointer;
	color: var(--el-text-color-regular);
}

.time-cell.is-expanded {
	white-space: pre-line;
}

.time-tooltip-content {
	white-space: pre-line;
	line-height: 1.6;
	max-width: 360px;
}
</style>
