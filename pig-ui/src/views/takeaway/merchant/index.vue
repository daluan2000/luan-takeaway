<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row class="ml10" v-show="showSearch">
				<el-form :inline="true" :model="state.queryForm" ref="queryRef" @keyup.enter="getDataList">
					<el-form-item label="商家用户ID" prop="userId">
						<el-input v-model="state.queryForm.userId" placeholder="请输入商家用户ID" style="max-width: 180px" clearable />
					</el-form-item>
					<el-form-item label="审核状态" prop="auditStatus">
						<el-select v-model="state.queryForm.auditStatus" placeholder="请选择审核状态" style="max-width: 180px" clearable>
							<el-option label="待审核" value="0" />
							<el-option label="已通过" value="1" />
							<el-option label="已拒绝" value="2" />
						</el-select>
					</el-form-item>
					<el-form-item label="营业状态" prop="businessStatus">
						<el-select v-model="state.queryForm.businessStatus" placeholder="请选择营业状态" style="max-width: 180px" clearable>
							<el-option label="休息中" value="0" />
							<el-option label="营业中" value="1" />
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
					<el-button type="primary" class="ml10" icon="Plus" v-auth="'wm_merchant_apply'" @click="openApply">商家入驻</el-button>
					<el-button type="primary" class="ml10" icon="Finished" v-auth="'wm_merchant_audit'" @click="openAudit">商家审核</el-button>
					<el-button type="primary" class="ml10" icon="Select" v-auth="'wm_merchant_accept'" @click="openAccept">商家接单</el-button>
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
				<el-table-column label="ID" prop="id" width="190" show-overflow-tooltip />
				<el-table-column label="商家用户ID" prop="userId" width="140" />
				<el-table-column label="商家名称" prop="merchantName" min-width="160" show-overflow-tooltip />
				<el-table-column label="联系人" prop="contactName" width="120" show-overflow-tooltip />
				<el-table-column label="门店地址ID" prop="storeAddressId" width="150" />
				<el-table-column label="审核状态" prop="auditStatus" width="110">
					<template #default="scope">
						<el-tag v-if="scope.row.auditStatus === '0'" type="warning">待审核</el-tag>
						<el-tag v-else-if="scope.row.auditStatus === '1'" type="success">已通过</el-tag>
						<el-tag v-else type="danger">已拒绝</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="营业状态" prop="businessStatus" width="110">
					<template #default="scope">
						<el-tag v-if="scope.row.businessStatus === '1'" type="success">营业中</el-tag>
						<el-tag v-else type="info">休息中</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="创建时间" prop="createTime" min-width="170" show-overflow-tooltip />
				<el-table-column label="操作" width="280" fixed="right">
					<template #default="scope">
						<el-button text type="primary" icon="EditPen" v-auth="'wm_merchant_edit'" @click="openEdit(scope.row)">编辑</el-button>
						<el-button
							text
							type="primary"
							icon="SwitchButton"
							v-auth="'wm_merchant_business'"
							@click="onUpdateBusiness(scope.row, scope.row.businessStatus === '1' ? '0' : '1')"
						>
							{{ scope.row.businessStatus === '1' ? '打烊' : '营业' }}
						</el-button>
					</template>
				</el-table-column>
			</el-table>
			<pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" v-bind="state.pagination" />
		</div>

		<el-dialog v-model="merchantDialogVisible" :title="merchantForm.id ? '编辑商家' : '商家入驻'" width="620px" destroy-on-close>
			<el-form :model="merchantForm" :rules="merchantRules" ref="merchantFormRef" label-width="110px">
				<el-form-item label="商家用户ID" prop="userId">
					<el-input v-model="merchantForm.userId" placeholder="请输入商家用户ID" />
				</el-form-item>
				<el-form-item label="商家名称" prop="merchantName">
					<el-input v-model="merchantForm.merchantName" placeholder="请输入商家名称" />
				</el-form-item>
				<el-form-item label="联系人" prop="contactName">
					<el-input v-model="merchantForm.contactName" placeholder="请输入联系人" />
				</el-form-item>
				<el-form-item label="门店地址ID" prop="storeAddressId">
					<el-input v-model="merchantForm.storeAddressId" placeholder="请输入门店地址ID" />
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="merchantDialogVisible = false">取消</el-button>
				<el-button type="primary" @click="submitMerchant">保存</el-button>
			</template>
		</el-dialog>

		<el-dialog v-model="auditDialogVisible" title="商家审核" width="460px" destroy-on-close>
			<el-form :model="auditForm" :rules="auditRules" ref="auditFormRef" label-width="100px">
				<el-form-item label="商家ID" prop="id">
					<el-input v-model="auditForm.id" placeholder="请输入商家ID" />
				</el-form-item>
				<el-form-item label="审核状态" prop="auditStatus">
					<el-select v-model="auditForm.auditStatus" placeholder="请选择审核状态" style="width: 100%">
						<el-option label="已通过" value="1" />
						<el-option label="已拒绝" value="2" />
					</el-select>
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="auditDialogVisible = false">取消</el-button>
				<el-button type="primary" @click="submitAudit">确认</el-button>
			</template>
		</el-dialog>

		<el-dialog v-model="acceptDialogVisible" title="商家接单" width="420px" destroy-on-close>
			<el-form :model="acceptForm" :rules="acceptRules" ref="acceptFormRef" label-width="90px">
				<el-form-item label="订单ID" prop="orderId">
					<el-input v-model="acceptForm.orderId" placeholder="请输入订单ID" />
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="acceptDialogVisible = false">取消</el-button>
				<el-button type="primary" @click="submitAccept">确认接单</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup lang="ts" name="takeawayMerchantIndex">
