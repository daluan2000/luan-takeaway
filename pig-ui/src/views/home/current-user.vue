<template>
	<el-card class="h-full shadow-sm hover:shadow-md transition-shadow">
		<div class="flex items-center justify-between">
			<!-- 用户信息 -->
			<div class="flex items-center gap-4">
				<el-avatar 
					:size="56" 
					shape="circle" 
					:src="baseURL + userData.avatar"
					class="ring-1 ring-gray-100" 
				/>
				<div>
					<h3 class="text-lg font-semibold text-gray-800 mb-2">{{ userData.name }}</h3>
					<div class="flex items-center gap-2 text-sm">
						<span class="px-3 py-1 bg-blue-50 text-blue-600 rounded-full">{{ userData?.dept?.name }}</span>
						<span v-if="userData.postName" class="px-3 py-1 bg-green-50 text-green-600 rounded-full">{{ userData.postName }}</span>
					</div>
				</div>
			</div>

			<!-- 时间 -->
			<div class="flex items-center gap-2 text-sm text-gray-500">
				<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
				</svg>
				<span>{{ parseTime(date) }}</span>
			</div>
		</div>

		<el-divider class="my-4" />

		<div class="role-form-wrapper">
			<el-form label-width="90px" v-loading="loading">
				<div class="role-row">
					<div class="role-field">
						<el-form-item label="角色">
							<el-select
								v-model="roleForm.role"
								multiple
								clearable
								class="w100"
								placeholder="请选择角色"
								:disabled="!roleEditable"
							>
								<el-option v-for="item in roleData" :key="item.roleId" :label="item.roleName" :value="item.roleId" />
							</el-select>
						</el-form-item>
					</div>

					<div class="role-actions">
						<el-button type="primary" plain @click="startRoleEdit" :disabled="roleEditable">编辑</el-button>
						<el-button type="primary" @click="submitRoleEdit" :loading="roleSubmitting" :disabled="!roleEditable">提交</el-button>
					</div>
				</div>
				<div v-if="extMissingWarnings.length" class="role-warning" v-loading="extChecking">
					<div v-for="item in extMissingWarnings" :key="item" class="role-warning-item">{{ item }}</div>
				</div>
			</el-form>
		</div>
	</el-card>
</template>

<script setup lang="ts" name="currentUser">
import { useUserInfo } from '/@/stores/userInfo';
import { getObj, updateUserRole } from '/@/api/admin/user';
import { list as roleList } from '/@/api/admin/role';
import { useMessage } from '/@/hooks/message';
import { currentMerchant } from '/@/api/takeaway/merchant';
import { currentRider } from '/@/api/takeaway/delivery';
import { currentCustomer } from '/@/api/takeaway/customer';

const date = ref(new Date());

const userData = ref({
	postName: '',
	name: '',
	username: '',
	userId: '',
	avatar: '',
	deptName: '',
} as any);
const loading = ref(false);
const roleSubmitting = ref(false);
const roleEditable = ref(false);
const extChecking = ref(false);
const extMissingWarnings = ref<string[]>([]);
const roleData = ref<any[]>([]);
const roleForm = reactive({
	username: '',
	role: [] as (string | number)[],
});

const ROLE_EXTENSION_CHECKS = [
	{ roleCode: 'ROLE_MERCHANT', roleName: '商家', checker: currentMerchant },
	{ roleCode: 'ROLE_DELIVERY', roleName: '骑手', checker: currentRider },
	{ roleCode: 'ROLE_CUSTOMER', roleName: '客户', checker: currentCustomer },
];

setInterval(() => {
	date.value = new Date();
}, 1000);

onMounted(() => {
	const data = useUserInfo().userInfos;
	getRoleData();
	initUserInfo(data.user.userId);
});

/**
 * 根据用户 ID 初始化用户信息。
 * @param {any} userId - 要查询的用户 ID。
 * @returns {Promise<void>} - 初始化用户信息的 Promise 实例。
 */
