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

				<el-alert
					v-if="!isCreateMode && form.auditStatus === '0'"
					title="当前为待审状态，系统将于3-10秒内自动审批，请稍候或点击刷新"
					type="info"
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
						<el-select v-model="form.storeAddressId" placeholder="请选择门店地址" filterable clearable style="width: 100%">
							<el-option v-for="item in addressOptions" :key="item.value" :label="item.label" :value="item.value" />
						</el-select>
						<div v-if="!addressOptions.length" class="address-empty-tip">
							<el-alert title="暂无可用地址，请先新增地址" type="warning" :closable="false" show-icon />
							<el-button type="primary" link @click="goAddressPage">去新增地址</el-button>
						</div>
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
import { listAddress } from '/@/api/takeaway/address';
import { applyMerchant, currentMerchant, updateMerchant } from '/@/api/takeaway/merchant';
import { useUserInfo } from '/@/stores/userInfo';
import { useRouter } from 'vue-router';

const formRef = ref();
const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const isCreateMode = ref(true);
const addressOptions = ref<Array<{ label: string; value: string }>>([]);
const autoAuditPolling = ref(false);
let pollTimer: ReturnType<typeof setInterval> | null = null;
let pollTimeout: ReturnType<typeof setTimeout> | null = null;

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
	storeAddressId: [{ required: true, message: '请选择门店地址', trigger: 'change' }],
	businessStatus: [{ required: true, message: '请选择营业状态', trigger: 'change' }],
});

const validateAddressField = async () => {
	await nextTick();
	formRef.value?.validateField('storeAddressId');
};

const buildAddressOptionLabel = (item: any) => {
	const addressText = [item?.province, item?.city, item?.district, item?.detailAddress].filter(Boolean).join(' ');
	if (addressText) {
		return `${addressText}（ID:${item.id}）`;
	}
	return `地址ID: ${item.id}`;
};

const loadAddressOptions = async () => {
	try {
		const res = await listAddress();
		const list = res?.data || [];
		addressOptions.value = list
			.filter((item: any) => item?.id !== undefined && item?.id !== null)
			.map((item: any) => ({
				label: buildAddressOptionLabel(item),
				value: String(item.id),
			}));
	} catch {
		addressOptions.value = [];
		useMessage().warning('地址列表加载失败，请稍后重试');
	}
};

const goAddressPage = async () => {
	const candidates = ['/address/index'];
	for (const path of candidates) {
		try {
			await router.push(path);
			return;
		} catch {
			// 尝试下一个候选路由
		}
	}
	useMessage().warning('未找到地址管理页面，请联系管理员配置菜单路由');
};

const resetForm = () => {
	form.id = undefined;
	form.userId = undefined;
	form.merchantName = '';
	form.contactName = '';
	form.storeAddressId = '';
	form.businessStatus = '1';
	form.auditStatus = '0';
};

const loadCurrent = async (showLoading = true) => {
	if (showLoading) {
		loading.value = true;
	}
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
		if (showLoading) {
			loading.value = false;
			await validateAddressField();
		}
	}
};

const stopAutoAuditPolling = () => {
	if (pollTimer) {
		clearInterval(pollTimer);
		pollTimer = null;
	}
	if (pollTimeout) {
		clearTimeout(pollTimeout);
		pollTimeout = null;
	}
	autoAuditPolling.value = false;
};

const startAutoAuditPolling = () => {
	stopAutoAuditPolling();
	autoAuditPolling.value = true;

	pollTimer = setInterval(async () => {
		await loadCurrent(false);
		if (form.auditStatus !== '0') {
			stopAutoAuditPolling();
			if (form.auditStatus === '1') {
				useMessage().success('系统已自动审批通过');
			}
		}
	}, 1500);

	pollTimeout = setTimeout(() => {
		stopAutoAuditPolling();
	}, 20000);
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
				storeAddressId: form.storeAddressId,
				businessStatus: form.businessStatus,
			};

			if (isCreateMode.value) {
				payload.userId = currentUserId;
				await applyMerchant(payload);
				useMessage().success('已提交入驻申请，状态已置为待审，系统将自动审批');
			} else {
				payload.id = form.id;
				payload.userId = form.userId || currentUserId;
				await updateMerchant(payload);
				useMessage().success('已提交更新，状态已重置为待审，系统将自动审批');
			}

			await loadCurrent();
			if (!isCreateMode.value && form.auditStatus === '0') {
				startAutoAuditPolling();
			}
		} catch (error: any) {
			useMessage().error(error?.msg || error?.response?.data?.msg || '提交失败');
		} finally {
			submitting.value = false;
		}
	});
};

onMounted(async () => {
	loadAddressOptions();
	await loadCurrent();
	if (!isCreateMode.value && form.auditStatus === '0') {
		startAutoAuditPolling();
	}
});

onBeforeUnmount(() => {
	stopAutoAuditPolling();
});
</script>

<style scoped>
.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.address-empty-tip {
	margin-top: 8px;
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12px;
}

.address-empty-tip :deep(.el-alert) {
	flex: 1;
}
</style>
