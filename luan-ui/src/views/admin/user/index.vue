<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
					<el-row v-show="showSearch">
						<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList">
							<el-form-item :label="$t('sysuser.username')" prop="username">
								<el-input v-model="state.queryForm.username" :placeholder="$t('sysuser.inputUsernameTip')" clearable />
							</el-form-item>
							<el-form-item :label="$t('sysuser.phone')" prop="phone">
								<el-input v-model="state.queryForm.phone" :placeholder="$t('sysuser.inputPhoneTip')" clearable />
							</el-form-item>
							<el-form-item>
								<el-button icon="Search" type="primary" @click="getDataList">{{ $t('common.queryBtn') }}</el-button>
								<el-button icon="Refresh" @click="resetQuery">{{ $t('common.resetBtn') }}</el-button>
							</el-form-item>
						</el-form>
					</el-row>
					<el-row>
						<div class="mb8" style="width: 100%">
							<el-button v-auth="'sys_user_add'" icon="folder-add" type="primary" @click="userDialogRef.openDialog()">
								{{ $t('common.addBtn') }}
							</el-button>

							<el-button
								plain
								class="ml10"
								icon="document"
								type="primary"
								@click="triggerJsonUpload"
							>
								{{ $t('sysuser.batchRegisterBtn') }}
							</el-button>
							<input
								ref="jsonUploadRef"
								type="file"
								accept=".json"
								style="display: none"
								@change="handleJsonUpload"
							/>

							<el-button
								plain
								v-auth="'sys_user_del'"
								:disabled="multiple"
								class="ml10"
								icon="Delete"
								type="primary"
								@click="handleDelete(selectObjs)"
							>
								{{ $t('common.delBtn') }}
							</el-button>

							<right-toolbar
								v-model:showSearch="showSearch"
								:export="'sys_user_export'"
								@exportExcel="exportExcel"
								@queryTable="getDataList"
								class="ml10 mr20"
								style="float: right"
							/>
						</div>
					</el-row>
					<el-table
						v-loading="state.loading"
						:data="state.dataList"
						@selection-change="handleSelectionChange"
						border
						:cell-style="tableStyle.cellStyle"
						:header-cell-style="tableStyle.headerCellStyle"
					>
						<el-table-column :selectable="handleSelectable" type="selection" width="40" />
						<el-table-column :label="$t('sysuser.index')" type="index" width="60" fixed="left" />
						<el-table-column :label="$t('sysuser.username')" prop="username" fixed="left" show-overflow-tooltip></el-table-column>
						<el-table-column :label="$t('sysuser.name')" prop="name" show-overflow-tooltip></el-table-column>
						<el-table-column :label="$t('sysuser.phone')" prop="phone" show-overflow-tooltip></el-table-column>
						<el-table-column :label="$t('sysuser.role')" show-overflow-tooltip>
							<template #default="scope">
								<el-tag v-for="(item, index) in scope.row.roleList" :key="index">{{ item.roleName }}</el-tag>
							</template>
						</el-table-column>
						<el-table-column :label="$t('sysuser.lockFlag')" show-overflow-tooltip>
							<template #default="scope">
								<el-switch v-model="scope.row.lockFlag" @change="changeSwitch(scope.row)" active-value="0" inactive-value="9"></el-switch>
							</template>
						</el-table-column>
						<el-table-column :label="$t('sysuser.createTime')" prop="createTime" show-overflow-tooltip width="180"></el-table-column>
						<el-table-column :label="$t('common.action')" width="160" fixed="right">
							<template #default="scope">
								<el-button v-auth="'sys_user_edit'" icon="edit-pen" text type="primary" @click="userDialogRef.openDialog(scope.row.userId)">
									{{ $t('common.editBtn') }}
								</el-button>
								<el-tooltip :content="$t('sysuser.deleteDisabledTip')" :disabled="scope.row.userId !== '1'" placement="top">
									<span style="margin-left: 12px">
										<el-button
											icon="delete"
											v-auth="'sys_user_del'"
											:disabled="scope.row.username === 'admin'"
											text
											type="primary"
											@click="handleDelete([scope.row.userId])"
											>{{ $t('common.delBtn') }}
										</el-button>
									</span>
								</el-tooltip>
							</template>
						</el-table-column>
					</el-table>
					<pagination v-bind="state.pagination" @current-change="currentChangeHandle" @size-change="sizeChangeHandle"> </pagination>
		</div>

		<user-form ref="userDialogRef" @refresh="getDataList(false)" />
	</div>