const initUserInfo = async (userId: any): Promise<void> => {
	try {
		loading.value = true; // 显示加载状态

		const res = await getObj(userId); // 执行查询操作
		userData.value = res.data; // 将查询到的数据保存到 userData 变量中
		userData.value.postName = res.data?.postList?.map((item: any) => item.postName).join(',') || ''; // 将 postList 中的 postName 合并成字符串并保存到 userData 变量中
		roleForm.username = res.data?.username || '';
		roleForm.role = res.data?.roleList?.map((item: any) => item.roleId) || [];
		// 文件上传增加后端前缀
		userData.value.avatar = res.data.avatar;
		await checkRoleExtInfo(res.data?.roleList || []);
	} finally {
		loading.value = false; // 结束加载状态
	}
};

const checkRoleExtInfo = async (roleList: any[]) => {
	const roleCodeSet = new Set((roleList || []).map((item: any) => item?.roleCode).filter(Boolean));
	const checks = ROLE_EXTENSION_CHECKS.filter((item) => roleCodeSet.has(item.roleCode));
	if (!checks.length) {
		extMissingWarnings.value = [];
		return;
	}

	extChecking.value = true;
	try {
		const results = await Promise.allSettled(checks.map((item) => item.checker()));
		extMissingWarnings.value = results
			.map((result, index) => ({ result, roleName: checks[index].roleName }))
			.filter(({ result }) => result.status === 'rejected' || !result.value?.data)
			.map(({ roleName }) => `当前用户拥有【${roleName}】角色，但未找到对应扩展表信息，请先完善扩展资料`);
	} finally {
		extChecking.value = false;
	}
};

const getRoleData = async () => {
	const res = await roleList();
	roleData.value = res.data || [];
};

const startRoleEdit = () => {
	roleEditable.value = true;
};

const stopRoleEdit = () => {
	roleForm.role = (userData.value?.roleList || []).map((item: any) => item.roleId);
	roleEditable.value = false;
};

const submitRoleEdit = async () => {

	if (!roleForm.role.length) {
		useMessage().error('角色不能为空');
		stopRoleEdit();
		return;
	}

	try {
		roleSubmitting.value = true;
		await updateUserRole({
			username: roleForm.username,
			role: roleForm.role,
		});
		useMessage().success('角色修改成功');
		window.location.reload();
	} catch (error: any) {
		const message = error?.msg || error?.response?.data?.msg || '角色修改失败';
		stopRoleEdit();
		useMessage().error(message);
	} finally {
		roleSubmitting.value = false;
	}
};
</script>

<style scoped>
.el-card {
	border: 1px solid #e5e7eb;
	border-radius: 12px;
	transition: all 0.2s ease;
}

.el-card :deep(.el-card__body) {
	padding: 24px;
}

/* 头像轻微悬停效果 */
.el-avatar {
	transition: transform 0.2s ease;
}

.el-avatar:hover {
	transform: scale(1.05);
}

.role-form-wrapper {
	width: 100%;
}

.role-row {
	display: flex;
	align-items: flex-end;
	gap: 12px;
	flex-wrap: wrap;
}

.role-field {
	flex: 1;
	min-width: 260px;
}

.role-field :deep(.el-form-item) {
	margin-bottom: 0;
}

.role-actions {
	display: flex;
	justify-content: flex-start;
	gap: 12px;
}

.role-warning {
	margin-top: 12px;
	padding-left: 90px;
	font-size: 13px;
	line-height: 1.7;
	color: #ef4444;
}

.role-warning-item {
	font-weight: 500;
}

/* 标签悬停效果 */
.px-3.py-1 {
	transition: all 0.2s ease;
	font-weight: 500;
}

.px-3.py-1:hover {
	transform: translateY(-1px);
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 响应式适配 */
@media (max-width: 640px) {
	.flex.items-center.justify-between {
		flex-direction: column;
		align-items: flex-start;
		gap: 16px;
	}
	
	.el-card :deep(.el-card__body) {
		padding: 16px;
	}

	.role-form-wrapper {
		max-width: 100%;
	}

	.role-row {
		align-items: stretch;
	}

	.role-field {
		min-width: 100%;
	}

	.role-actions {
		width: 100%;
		justify-content: flex-start;
	}

	.role-warning {
		padding-left: 0;
	}
}
</style>