import { BasicTableProps, useTable } from '/@/hooks/table';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { FormInstance, FormRules } from 'element-plus';
import { acceptMerchantOrder, applyMerchant, auditMerchant, pageList, updateBusinessStatus, updateMerchant } from '/@/api/takeaway/merchant';

const queryRef = ref();
const showSearch = ref(true);
const state: BasicTableProps = reactive<BasicTableProps>({
	queryForm: {},
	pageList,
	descs: ['create_time'],
});

const { getDataList, sizeChangeHandle, currentChangeHandle, tableStyle } = useTable(state);

const merchantDialogVisible = ref(false);
const auditDialogVisible = ref(false);
const acceptDialogVisible = ref(false);

const merchantFormRef = ref<FormInstance>();
const auditFormRef = ref<FormInstance>();
const acceptFormRef = ref<FormInstance>();

const merchantForm = reactive<any>({
	id: '',
	userId: '',
	merchantName: '',
	contactName: '',
	storeAddressId: '',
});

const auditForm = reactive<any>({
	id: '',
	auditStatus: '1',
});

const acceptForm = reactive<any>({
	orderId: '',
});

const merchantRules: FormRules = {
	userId: [{ required: true, message: '请输入商家用户ID', trigger: 'blur' }],
	merchantName: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
	contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
};

const auditRules: FormRules = {
	id: [{ required: true, message: '请输入商家ID', trigger: 'blur' }],
	auditStatus: [{ required: true, message: '请选择审核状态', trigger: 'change' }],
};

const acceptRules: FormRules = {
	orderId: [{ required: true, message: '请输入订单ID', trigger: 'blur' }],
};

const resetMerchantForm = () => {
	merchantForm.id = '';
	merchantForm.userId = '';
	merchantForm.merchantName = '';
	merchantForm.contactName = '';
	merchantForm.storeAddressId = '';
};

const resetQuery = () => {
	queryRef.value?.resetFields();
	getDataList();
};

const openApply = () => {
	resetMerchantForm();
	merchantDialogVisible.value = true;
};

const openEdit = (row: any) => {
	merchantForm.id = row.id;
	merchantForm.userId = row.userId;
	merchantForm.merchantName = row.merchantName;
	merchantForm.contactName = row.contactName;
	merchantForm.storeAddressId = row.storeAddressId;
	merchantDialogVisible.value = true;
};

const openAudit = () => {
	auditForm.id = '';
	auditForm.auditStatus = '1';
	auditDialogVisible.value = true;
};

const openAccept = () => {
	acceptForm.orderId = '';
	acceptDialogVisible.value = true;
};

const submitMerchant = async () => {
	await merchantFormRef.value?.validate();
	if (merchantForm.id) {
		await updateMerchant({ ...merchantForm });
		useMessage().success('商家信息已更新');
	} else {
		await applyMerchant({ ...merchantForm });
		useMessage().success('入驻申请已提交');
	}
	merchantDialogVisible.value = false;
	getDataList(false);
};

const submitAudit = async () => {
	await auditFormRef.value?.validate();
	await auditMerchant(auditForm.id, auditForm.auditStatus);
	useMessage().success('审核完成');
	auditDialogVisible.value = false;
	getDataList(false);
};

const submitAccept = async () => {
	await acceptFormRef.value?.validate();
	await acceptMerchantOrder(acceptForm.orderId);
	useMessage().success('接单成功');
	acceptDialogVisible.value = false;
	getDataList(false);
};

const onUpdateBusiness = async (row: any, status: string) => {
	try {
		await useMessageBox().confirm(`确认将商家【${row.merchantName || row.id}】状态改为${status === '1' ? '营业中' : '休息中'}吗？`);
	} catch {
		return;
	}
	await updateBusinessStatus(row.id, status);
	useMessage().success('营业状态已更新');
	getDataList(false);
};
</script>