</template>

<script lang="ts" name="systemUser" setup>
import { delObj, pageList, putObj, batchRegister } from '/@/api/admin/user';
import { BasicTableProps, useTable } from '/@/hooks/table';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { useI18n } from 'vue-i18n';

// 动态引入组件
const UserForm = defineAsyncComponent(() => import('./form.vue'));

const { t } = useI18n();

// 定义变量内容
const userDialogRef = ref();
const queryRef = ref();
const showSearch = ref(true);
const jsonUploadRef = ref();

// 多选rows
const selectObjs = ref([]) as any;
// 是否可以多选
const multiple = ref(true);

// 定义表格查询、后台调用的API
const state: BasicTableProps = reactive<BasicTableProps>({
	queryForm: {
		username: '',
		phone: '',
	},
	pageList: pageList,
});
const { getDataList, currentChangeHandle, sizeChangeHandle, downBlobFile, tableStyle } = useTable(state);

// 清空搜索条件
const resetQuery = () => {
	queryRef.value?.resetFields();
	getDataList();
};

// 导出excel
const exportExcel = () => {
	downBlobFile('/admin/user/export', state.queryForm, 'users.xlsx');
};

// 是否可以多选
const handleSelectable = (row: any) => {
	return row.username !== 'admin';
};

// 多选事件
const handleSelectionChange = (objs: { userId: string }[]) => {
	selectObjs.value = objs.map(({ userId }) => userId);
	multiple.value = !objs.length;
};

// 删除操作
const handleDelete = async (ids: string[]) => {
	try {
		await useMessageBox().confirm(t('common.delConfirmText'));
	} catch {
		return;
	}

	try {
		await delObj(ids);
		getDataList();
		useMessage().success(t('common.delSuccessText'));
	} catch (err: any) {
		useMessage().error(err.msg);
	}
};

//表格内开关 (用户状态)
const changeSwitch = async (row: object) => {
	await putObj(row);
	useMessage().success(t('common.optSuccessText'));
	getDataList();
};

// 触发JSON文件选择
const triggerJsonUpload = () => {
	jsonUploadRef.value?.click();
};

// JSON文件上传处理
const handleJsonUpload = async (event: Event) => {
	const input = event.target as HTMLInputElement;
	if (!input.files || !input.files[0]) {
		return;
	}

	const file = input.files[0];
	try {
		const text = await file.text();
		const jsonData = JSON.parse(text);

		// 构建请求格式 - 支持两种格式：
		// 1. 直接的 users 数组: [{username: "xxx", ...}]
		// 2. 分类型格式: {merchants: [...], customers: [...], deliveries: [...]}
		let users = [];
		if (Array.isArray(jsonData)) {
			users = jsonData;
		} else if (jsonData.users) {
			users = jsonData.users;
		} else {
			// 尝试合并分类数据
			const categoryKeys = ['merchants', 'customers', 'deliveries', 'roles'];
			for (const key of categoryKeys) {
				if (Array.isArray(jsonData[key])) {
					users = users.concat(jsonData[key]);
				}
			}
		}

		if (users.length === 0) {
			useMessage().warning('JSON文件中没有找到用户数据');
			return;
		}

		const response: any = await batchRegister({ users });

		// 检查响应状态
		if (response.code === 0 || response.code === 200) {
			const result = response.data;
			let msg = `批量注册完成：共 ${result.total} 个，成功 ${result.successCount} 个，失败 ${result.failCount} 个`;
			if (result.failCount > 0) {
				msg += '\n失败详情：\n';
				result.results
					.filter((r: any) => !r.success)
					.forEach((r: any) => {
						msg += `- ${r.username}: ${r.errorMessage}\n`;
					});
				useMessage().error(msg);
			} else {
				useMessage().success(msg);
			}
			getDataList();
		} else {
			useMessage().error(response.msg || '批量注册失败');
		}
	} catch (err: any) {
		console.error('批量注册错误:', err);
		const errorMsg = err?.response?.data?.msg || err?.message || '请检查JSON格式';
		useMessage().error('操作失败：' + errorMsg);
	}

	// 清空input值，允许重复选择同一文件
	input.value = '';
};
</script>
