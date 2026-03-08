<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card v-loading="loading">
				<template #header>
					<div class="card-header">
						<span>客户扩展信息</span>
					</div>
				</template>

				<el-alert
					v-if="isCreateMode"
					title="当前用户暂无客户扩展信息，请先新增"
					type="warning"
					show-icon
					:closable="false"
					class="mb20"
				/>

				<el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 700px">
					<el-form-item label="客户姓名" prop="realName">
						<el-input v-model="form.realName" maxlength="64" placeholder="请输入客户姓名" />
					</el-form-item>

					<el-form-item label="默认地址ID" prop="defaultAddressId">
						<el-select v-model="form.defaultAddressId" placeholder="请选择默认地址" filterable clearable style="width: 100%">
							<el-option v-for="item in addressOptions" :key="item.value" :label="item.label" :value="item.value" />
						</el-select>
						<div v-if="!addressOptions.length" class="address-empty-tip">
							<el-alert title="暂无可用地址，请先新增地址" type="warning" :closable="false" show-icon />
							<el-button type="primary" link @click="goAddressPage">去新增地址</el-button>
						</div>
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

<script setup lang="ts" name="customerInfo">
import { useMessage } from '/@/hooks/message';
import { listAddress } from '/@/api/takeaway/address';
import { createCustomer, currentCustomer, updateCustomer } from '/@/api/takeaway/customer';
import { useUserInfo } from '/@/stores/userInfo';
import { useRouter } from 'vue-router';

const formRef = ref();
const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const isCreateMode = ref(true);
const addressOptions = ref<Array<{ label: string; value: string }>>([]);

const form = reactive({
	id: undefined as number | undefined,
	userId: undefined as number | undefined,
	realName: '',
	defaultAddressId: '',
});

const rules = reactive({
	realName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
	defaultAddressId: [{ required: true, message: '请选择默认地址', trigger: 'change' }],
});

const validateAddressField = async () => {
	await nextTick();
	formRef.value?.validateField('defaultAddressId');
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
	form.realName = '';
	form.defaultAddressId = '';
};

const loadCurrent = async () => {
	loading.value = true;
	try {
		const res = await currentCustomer();
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
		form.realName = data.realName || '';
		form.defaultAddressId = data.defaultAddressId ? String(data.defaultAddressId) : '';
	} finally {
		loading.value = false;
		await validateAddressField();
	}
};

const handleSubmit = async () => {
	await formRef.value.validate(async (valid: boolean) => {
		if (!valid) return;

		submitting.value = true;
		try {
			const currentUserId = useUserInfo().userInfos?.user?.userId;
			const payload: any = {
				realName: form.realName,
				defaultAddressId: form.defaultAddressId || undefined,
			};

			if (isCreateMode.value) {
				payload.userId = currentUserId;
				await createCustomer(payload);
				useMessage().success('客户扩展信息新增成功');
			} else {
				payload.id = form.id;
				payload.userId = form.userId || currentUserId;
				await updateCustomer(payload);
				useMessage().success('客户扩展信息更新成功');
			}

			await loadCurrent();
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
