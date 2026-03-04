<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row class="ml10" v-show="showSearch">
				<el-form :inline="true" :model="state.queryForm" ref="queryRef" @keyup.enter="getDataList">
					<el-form-item label="订单ID" prop="orderId">
						<el-input v-model="state.queryForm.orderId" placeholder="请输入订单ID" style="max-width: 200px" clearable />
					</el-form-item>
					<el-form-item>
						<el-button type="primary" icon="Search" @click="getDataList">查询</el-button>
						<el-button icon="Refresh" @click="resetQuery">重置</el-button>
					</el-form-item>
				</el-form>
			</el-row>
			<el-row>
				<div class="mb8" style="width: 100%">
					<el-button type="primary" class="ml10" icon="Money" v-auth="'wm_pay_mock'" @click="openMockPay">模拟支付</el-button>
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
				<el-table-column label="支付ID" prop="id" width="190" show-overflow-tooltip />
				<el-table-column label="订单ID" prop="orderId" width="170" show-overflow-tooltip />
				<el-table-column label="订单号" prop="orderNo" width="170" show-overflow-tooltip />
				<el-table-column label="支付单号" prop="payNo" width="170" show-overflow-tooltip />
				<el-table-column label="支付金额" prop="payAmount" width="120" />
				<el-table-column label="支付状态" prop="payStatus" width="120">
					<template #default="scope">
						<el-tag v-if="scope.row.payStatus === '0'" type="warning">待支付</el-tag>
						<el-tag v-else-if="scope.row.payStatus === '1'" type="success">支付成功</el-tag>
						<el-tag v-else type="danger">支付失败</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="支付渠道" prop="payChannel" width="110">
					<template #default="scope">
						<span>{{ scope.row.payChannel === '0' ? 'MOCK' : scope.row.payChannel }}</span>
					</template>
				</el-table-column>
				<el-table-column label="支付时间" prop="payTime" min-width="170" show-overflow-tooltip />
				<el-table-column label="失败原因" prop="failReason" min-width="160" show-overflow-tooltip />
			</el-table>
			<pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" v-bind="state.pagination" />
		</div>

		<el-dialog v-model="mockPayDialogVisible" title="模拟支付" width="480px" destroy-on-close>
			<el-form :model="mockPayForm" :rules="mockPayRules" ref="mockPayFormRef" label-width="90px">
				<el-form-item label="订单ID" prop="orderId">
					<el-input v-model="mockPayForm.orderId" placeholder="请输入订单ID" />
				</el-form-item>
				<el-form-item label="支付渠道" prop="payChannel">
					<el-select v-model="mockPayForm.payChannel" style="width: 100%">
						<el-option label="MOCK" value="0" />
					</el-select>
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="mockPayDialogVisible = false">取消</el-button>
				<el-button type="primary" @click="submitMockPay">提交</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup lang="ts" name="takeawayPayIndex">
import { BasicTableProps, useTable } from '/@/hooks/table';
import { useMessage } from '/@/hooks/message';
import { FormInstance, FormRules } from 'element-plus';
import { mockPay, pageList } from '/@/api/takeaway/pay';

const queryRef = ref();
const showSearch = ref(true);
const mockPayDialogVisible = ref(false);
const mockPayFormRef = ref<FormInstance>();

const state: BasicTableProps = reactive<BasicTableProps>({
	queryForm: {},
	pageList,
	descs: ['create_time'],
});

const { getDataList, sizeChangeHandle, currentChangeHandle, tableStyle } = useTable(state);

const mockPayForm = reactive<any>({
	orderId: '',
	payChannel: '0',
});

const mockPayRules: FormRules = {
	orderId: [{ required: true, message: '请输入订单ID', trigger: 'blur' }],
	payChannel: [{ required: true, message: '请选择支付渠道', trigger: 'change' }],
};

const resetQuery = () => {
	queryRef.value?.resetFields();
	getDataList();
};

const openMockPay = () => {
	mockPayForm.orderId = '';
	mockPayForm.payChannel = '0';
	mockPayDialogVisible.value = true;
};

const submitMockPay = async () => {
	await mockPayFormRef.value?.validate();
	await mockPay({ ...mockPayForm });
	useMessage().success('模拟支付已触发');
	mockPayDialogVisible.value = false;
	getDataList(false);
};
</script>
