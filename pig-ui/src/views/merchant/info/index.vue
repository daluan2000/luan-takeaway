<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card v-loading="loading">
				<template #header>
					<div class="card-header">
						<span>商家扩展信息</span>
					</div>
				</template>

				<el-alert
					v-if="isCreateMode"
					title="当前用户暂无商家扩展信息，请先新增"
					type="warning"
					show-icon
					:closable="false"
					class="mb20"
				/>

				<el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 700px">
					<el-form-item label="商家名称" prop="merchantName">
						<el-input v-model="form.merchantName" maxlength="128" placeholder="请输入商家名称" />
					</el-form-item>

					<el-form-item label="联系人" prop="contactName">
						<el-input v-model="form.contactName" maxlength="64" placeholder="请输入联系人" />
					</el-form-item>

					<el-form-item label="门店地址ID" prop="storeAddressId">
						<el-input v-model="form.storeAddressId" placeholder="请输入门店地址ID" />
					</el-form-item>

					<el-form-item label="营业状态" prop="businessStatus">
						<el-select v-model="form.businessStatus" placeholder="请选择营业状态" style="width: 100%">
							<el-option label="营业" value="1" />
							<el-option label="休息" value="0" />
						</el-select>
					</el-form-item>

					<el-form-item label="审核状态" v-if="!isCreateMode">
						<el-tag v-if="form.auditStatus === '1'" type="success">通过</el-tag>
						<el-tag v-else-if="form.auditStatus === '2'" type="danger">驳回</el-tag>
						<el-tag v-else type="warning">待审</el-tag>
					</el-form-item>

					<el-form-item>
						<el-button type="primary" :loading="submitting" @click="handleSubmit">
							{{ isCreateMode ? '新增' : '修改' }}
						</el-button>
						<el-button @click="loadCurrent">刷新</el-button>
					</el-form-item>
				</el-form>
			</el-card>
		</div>
	</div>
</template>

<script setup lang="ts" name="merchantInfo">
import { useMessage } from '/@/hooks/message';
import { applyMerchant, currentMerchant, updateMerchant } from '/@/api/takeaway/merchant';
import { useUserInfo } from '/@/stores/userInfo';

const formRef = ref();
const loading = ref(false);
const submitting = ref(false);
const isCreateMode = ref(true);

const form = reactive({
	id: undefined as number | undefined,
	userId: undefined as number | undefined,
	merchantName: '',
	contactName: '',
	storeAddressId: '',
	businessStatus: '1',
	auditStatus: '0',
});

const rules = reactive({
	merchantName: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
	contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
	storeAddressId: [{ required: true, message: '请输入门店地址ID', trigger: 'blur' }],
	businessStatus: [{ required: true, message: '请选择营业状态', trigger: 'change' }],
});

const resetForm = () => {
	form.id = undefined;
	form.merchantName = '';
	form.contactName = '';
	form.storeAddressId = '';
	form.businessStatus = '1';
	form.auditStatus = '0';
};

const loadCurrent = async () => {
	loading.value = true;
	try {
		const res = await currentMerchant();
		const data = res?.data || {};
		const noExist = !!data.noExist || !data.id;
		if (noExist) {
			isCreateMode.value = true;
			resetForm();
			return;
		}

		isCreateMode.value = false;
		form.id = data.id;
		form.userId = data.userId;
		form.merchantName = data.merchantName || '';
		form.contactName = data.contactName || '';
		form.storeAddressId = data.storeAddressId ? String(data.storeAddressId) : '';
		form.businessStatus = data.businessStatus || '1';
		form.auditStatus = data.auditStatus || '0';
	} finally {
		loading.value = false;
	}
};

const handleSubmit = async () => {
	await formRef.value.validate(async (valid: boolean) => {
		if (!valid) return;

		submitting.value = true;
		try {
			const currentUserId = useUserInfo().userInfos?.user?.userId;
			const payload: any = {
				merchantName: form.merchantName,
				contactName: form.contactName,
				storeAddressId: Number(form.storeAddressId),
				businessStatus: form.businessStatus,
			};

			if (isCreateMode.value) {
				payload.userId = currentUserId;
				await applyMerchant(payload);
				useMessage().success('新增成功');
			} else {
				payload.id = form.id;
				payload.userId = form.userId || currentUserId;
				await updateMerchant(payload);
				useMessage().success('修改成功');
			}

			await loadCurrent();
		} catch (error: any) {
			useMessage().error(error?.msg || error?.response?.data?.msg || '提交失败');
		} finally {
			submitting.value = false;
		}
	});
};

onMounted(() => {
	loadCurrent();
});
</script>

<style scoped>
.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}
</style>
