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
							<el-tag :type="getStatusTagType(scope.row)">
								{{ getStatusLabel(scope.row) }}
							</el-tag>
							<div v-if="showCountdown(scope.row)" class="countdown-text">剩余：{{ getCountdownText(scope.row) }}</div>
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
							<template v-if="canPayOrder(scope.row)">
								<el-button v-auth="'wm_customer_order_pay'" text type="primary" :loading="payingId === String(scope.row.id)" @click="handlePay(scope.row)">
									去支付
								</el-button>
								<el-button v-auth="'wm_customer_order_cancel'" text type="danger" :loading="cancellingId === String(scope.row.id)" @click="handleCancel(scope.row)">
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
import { useDict } from '/@/hooks/dict';

// 默认兜底值：当历史数据未返回截止时间时，临时按 10 分钟展示。
const DEFAULT_AUTO_CANCEL_MS = 10 * 60 * 1000;

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

const { takeaway_order_status, takeaway_pay_channel } = useDict('takeaway_order_status', 'takeaway_pay_channel');

const orderStatusOptions = computed(() => {
	const dictOptions = (takeaway_order_status.value || []).map((item: any) => ({
		label: item.label,
		value: String(item.value),
	}));
	return [...ORDER_STATUS_OPTIONS, ...dictOptions];
});

const defaultPayChannel = computed(() => {
	const options = takeaway_pay_channel.value || [];
	if (!options.length) {
		return '0';
	}
	const mockChannel = options.find((item: any) => item.label === '模拟支付' || String(item.value) === '0');
	return String((mockChannel || options[0]).value);
});

const queryRef = ref();
const cancellingId = ref('');
const payingId = ref('');
const expandedTimeMap = reactive<Record<string, boolean>>({});
// “当前时间”使用响应式变量承载，便于每秒触发一次倒计时重算和界面刷新。
const nowTs = ref(Date.now());
let countdownTimer: ReturnType<typeof setInterval> | undefined;

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


const parseTimeToTimestamp = (value: unknown) => {
	// 后端时间可能是 "yyyy-MM-dd HH:mm:ss" 或 ISO 字符串，这里统一做兼容解析。
	if (value === null || value === undefined || value === '') {
		return undefined;
	}
	const rawText = String(value).trim();
	if (!rawText) {
		return undefined;
	}
	const normalized = rawText.includes('T') ? rawText : rawText.replace(' ', 'T');
	const ts = new Date(normalized).getTime();
	if (!Number.isNaN(ts)) {
		return ts;
	}
	const fallbackTs = new Date(rawText).getTime();
	return Number.isNaN(fallbackTs) ? undefined : fallbackTs;
};

const getExpireTimestamp = (row: Record<string, unknown>) => {
	// 优先使用后端返回的绝对截止时间戳，保证刷新页面后倒计时依旧连续准确。
	const deadlineTs = Number(row?.autoCancelDeadlineTs);
	if (!Number.isNaN(deadlineTs) && deadlineTs > 0) {
		return deadlineTs;
	}

	// 兼容兜底：若缺少截止时间字段，则按“下单时间 + 默认时长”估算。
	const createTs = parseTimeToTimestamp(row?.createTime);
	if (!createTs) {
		return undefined;
	}
	return createTs + DEFAULT_AUTO_CANCEL_MS;
};

const getRemainingMs = (row: Record<string, unknown>) => {
	// 负数代表已超时。
	const expireTs = getExpireTimestamp(row);
	if (!expireTs) {
		return undefined;
	}
	return expireTs - nowTs.value;
};

const isWaitPayExpired = (row: Record<string, unknown>) => {
	// 仅“待支付”订单参与超时判断，其他状态不做本地覆盖。
	if (row?.orderStatus !== ORDER_STATUS.WAIT_PAY) {
		return false;
	}
	const remaining = getRemainingMs(row);
	if (remaining === undefined) {
		return false;
	}
	return remaining <= 0;
};

const getDisplayStatus = (row: Record<string, unknown>) => {
	// 前端展示层做一层兜底：倒计时到期后，即使列表还没刷新，也先显示为“已取消”。
	// 这只是展示优化，最终状态仍以后端数据为准。
	if (isWaitPayExpired(row)) {
		return ORDER_STATUS.CANCELED;
	}
	return String(row?.orderStatus ?? '');
};

const getStatusLabel = (row: Record<string, unknown>) => {
	const status = getDisplayStatus(row);
	const target = (takeaway_order_status.value || []).find((item: any) => String(item.value) === status);
	return target?.label || `未知状态(${status ?? '-'})`;
};


const getStatusTagType = (row: Record<string, unknown>) => {
	const status = getDisplayStatus(row);
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

const formatDuration = (ms: number) => {
	// 将毫秒格式化为 mm:ss，便于用户直观看到剩余支付时间。
	const safeMs = Math.max(ms, 0);
	const totalSeconds = Math.floor(safeMs / 1000);
	const minutes = Math.floor(totalSeconds / 60);
	const seconds = totalSeconds % 60;
	return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
};

const getCountdownText = (row: Record<string, unknown>) => {
	// 已超时时固定显示 00:00，避免出现负值或闪烁。
	if (isWaitPayExpired(row)) {
		return '00:00';
	}
	const remaining = getRemainingMs(row);
	if (remaining === undefined) {
		return '-';
	}
	return formatDuration(remaining);
};

const showCountdown = (row: Record<string, unknown>) => {
	// 仅待支付状态展示倒计时文案。
	return getDisplayStatus(row) === ORDER_STATUS.WAIT_PAY;
};

const canPayOrder = (row: Record<string, unknown>) => {
	// 支付和手动取消共用同一判定：只要前端已判断超时，就不再允许用户操作。
	return getDisplayStatus(row) === ORDER_STATUS.WAIT_PAY;
};

const getOrderTimeText = (row: Record<string, unknown>) => {
	const baseText = getOrderTimelineText(row);
	if (!showCountdown(row)) {
		return baseText;
	}
	return `${baseText}\n支付倒计时：${getCountdownText(row)}`;
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
	if (!canPayOrder(row)) {
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
	if (!canPayOrder(row)) {
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
			payChannel: defaultPayChannel.value,
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
	// 每秒推进一次“当前时间”，驱动倒计时刷新。
	countdownTimer = setInterval(() => {
		nowTs.value = Date.now();
	}, 1000);

	state.queryForm.customerUserId = await resolveCurrentCustomerUserId();
	if (!state.queryForm.customerUserId) {
		useMessage().warning('未获取到当前客户身份，无法查询订单');
		return;
	}
	getDataList();
});

onUnmounted(() => {
	// 组件销毁时释放定时器，避免内存泄漏和无效计算。
	if (countdownTimer) {
		clearInterval(countdownTimer);
	}
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

.countdown-text {
	margin-top: 4px;
	font-size: 12px;
	color: var(--el-color-warning);
}
</style>
