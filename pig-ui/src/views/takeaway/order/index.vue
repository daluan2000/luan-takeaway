<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row class="ml10" v-show="showSearch">
				<el-form :inline="true" :model="state.queryForm" ref="queryRef" @keyup.enter="getDataList">
					<el-form-item label="用户ID" prop="customerUserId">
						<el-input v-model="state.queryForm.customerUserId" placeholder="请输入下单用户ID" style="max-width: 180px" clearable />
					</el-form-item>
					<el-form-item label="商家ID" prop="merchantUserId">
						<el-input v-model="state.queryForm.merchantUserId" placeholder="请输入商家用户ID" style="max-width: 180px" clearable />
					</el-form-item>
					<el-form-item label="骑手ID" prop="deliveryUserId">
						<el-input v-model="state.queryForm.deliveryUserId" placeholder="请输入骑手用户ID" style="max-width: 180px" clearable />
					</el-form-item>
					<el-form-item label="订单状态" prop="status">
						<el-select v-model="state.queryForm.status" placeholder="请选择订单状态" style="max-width: 180px" clearable>
							<el-option label="待支付" value="0" />
							<el-option label="已支付" value="1" />
							<el-option label="商家已接单" value="2" />
							<el-option label="配送中" value="3" />
							<el-option label="已完成" value="4" />
							<el-option label="已取消" value="5" />
						</el-select>
					</el-form-item>
					<el-form-item>
						<el-button type="primary" icon="Search" @click="getDataList">查询</el-button>
						<el-button icon="Refresh" @click="resetQuery">重置</el-button>
					</el-form-item>
				</el-form>
			</el-row>
			<el-row>
				<div class="mb8" style="width: 100%">
					<el-button type="primary" class="ml10" icon="Plus" v-auth="'wm_order_create'" @click="openCreate">订单创建</el-button>
					<right-toolbar class="ml10" style="float: right; margin-right: 20px" v-model:showSearch="showSearch" @queryTable="getDataList"></right-toolbar>
				</div>
			</el-row>
			<el-table
				:data="state.dataList"
				v-loading="state.loading"
				border
				style="width: 100%"
				:cell-style="tableStyle.cellStyle"
				:header-cell-style="tableStyle.headerCellStyle"
			>
				<el-table-column label="订单ID" prop="id" width="190" show-overflow-tooltip />
				<el-table-column label="订单号" prop="orderNo" width="180" show-overflow-tooltip />
				<el-table-column label="用户ID" prop="customerUserId" width="120" />
				<el-table-column label="商家ID" prop="merchantUserId" width="120" />
				<el-table-column label="骑手ID" prop="deliveryUserId" width="120" />
				<el-table-column label="总金额" prop="totalAmount" width="100" />
				<el-table-column label="支付金额" prop="payAmount" width="100" />
				<el-table-column label="订单状态" prop="orderStatus" width="120">
					<template #default="scope">
						<el-tag v-if="scope.row.orderStatus === '0'" type="warning">待支付</el-tag>
						<el-tag v-else-if="scope.row.orderStatus === '1'" type="success">已支付</el-tag>
						<el-tag v-else-if="scope.row.orderStatus === '2'" type="success">商家已接单</el-tag>
						<el-tag v-else-if="scope.row.orderStatus === '3'" type="primary">配送中</el-tag>
						<el-tag v-else-if="scope.row.orderStatus === '4'" type="success">已完成</el-tag>
						<el-tag v-else type="info">已取消</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="备注" prop="remark" min-width="140" show-overflow-tooltip />
				<el-table-column label="创建时间" prop="createTime" min-width="170" show-overflow-tooltip />
				<el-table-column label="操作" width="220" fixed="right">
					<template #default="scope">
						<el-button text type="primary" icon="Document" v-auth="'wm_order_detail'" @click="onDetail(scope.row)">详情</el-button>
						<el-button text type="primary" icon="CircleClose" v-auth="'wm_order_cancel'" @click="onCancel(scope.row)">取消</el-button>
					</template>
				</el-table-column>
			</el-table>
			<pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" v-bind="state.pagination" />
		</div>

		<el-dialog v-model="createDialogVisible" title="创建订单" width="760px" destroy-on-close>
			<el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px">
				<el-form-item label="用户ID" prop="customerUserId">
					<el-input v-model="createForm.customerUserId" placeholder="请输入下单用户ID" />
				</el-form-item>
				<el-form-item label="商家用户ID" prop="merchantUserId">
					<el-input v-model="createForm.merchantUserId" placeholder="请输入商家用户ID" />
				</el-form-item>
				<el-form-item label="收货地址ID" prop="deliveryAddressId">
					<el-input v-model="createForm.deliveryAddressId" placeholder="请输入收货地址ID" />
				</el-form-item>
				<el-form-item label="备注" prop="remark">
					<el-input type="textarea" :rows="2" v-model="createForm.remark" placeholder="请输入备注" />
				</el-form-item>
				<el-form-item label="菜品明细">
					<div style="width: 100%">
						<el-row v-for="(item, index) in createForm.items" :key="index" :gutter="12" class="mb8">
							<el-col :span="10"><el-input v-model="item.dishId" placeholder="菜品ID" /></el-col>
							<el-col :span="10"><el-input v-model="item.quantity" placeholder="数量" /></el-col>
							<el-col :span="4">
								<el-button link type="danger" @click="removeItem(index)">删除</el-button>
							</el-col>
						</el-row>
						<el-button type="primary" link @click="addItem">+ 添加菜品</el-button>
					</div>
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="createDialogVisible = false">取消</el-button>
				<el-button type="primary" @click="submitCreate">提交订单</el-button>
			</template>
		</el-dialog>

		<el-dialog v-model="detailDialogVisible" title="订单详情" width="860px" destroy-on-close>
			<el-descriptions :column="2" border v-if="detailData.order">
				<el-descriptions-item label="订单ID">{{ detailData.order.id }}</el-descriptions-item>
				<el-descriptions-item label="订单号">{{ detailData.order.orderNo }}</el-descriptions-item>
				<el-descriptions-item label="用户ID">{{ detailData.order.customerUserId }}</el-descriptions-item>
				<el-descriptions-item label="商家ID">{{ detailData.order.merchantUserId }}</el-descriptions-item>
				<el-descriptions-item label="骑手ID">{{ detailData.order.deliveryUserId }}</el-descriptions-item>
				<el-descriptions-item label="订单状态">{{ detailData.order.orderStatus }}</el-descriptions-item>
				<el-descriptions-item label="总金额">{{ detailData.order.totalAmount }}</el-descriptions-item>
				<el-descriptions-item label="支付金额">{{ detailData.order.payAmount }}</el-descriptions-item>
				<el-descriptions-item label="备注" :span="2">{{ detailData.order.remark || '-' }}</el-descriptions-item>
			</el-descriptions>
			<el-divider>订单明细</el-divider>
			<el-table :data="detailData.items || []" border style="width: 100%">
				<el-table-column label="菜品ID" prop="dishId" width="140" />
				<el-table-column label="菜品名称" prop="dishName" min-width="150" />
				<el-table-column label="单价" prop="dishPrice" width="120" />
				<el-table-column label="数量" prop="quantity" width="100" />
				<el-table-column label="金额" prop="itemAmount" width="140" />
			</el-table>
		</el-dialog>
	</div>
