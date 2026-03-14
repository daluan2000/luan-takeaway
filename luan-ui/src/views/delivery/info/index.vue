<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card v-loading="loading">
				<template #header>
					<div class="card-header">
						<span>骑手扩展信息</span>
					</div>
				</template>

				<el-alert
					v-if="isCreateMode"
					title="当前用户暂无骑手扩展信息，请先新增"
					type="warning"
					show-icon
					:closable="false"
					class="mb20"
				/>

				<el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 700px">
					<el-form-item label="骑手姓名" prop="realName">
						<el-input v-model="form.realName" maxlength="64" placeholder="请输入骑手姓名" />
					</el-form-item>

					<el-form-item label="配送范围(公里)" prop="deliveryScopeKm">
						<el-input-number
							v-model="form.deliveryScopeKm"
							:precision="2"
							:step="0.5"
							:min="0.5"
							:controls="true"
							placeholder="请输入配送范围"
							style="width: 100%"
						/>
					</el-form-item>

					<el-form-item label="在线状态" prop="onlineStatus">
						<el-select v-model="form.onlineStatus" placeholder="请选择在线状态" style="width: 100%">
							<el-option v-for="item in onlineStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
						</el-select>
					</el-form-item>

					<el-form-item label="在职状态" prop="employmentStatus">
						<el-select v-model="form.employmentStatus" placeholder="请选择在职状态" style="width: 100%">
							<el-option v-for="item in employmentStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
						</el-select>
					</el-form-item>

					<el-form-item>
						<el-button v-if="isCreateMode" v-auth="'wm_delivery_info_add'" type="primary" :loading="submitting" @click="handleSubmit">新增</el-button>
						<el-button v-else v-auth="'wm_delivery_info_edit'" type="primary" :loading="submitting" @click="handleSubmit">修改</el-button>
						<el-button @click="loadCurrent">刷新</el-button>
					</el-form-item>
				</el-form>
			</el-card>
		</div>
	</div>
</template>

<script setup lang="ts" name="deliveryInfo">
import { useMessage } from '/@/hooks/message';
import { currentRider, saveRider, updateRider } from '/@/api/takeaway/delivery';
import { useUserInfo } from '/@/stores/userInfo';
import { useDict } from '/@/hooks/dict';

const formRef = ref();
const loading = ref(false);
const submitting = ref(false);
const isCreateMode = ref(true);
const { takeaway_delivery_online_status, takeaway_delivery_employment_status } = useDict(
	'takeaway_delivery_online_status',
	'takeaway_delivery_employment_status'
);

const onlineStatusOptions = computed(() => takeaway_delivery_online_status.value || []);
const employmentStatusOptions = computed(() => takeaway_delivery_employment_status.value || []);

const form = reactive({
	id: undefined as number | undefined,
	userId: undefined as number | undefined,
	realName: '',
	deliveryScopeKm: undefined as number | undefined,
	onlineStatus: '0',
	employmentStatus: '1',
});

const rules = reactive({
	realName: [{ required: true, message: '请输入骑手姓名', trigger: 'blur' }],
	deliveryScopeKm: [{ required: true, message: '请输入配送范围', trigger: 'change' }],
	onlineStatus: [{ required: true, message: '请选择在线状态', trigger: 'change' }],
	employmentStatus: [{ required: true, message: '请选择在职状态', trigger: 'change' }],
});

const resetForm = () => {
	form.id = undefined;
	form.userId = undefined;
	form.realName = '';
	form.deliveryScopeKm = undefined;
	form.onlineStatus = '0';
	form.employmentStatus = '1';
};

const loadCurrent = async () => {
	loading.value = true;
	try {
		const res = await currentRider();
		const data = res?.data || {};
		const noExist = data?.noExist === true || !data.id;
		if (noExist) {
			isCreateMode.value = true;
			resetForm();
			return;
		}

		isCreateMode.value = false;
		form.id = data.id;
		form.userId = data.userId;
		form.realName = data.realName || '';
		form.deliveryScopeKm = data.deliveryScopeKm !== null && data.deliveryScopeKm !== undefined ? Number(data.deliveryScopeKm) : undefined;
		form.onlineStatus = data.onlineStatus || '0';
		form.employmentStatus = data.employmentStatus || '1';
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
				realName: form.realName,
				deliveryScopeKm: form.deliveryScopeKm,
				onlineStatus: form.onlineStatus,
				employmentStatus: form.employmentStatus,
			};

			if (isCreateMode.value) {
				payload.userId = currentUserId;
				await saveRider(payload);
				useMessage().success('骑手扩展信息新增成功');
			} else {
				payload.id = form.id;
				payload.userId = form.userId || currentUserId;
				await updateRider(payload);
				useMessage().success('骑手扩展信息更新成功');
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
	await loadCurrent();
});
</script>

<style scoped>
.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}
</style>
