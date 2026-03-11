<template>
	<div class="system-user-dialog-container">
		<el-dialog :close-on-click-modal="false" :title="dataForm.userId ? $t('common.editBtn') : $t('common.addBtn')" draggable v-model="visible">
			<el-form :model="dataForm" :rules="dataRules" label-width="90px" ref="dataFormRef" v-loading="loading">
				<el-row :gutter="20">
					<el-col :span="12" class="mb20">
						<el-form-item :label="$t('sysuser.username')" prop="username">
							<el-input :disabled="dataForm.userId !== ''" placeholder="请输入用户名" v-model="dataForm.username"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="12" class="mb20">
						<el-form-item :label="$t('sysuser.password')" prop="password">
							<el-input clearable placeholder="请输入密码" type="password" v-model="dataForm.password"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="12" class="mb20">
						<el-form-item :label="$t('sysuser.name')" prop="name">
							<el-input clearable placeholder="请输入姓名" v-model="dataForm.name"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="12" class="mb20">
						<el-form-item :label="$t('sysuser.phone')" prop="phone">
							<el-input clearable placeholder="请输入手机号" v-model="dataForm.phone"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="12" class="mb20">
						<el-form-item :label="$t('sysuser.role')" prop="role">
							<el-select class="w100" clearable multiple placeholder="请选择角色" v-model="dataForm.role">
								<el-option :key="item.roleId" :label="item.roleName" :value="item.roleId" v-for="item in roleData" />
							</el-select>
						</el-form-item>
					</el-col>

					<el-col :span="12" class="mb20">
						<el-form-item :label="$t('sysuser.email')" prop="email">
							<el-input clearable placeholder="请输入邮箱" v-model="dataForm.email"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="12" class="mb20">
						<el-form-item :label="$t('sysuser.nickname')" prop="nickname">
							<el-input clearable placeholder="请输入昵称" v-model="dataForm.nickname"></el-input>
						</el-form-item>
					</el-col>
					<el-col :span="12" class="mb20">
						<el-form-item :label="$t('sysuser.lockFlag')" prop="lockFlag">
							<el-radio-group v-model="dataForm.lockFlag">
								<el-radio :key="index" :value="item.value" border v-for="(item, index) in lock_flag">{{ item.label }} </el-radio>
							</el-radio-group>
						</el-form-item>
					</el-col>
				</el-row>
			</el-form>
			<template #footer>
				<span class="dialog-footer">
					<el-button @click="visible = false">{{ $t('common.cancelButtonText') }}</el-button>
					<el-button @click="onSubmit" type="primary" :disabled="loading">{{ $t('common.confirmButtonText') }}</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>

<script lang="ts" name="systemUserDialog" setup>
import { addObj, getObj, putObj, validatePhone, validateUsername } from '/@/api/admin/user';
import { list as roleList } from '/@/api/admin/role';
import { useDict } from '/@/hooks/dict';
import { useI18n } from 'vue-i18n';
import { useMessage } from '/@/hooks/message';
import { rule } from '/@/utils/validate';

const { t } = useI18n();

// 定义刷新表格emit
const emit = defineEmits(['refresh']);
// @ts-ignore
const { lock_flag } = useDict('lock_flag');

// 定义变量内容
const dataFormRef = ref();
const visible = ref(false);
const roleData = ref<any[]>([]);
const loading = ref(false);

const dataForm = reactive({
	userId: '',
	username: '',
	password: '' as String | undefined,
	salt: '',
	wxOpenid: '',
	qqOpenid: '',
	lockFlag: '0',
	phone: '' as String | undefined,
	roleList: [],
	nickname: '',
	name: '',
	email: '',
	role: [] as string[],
});

const dataRules = ref({
	// 用户名校验，不能为空 、长度 5-20、不能和已有数据重复
	username: [
		{validator: rule.overLength, trigger: 'blur'},
		{ required: true, message: '用户名不能为空', trigger: 'blur' },
		{ min: 5, max: 20, message: '用户名称长度必须介于 5 和 20 之间', trigger: 'blur' },
		{
			validator: (rule: any, value: any, callback: any) => {
				validateUsername(rule, value, callback, dataForm.userId !== '');
			},
			trigger: 'blur',
		},
	],
	password: [
		{validator: rule.overLength, trigger: 'blur'},
		{ required: true, message: '密码不能为空', trigger: 'blur' },
		{
			min: 6,
			max: 20,
			message: '用户密码长度必须介于 5 和 20 之间',
			trigger: 'blur',
		},
	],
	// 姓名校验，不能为空、只能是中文
	name: [
		{ required: true, message: '姓名不能为空', trigger: 'blur' },
		{validator: rule.overLength, trigger: 'blur'},
		{ validator: rule.chinese, trigger: 'blur' },
	],
	role: [{ required: true, message: '角色不能为空', trigger: 'blur' }],
	// 手机号校验，不能为空、新增的时不能重复校验
	phone: [
		{ required: true, message: '手机号不能为空', trigger: 'blur' },
		{validator: rule.overLength, trigger: 'blur'},
		{ validator: rule.validatePhone, trigger: 'blur' },
		{
			validator: (rule: any, value: any, callback: any) => {
				validatePhone(rule, value, callback, dataForm.userId !== '');
			},
			trigger: 'blur',
		},
	],
	email: [{validator: rule.overLength, trigger: 'blur'},{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
	lockFlag: [{ required: true, message: '状态不能为空', trigger: 'blur' }],
  nickname: [{validator: rule.overLength, trigger: 'blur'}],
});

// 打开弹窗
const openDialog = async (id: string) => {
	visible.value = true;
	dataForm.userId = '';

	// 重置表单数据
	nextTick(() => {
		dataFormRef.value?.resetFields();
	});

	// 加载使用的数据
	getRoleData();

    // 修改获取用户信息
	if (id) {
		dataForm.userId = id;
		await getUserData(id);
		dataForm.password = '******';
	}
};

// 提交
const onSubmit = async () => {
	const valid = await dataFormRef.value.validate().catch(() => {});
	if (!valid) return false;

	try {
		const { userId, phone, password } = dataForm;

		if (userId) {
			// 清除占位符，避免提交错误的数据
			if (phone?.includes('*')) dataForm.phone = undefined;
			if (password?.includes('******')) dataForm.password = undefined;

			loading.value = true;
			await putObj(dataForm);
			useMessage().success(t('common.editSuccessText'));
			visible.value = false; // 关闭弹窗
			emit('refresh');
		} else {
			loading.value = true;
			await addObj(dataForm);
			useMessage().success(t('common.addSuccessText'));
			visible.value = false; // 关闭弹窗
			emit('refresh');
		}
	} catch (error: any) {
		useMessage().error(error.msg);
	} finally {
		loading.value = false;
	}
};

/**
 * 从服务器获取用户数据
 *
 * @async
 * @param {string} id - 用户 ID
 * @return {Promise} - 包含用户数据的 Promise 对象
 */
const getUserData = async (id: string) => {
	try {
		loading.value = true;
		const { data } = await getObj(id);
		Object.assign(dataForm, data);
		if (data.roleList) {
			dataForm.role = data.roleList.map((item) => item.roleId);
		}
	} catch (err: any) {
		useMessage().error(err.msg);
	} finally {
		loading.value = false;
	}
};

// 角色数据
const getRoleData = () => {
	roleList().then((res) => {
		roleData.value = res.data;
		// 默认选择第一个
		dataForm.role = [res.data[0].roleId];
	});
};

// 暴露变量
defineExpose({
	openDialog,
});
</script>
