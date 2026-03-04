<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row class="ml10" v-show="showSearch">
				<el-form :inline="true" :model="state.queryForm" ref="queryRef" @keyup.enter="getDataList">
					<el-form-item label="骑手ID" prop="deliveryUserId">
						<el-input v-model="state.queryForm.deliveryUserId" placeholder="请输入骑手用户ID" style="max-width: 180px" clearable />
					</el-form-item>
					<el-form-item label="配送状态" prop="status">
						<el-select v-model="state.queryForm.status" placeholder="请选择配送状态" style="max-width: 180px" clearable>
							<el-option label="待接单" value="0" />
							<el-option label="已接单" value="1" />
							<el-option label="配送中" value="2" />
							<el-option label="已送达" value="3" />
							<el-option label="已取消" value="4" />
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
					<el-button type="primary" class="ml10" icon="User" v-auth="'wm_delivery_rider'" @click="openRider">骑手信息</el-button>
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
				<el-table-column label="配送单ID" prop="id" width="190" show-overflow-tooltip />
				<el-table-column label="订单ID" prop="orderId" width="170" show-overflow-tooltip />
				<el-table-column label="订单号" prop="orderNo" width="170" show-overflow-tooltip />
				<el-table-column label="商家ID" prop="merchantUserId" width="120" />
				<el-table-column label="骑手ID" prop="deliveryUserId" width="120" />
				<el-table-column label="配送状态" prop="deliveryStatus" width="120">
					<template #default="scope">
						<el-tag v-if="scope.row.deliveryStatus === '0'" type="warning">待接单</el-tag>
						<el-tag v-else-if="scope.row.deliveryStatus === '1'" type="success">已接单</el-tag>
						<el-tag v-else-if="scope.row.deliveryStatus === '2'" type="primary">配送中</el-tag>
						<el-tag v-else-if="scope.row.deliveryStatus === '3'" type="success">已送达</el-tag>
						<el-tag v-else type="info">已取消</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="接单时间" prop="acceptTime" min-width="170" show-overflow-tooltip />
				<el-table-column label="送达时间" prop="deliveredTime" min-width="170" show-overflow-tooltip />
				<el-table-column label="操作" width="220" fixed="right">
					<template #default="scope">
						<el-button text type="primary" icon="Select" v-auth="'wm_delivery_accept'" @click="onAccept(scope.row)">骑手接单</el-button>
						<el-button text type="primary" icon="SuccessFilled" v-auth="'wm_delivery_complete'" @click="onComplete(scope.row)">配送完成</el-button>
					</template>
				</el-table-column>
			</el-table>
			<pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" v-bind="state.pagination" />
		</div>

		<el-dialog v-model="riderDialogVisible" title="骑手注册/更新" width="620px" destroy-on-close>
			<el-form :model="riderForm" :rules="riderRules" ref="riderFormRef" label-width="120px">
				<el-form-item label="骑手用户ID" prop="userId">
					<el-input v-model="riderForm.userId" placeholder="请输入骑手用户ID" />
				</el-form-item>
				<el-form-item label="真实姓名" prop="realName">
					<el-input v-model="riderForm.realName" placeholder="请输入真实姓名" />
				</el-form-item>
				<el-form-item label="配送范围(km)" prop="deliveryScopeKm">
					<el-input v-model="riderForm.deliveryScopeKm" placeholder="请输入配送范围(km)" />
				</el-form-item>
				<el-form-item label="在线状态" prop="onlineStatus">
					<el-select v-model="riderForm.onlineStatus" style="width: 100%">
						<el-option label="离线" value="0" />
						<el-option label="在线" value="1" />
					</el-select>
				</el-form-item>
				<el-form-item label="在职状态" prop="employmentStatus">
					<el-select v-model="riderForm.employmentStatus" style="width: 100%">
						<el-option label="离职" value="0" />
						<el-option label="在职" value="1" />
					</el-select>
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="riderDialogVisible = false">取消</el-button>
				<el-button type="primary" @click="submitRider">保存</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup lang="ts" name="takeawayDeliveryIndex">
import { BasicTableProps, useTable } from '/@/hooks/table';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { FormInstance, FormRules } from 'element-plus';
import { acceptOrder, completeOrder, pageList, saveRider } from '/@/api/takeaway/delivery';

const queryRef = ref();
const showSearch = ref(true);
const riderDialogVisible = ref(false);
const riderFormRef = ref<FormInstance>();

const state: BasicTableProps = reactive<BasicTableProps>({
	queryForm: {},
	pageList,
	descs: ['create_time'],
});

const { getDataList, sizeChangeHandle, currentChangeHandle, tableStyle } = useTable(state);

const riderForm = reactive<any>({
	userId: '',
	realName: '',
	deliveryScopeKm: '',
	onlineStatus: '1',
	employmentStatus: '1',
});

const riderRules: FormRules = {
	userId: [{ required: true, message: '请输入骑手用户ID', trigger: 'blur' }],
	realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
	deliveryScopeKm: [{ required: true, message: '请输入配送范围', trigger: 'blur' }],
};

const resetQuery = () => {
	queryRef.value?.resetFields();
	getDataList();
};

const openRider = () => {
	riderForm.userId = '';
	riderForm.realName = '';
	riderForm.deliveryScopeKm = '';
	riderForm.onlineStatus = '1';
	riderForm.employmentStatus = '1';
	riderDialogVisible.value = true;
};

const submitRider = async () => {
	await riderFormRef.value?.validate();
	await saveRider({ ...riderForm });
	useMessage().success('骑手信息已保存');
	riderDialogVisible.value = false;
};

const askDeliveryUserId = async (row: any) => {
	try {
		const res = await useMessageBox().prompt(`请输入订单【${row.orderNo || row.orderId}】的骑手用户ID`);
		return res.value;
	} catch {
		return '';
	}
};

const onAccept = async (row: any) => {
	const deliveryUserId = await askDeliveryUserId(row);
	if (!deliveryUserId) {
		return;
	}
	await acceptOrder(row.orderId, deliveryUserId);
	useMessage().success('骑手接单成功');
	getDataList(false);
};

const onComplete = async (row: any) => {
	const deliveryUserId = await askDeliveryUserId(row);
	if (!deliveryUserId) {
		return;
	}
	await completeOrder(row.orderId, deliveryUserId);
	useMessage().success('配送完成');
	getDataList(false);
};
</script>