</template>

<script setup lang="ts" name="takeawayOrderIndex">
import { BasicTableProps, useTable } from '/@/hooks/table';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { FormInstance, FormRules } from 'element-plus';
import { cancelOrder, createOrder, getOrderDetail, pageList } from '/@/api/takeaway/order';

const queryRef = ref();
const showSearch = ref(true);

const createDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const createFormRef = ref<FormInstance>();

const state: BasicTableProps = reactive<BasicTableProps>({
	queryForm: {},
	pageList,
	descs: ['create_time'],
});

const { getDataList, sizeChangeHandle, currentChangeHandle, tableStyle } = useTable(state);

const createForm = reactive<any>({
	customerUserId: '',
	merchantUserId: '',
	deliveryAddressId: '',
	remark: '',
	items: [{ dishId: '', quantity: '' }],
});

const detailData = reactive<any>({
	order: null,
	items: [],
});

const createRules: FormRules = {
	customerUserId: [{ required: true, message: '请输入下单用户ID', trigger: 'blur' }],
	merchantUserId: [{ required: true, message: '请输入商家用户ID', trigger: 'blur' }],
	deliveryAddressId: [{ required: true, message: '请输入收货地址ID', trigger: 'blur' }],
};

const resetQuery = () => {
	queryRef.value?.resetFields();
	getDataList();
};

const resetCreateForm = () => {
	createForm.customerUserId = '';
	createForm.merchantUserId = '';
	createForm.deliveryAddressId = '';
	createForm.remark = '';
	createForm.items = [{ dishId: '', quantity: '' }];
};

const openCreate = () => {
	resetCreateForm();
	createDialogVisible.value = true;
};

const addItem = () => {
	createForm.items.push({ dishId: '', quantity: '' });
};

const removeItem = (index: number) => {
	if (createForm.items.length <= 1) {
		useMessage().warning('至少保留一条菜品明细');
		return;
	}
	createForm.items.splice(index, 1);
};

const submitCreate = async () => {
	await createFormRef.value?.validate();
	if (!createForm.items.length || createForm.items.some((item: any) => !item.dishId || !item.quantity)) {
		useMessage().warning('请完整填写菜品明细');
		return;
	}
	await createOrder({
		customerUserId: createForm.customerUserId,
		merchantUserId: createForm.merchantUserId,
		deliveryAddressId: createForm.deliveryAddressId,
		remark: createForm.remark,
		items: createForm.items,
	});
	useMessage().success('订单创建成功');
	createDialogVisible.value = false;
	getDataList(false);
};

const onDetail = async (row: any) => {
	const res = await getOrderDetail(row.id);
	detailData.order = res.data.order;
	detailData.items = res.data.items || [];
	detailDialogVisible.value = true;
};

const onCancel = async (row: any) => {
	try {
		await useMessageBox().confirm(`确认取消订单【${row.orderNo || row.id}】吗？`);
	} catch {
		return;
	}
	await cancelOrder(row.id);
	useMessage().success('订单已取消');
	getDataList(false);
};
</script>
