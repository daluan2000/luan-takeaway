<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card>
				<template #header>
					<div class="card-header">
						<span>商家订单</span>
						<el-button link type="primary" @click="refreshList">刷新</el-button>
					</div>
				</template>

				<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList">
					<el-form-item label="订单状态" prop="status">
						<el-select v-model="state.queryForm.status" placeholder="请选择状态" clearable style="width: 220px">
							<el-option v-for="item in orderStatusOptions" :key="item.value || 'all'" :label="item.label" :value="item.value" />
						</el-select>
					</el-form-item>
					<el-form-item>
						<el-button type="primary" icon="Search" @click="getDataList">查询</el-button>
						<el-button icon="Refresh" @click="resetQuery">重置</el-button>
					</el-form-item>
				</el-form>

				<el-table
					:data="state.dataList"
					v-loading="state.loading"
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
					<el-table-column label="骑手" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.deliveryRiderName" show-overflow-tooltip>
						<template #default="scope">
							{{ scope.row.deliveryRiderName || '-' }}
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
					<el-table-column label="操作" :width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.merchantActions" fixed="right">
						<template #default="scope">
							<el-button
								v-auth="'wm_merchant_order_accept'"
								v-if="scope.row.orderStatus === ORDER_STATUS.PAID"
								text
								type="primary"
								:loading="acceptingId === String(scope.row.id)"
								@click="handleAccept(scope.row)"
							>
								接单
							</el-button>
							<span v-else class="op-disabled">不可接单</span>
						</template>
					</el-table-column>
				</el-table>

				<pagination @current-change="currentChangeHandle" @size-change="sizeChangeHandle" v-bind="state.pagination" />
			</el-card>
		</div>
	</div>
</template>

<script setup lang="ts" name="merchantOrderIndex">
import { useTable, type BasicTableProps } from '/@/hooks/table';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { merchantAcceptOrder, pageList } from '/@/api/takeaway/order';
import { useUserInfo } from '/@/stores/userInfo';
import { TAKEAWAY_ORDER_TABLE_COL_WIDTH } from '/@/constants/takeawayOrderTable';
import { getOrderTimelineText } from '/@/utils/takeawayOrderTime';
import { useDict } from '/@/hooks/dict';

const ORDER_STATUS = {
	WAIT_PAY: '0',
	PAID: '1',
	MERCHANT_ACCEPTED: '2',
	DELIVERING: '3',
	FINISHED: '4',
	CANCELED: '5',
} as const;

const ORDER_STATUS_OPTIONS = [
	{ label: '全部', value: '' },
];

const { takeaway_order_status } = useDict('takeaway_order_status');

const orderStatusOptions = computed(() => {
	const dictOptions = (takeaway_order_status.value || []).map((item: any) => ({
		label: item.label,
		value: String(item.value),
	}));
	return [...ORDER_STATUS_OPTIONS, ...dictOptions];
});

const queryRef = ref();
const acceptingId = ref('');
const expandedTimeMap = reactive<Record<string, boolean>>({});

const normalizeId = (value: unknown): string | undefined => {
	if (value === null || value === undefined || value === '') {
		return undefined;
	}
	const text = String(value).trim();
	return text ? text : undefined;
};

const state: BasicTableProps = reactive<BasicTableProps>({
	createdIsNeed: false,
	queryForm: {
		merchantUserId: undefined as string | number | undefined,
		status: '',
	},
	pageList,
});

const { getDataList, currentChangeHandle, sizeChangeHandle, tableStyle } = useTable(state);

const resolveCurrentMerchantUserId = () => {
	return normalizeId(useUserInfo().userInfos?.user?.userId);
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
	if (status === ORDER_STATUS.WAIT_PAY) return 'warning';
	if (status === ORDER_STATUS.PAID) return 'primary';
	if (status === ORDER_STATUS.MERCHANT_ACCEPTED) return 'info';
	if (status === ORDER_STATUS.DELIVERING) return '';
	if (status === ORDER_STATUS.FINISHED) return 'success';
	if (status === ORDER_STATUS.CANCELED) return 'danger';
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

const getRowKey = (row: Record<string, unknown>) => {
	const id = row?.id;
	const orderNo = row?.orderNo;
	if (id !== null && id !== undefined && String(id).trim()) {
		return String(id);
	}
	if (orderNo !== null && orderNo !== undefined && String(orderNo).trim()) {
		return String(orderNo);
	}
	return '';
};

const getOrderTimeText = (row: Record<string, unknown>) => getOrderTimelineText(row);

const isTimeExpanded = (row: Record<string, unknown>) => {
	const key = getRowKey(row);
	return key ? !!expandedTimeMap[key] : false;
};

const toggleTimeExpanded = (row: Record<string, unknown>) => {
	const key = getRowKey(row);
	if (!key) return;
	expandedTimeMap[key] = !expandedTimeMap[key];
};

const resetQuery = () => {
	queryRef.value?.resetFields();
	state.queryForm.status = '';
	getDataList();
};

const refreshList = () => {
	getDataList(false);
};

const handleAccept = async (row: any) => {
	if (!row?.id) {
		useMessage().warning('订单ID不存在，无法接单');
		return;
	}
	if (row.orderStatus !== ORDER_STATUS.PAID) {
		useMessage().warning('仅已支付订单可接单');
		return;
	}

	try {
		await useMessageBox().confirm(`确认接单「${row.orderNo || row.id}」吗？`);
	} catch {
		return;
	}

	acceptingId.value = String(row.id);
	try {
		await merchantAcceptOrder(row.id);
		useMessage().success('接单成功');
		getDataList(false);
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '接单失败');
	} finally {
		acceptingId.value = '';
	}
};

onMounted(() => {
	state.queryForm.merchantUserId = resolveCurrentMerchantUserId();
	if (!state.queryForm.merchantUserId) {
		useMessage().warning('未获取到当前商家身份，无法查询订单');
		return;
	}
	getDataList();
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
