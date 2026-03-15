<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card>
				<template #header>
					<div class="card-header">
						<span>我的配送单</span>
						<el-button link type="primary" @click="loadData">刷新</el-button>
					</div>
				</template>

				<el-alert
					v-if="!riderReady"
					title="未获取到当前骑手身份，请先完善骑手信息"
					type="warning"
					show-icon
					:closable="false"
					class="mb20"
				/>

				<el-form :inline="true" :model="queryForm" @keyup.enter="loadData">
					<el-form-item label="订单状态" prop="status">
						<el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 220px" @change="onStatusChange">
							<el-option v-for="item in displayStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
						</el-select>
					</el-form-item>
					<el-form-item>
						<el-button type="primary" icon="Search" @click="loadData">查询</el-button>
						<el-button icon="Refresh" @click="resetQuery">重置</el-button>
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
							<el-tag :type="getStatusTagType(scope.row.orderStatus)">
								{{ getStatusLabel(scope.row.orderStatus) }}
							</el-tag>
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
					<el-table-column label="操作" :width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.deliverySendActions" fixed="right">
						<template #default="scope">
							<el-button
								v-auth="'wm_delivery_order_finish'"
								v-if="scope.row.orderStatus === ORDER_STATUS.DELIVERING"
								text
								type="primary"
								:loading="finishingId === String(scope.row.id)"
								@click="handleFinish(scope.row)"
							>
								配送完成
							</el-button>
							<span v-else class="op-disabled">暂无操作</span>
						</template>
					</el-table-column>
				</el-table>

				<pagination v-bind="pagination" @current-change="currentChangeHandle" @size-change="sizeChangeHandle" />
			</el-card>
		</div>
	</div>
</template>

<script setup lang="ts" name="deliverySendIndex">
import { useMessage, useMessageBox } from '/@/hooks/message';
import { currentRider } from '/@/api/takeaway/delivery';
import { deliveryFinishOrder, pageList as pageOrderList } from '/@/api/takeaway/order';
import { useUserInfo } from '/@/stores/userInfo';
import type { Pagination, TableStyle } from '/@/hooks/table';
import { TAKEAWAY_ORDER_TABLE_COL_WIDTH } from '/@/constants/takeawayOrderTable';
import { getOrderTimelineText } from '/@/utils/takeawayOrderTime';
import { useDict } from '/@/hooks/dict';

interface AddressItem {
	province?: string;
	city?: string;
	district?: string;
	detailAddress?: string;
}

interface RiderInfo {
	id?: string | number;
	userId?: string | number;
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
	payTime?: string;
	merchantAcceptTime?: string;
	deliveryStartTime?: string;
	finishTime?: string;
	remark?: string;
	customerAddress?: AddressItem;
	orderItems?: Array<{ dishName?: string; quantity?: number }>;
	createTime?: string;
}

const ORDER_STATUS = {
	DELIVERING: '3',
	FINISHED: '4',
} as const;

const ORDER_STATUS_OPTIONS = [
	{ label: '配送中', value: ORDER_STATUS.DELIVERING },
	{ label: '配送已完成', value: ORDER_STATUS.FINISHED },
];

const { takeaway_order_status } = useDict('takeaway_order_status');

const statusOptionsFromDict = computed(() => {
	const all = takeaway_order_status.value || [];
	return all
		.filter((item: any) => String(item.value) === ORDER_STATUS.DELIVERING || String(item.value) === ORDER_STATUS.FINISHED)
		.map((item: any) => ({ label: item.label, value: String(item.value) }));
});

const displayStatusOptions = computed(() => {
	return statusOptionsFromDict.value.length ? statusOptionsFromDict.value : ORDER_STATUS_OPTIONS;
});

const loading = ref(false);
const riderReady = ref(false);
const riderUserId = ref<string | number | undefined>();
const finishingId = ref('');
const sourceRows = ref<OrderRow[]>([]);
const allRows = ref<OrderRow[]>([]);
const expandedTimeMap = reactive<Record<string, boolean>>({});

const queryForm = reactive({
	status: ORDER_STATUS.DELIVERING,
});

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

const getStatusLabel = (status: string) => {
	const target = (takeaway_order_status.value || []).find((item: any) => String(item.value) === String(status));
	return target?.label || `未知状态(${status ?? '-'})`;
};

const getStatusTagType = (status: string) => {
	if (status === ORDER_STATUS.DELIVERING) return 'warning';
	if (status === ORDER_STATUS.FINISHED) return 'success';
	return 'info';
};

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

const onStatusChange = () => {
	pagination.current = 1;
	applyLocalFilter();
};

const resetQuery = () => {
	queryForm.status = ORDER_STATUS.DELIVERING;
	pagination.current = 1;
	applyLocalFilter();
};

const getCurrentRiderUserId = () => {
	return normalizeId(useUserInfo().userInfos?.user?.userId);
};

const ensureRiderContext = async () => {
	const riderRes = await currentRider();
	const rider: RiderInfo = riderRes?.data || {};
	if (rider.noExist || !rider.id) {
		throw new Error('当前用户暂无骑手扩展信息');
	}
	riderUserId.value = normalizeId(rider.userId) || getCurrentRiderUserId();
	riderReady.value = !!riderUserId.value;
	if (!riderReady.value) {
		throw new Error('未获取到当前骑手身份，无法查询配送单');
	}
};

const applyLocalFilter = () => {
	const targetStatus = queryForm.status;
	const deliveryRows = sourceRows.value.filter((row) => {
		if (row.orderStatus !== ORDER_STATUS.DELIVERING && row.orderStatus !== ORDER_STATUS.FINISHED) {
			return false;
		}
		if (!targetStatus) {
			return true;
		}
		return row.orderStatus === targetStatus;
	});
	allRows.value = deliveryRows;
	pagination.total = deliveryRows.length;
};

const loadData = async () => {
	loading.value = true;
	riderReady.value = false;
	sourceRows.value = [];
	allRows.value = [];
	pagination.current = 1;
	pagination.total = 0;
	try {
		await ensureRiderContext();
		const res = await pageOrderList({
			current: 1,
			size: 500,
			deliveryUserId: riderUserId.value,
		});
		const records: OrderRow[] = res?.data?.records || [];
		sourceRows.value = records.sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')));
		applyLocalFilter();
	} catch (error: any) {
		useMessage().warning(error?.message || error?.msg || '加载失败');
	} finally {
		loading.value = false;
	}
};

const handleFinish = async (row: OrderRow) => {
	const orderId = normalizeId(row?.id);
	if (!orderId) {
		useMessage().warning('订单ID不存在，无法完成配送');
		return;
	}
	if (row.orderStatus !== ORDER_STATUS.DELIVERING) {
		useMessage().warning('仅配送中订单可操作完成');
		return;
	}

	try {
		await useMessageBox().confirm(`确认将订单「${row.orderNo || row.id}」标记为配送完成吗？`);
	} catch {
		return;
	}

	finishingId.value = orderId;
	try {
		await deliveryFinishOrder(orderId);
		useMessage().success('配送完成');
		await loadData();
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '操作失败');
	} finally {
		finishingId.value = '';
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

.op-disabled {
	color: var(--el-text-color-disabled);
	font-size: 13px;
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
