<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card>
				<template #header>
					<div class="card-header">
						<span>我的订单</span>
						<el-button link type="primary" @click="refreshList">刷新</el-button>
					</div>
				</template>

				<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList">
					<el-form-item label="订单状态" prop="status">
						<el-select v-model="state.queryForm.status" placeholder="请选择状态" clearable style="width: 220px">
							<el-option v-for="item in ORDER_STATUS_OPTIONS" :key="item.value || 'all'" :label="item.label" :value="item.value" />
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
					<el-table-column label="商家名称" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.merchantName" show-overflow-tooltip>
						<template #default="scope">
							{{ scope.row.merchantName || '-' }}
						</template>
					</el-table-column>
					<el-table-column label="骑手名称" :min-width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.deliveryRiderName" show-overflow-tooltip>
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
					<el-table-column label="操作" :width="TAKEAWAY_ORDER_TABLE_COL_WIDTH.customerActions" fixed="right">
						<template #default="scope">
							<template v-if="scope.row.orderStatus === ORDER_STATUS.WAIT_PAY">
								<el-button text type="primary" :loading="payingId === String(scope.row.id)" @click="handlePay(scope.row)">
									去支付
								</el-button>
								<el-button text type="danger" :loading="cancellingId === String(scope.row.id)" @click="handleCancel(scope.row)">
									取消订单
								</el-button>
							</template>
							<span v-else class="op-disabled">暂无操作</span>
						</template>
					</el-table-column>
				</el-table>

				<pagination @current-change="currentChangeHandle" @size-change="sizeChangeHandle" v-bind="state.pagination" />
			</el-card>
		</div>
	</div>
</template>

<script setup lang="ts" name="customerOrderIndex">
import { useTable, type BasicTableProps } from '/@/hooks/table';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { currentCustomer } from '/@/api/takeaway/customer';
import { cancelOrder, pageList } from '/@/api/takeaway/order';
import { mockPay } from '/@/api/takeaway/pay';
import { useUserInfo } from '/@/stores/userInfo';
import { TAKEAWAY_ORDER_TABLE_COL_WIDTH } from '/@/constants/takeawayOrderTable';
import { getOrderTimelineText } from '/@/utils/takeawayOrderTime';

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
	{ label: '待支付', value: ORDER_STATUS.WAIT_PAY },
	{ label: '已支付', value: ORDER_STATUS.PAID },
	{ label: '商家已接单', value: ORDER_STATUS.MERCHANT_ACCEPTED },
	{ label: '配送中', value: ORDER_STATUS.DELIVERING },
	{ label: '已完成', value: ORDER_STATUS.FINISHED },
	{ label: '已取消', value: ORDER_STATUS.CANCELED },
];

const statusLabelMap: Record<string, string> = {
	[ORDER_STATUS.WAIT_PAY]: '待支付',
	[ORDER_STATUS.PAID]: '已支付',
	[ORDER_STATUS.MERCHANT_ACCEPTED]: '商家已接单',
	[ORDER_STATUS.DELIVERING]: '配送中',
	[ORDER_STATUS.FINISHED]: '已完成',
	[ORDER_STATUS.CANCELED]: '已取消',
};

const queryRef = ref();
const cancellingId = ref('');
const payingId = ref('');
const expandedTimeMap = reactive<Record<string, boolean>>({});

const state: BasicTableProps = reactive<BasicTableProps>({
	createdIsNeed: false,
	queryForm: {
		customerUserId: undefined as number | undefined,
		status: '',
	},
	pageList,
});

const { getDataList, currentChangeHandle, sizeChangeHandle, tableStyle } = useTable(state);

const resolveCurrentCustomerUserId = async () => {
	try {
		const res = await currentCustomer();
		const customerUserId = Number(res?.data?.userId);
		if (!Number.isNaN(customerUserId) && customerUserId > 0) {
			return customerUserId;
		}
	} catch {
		// Ignore and fallback to current login info.
	}

	const fallbackUserId = Number(useUserInfo().userInfos?.user?.userId);
	if (!Number.isNaN(fallbackUserId) && fallbackUserId > 0) {
		return fallbackUserId;
	}
	return undefined;
};

const formatMoney = (value: unknown) => {
	const amount = Number(value ?? 0);
	if (Number.isNaN(amount)) {
		return '0.00';
	}
	return amount.toFixed(2);
};

const getStatusLabel = (status: string) => {
	return statusLabelMap[status] || `未知状态(${status ?? '-'})`;
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

const handleCancel = async (row: any) => {
	if (!row?.id) {
		useMessage().warning('订单ID不存在，无法取消');
		return;
	}
	if (row.orderStatus !== ORDER_STATUS.WAIT_PAY) {
		useMessage().warning('仅待支付订单可取消');
		return;
	}

	try {
		await useMessageBox().confirm(`确认取消订单「${row.orderNo || row.id}」吗？`);
	} catch {
		return;
	}

	cancellingId.value = String(row.id);
	try {
		await cancelOrder(row.id);
		useMessage().success('订单已取消');
		getDataList(false);
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '取消订单失败');
	} finally {
		cancellingId.value = '';
	}
};

const handlePay = async (row: any) => {
	if (!row?.id) {
		useMessage().warning('订单ID不存在，无法支付');
		return;
	}
	if (row.orderStatus !== ORDER_STATUS.WAIT_PAY) {
		useMessage().warning('仅待支付订单可支付');
		return;
	}

	try {
		await useMessageBox().confirm(`确认支付订单「${row.orderNo || row.id}」吗？`);
	} catch {
		return;
	}

	payingId.value = String(row.id);
	try {
		await mockPay({
			orderId: row.id,
			payChannel: '0',
		});
		useMessage().success('支付成功');
		getDataList(false);
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '支付失败');
	} finally {
		payingId.value = '';
	}
};

onMounted(async () => {
	state.queryForm.customerUserId = await resolveCurrentCustomerUserId();
	if (!state.queryForm.customerUserId) {
		useMessage().warning('未获取到当前客户身份，无法查询订单');
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
