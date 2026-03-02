<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row v-show="showSearch">
				<el-form :model="state.queryForm" ref="queryRef" :inline="true" @keyup.enter="getDataList">
					<el-form-item label="相册ID" prop="albumId">
						<el-input-number v-model="state.queryForm.albumId" :min="1" />
					</el-form-item>
					<el-form-item>
						<el-button type="primary" icon="Search" @click="getDataList">查询</el-button>
						<el-button icon="Refresh" @click="resetQuery">重置</el-button>
					</el-form-item>
				</el-form>
			</el-row>

			<el-row>
				<div class="mb8" style="width: 100%">
					<el-button type="primary" icon="Link" class="ml10" v-auth="'media_share_add'" @click="openCreate">创建分享</el-button>
					<right-toolbar v-model:showSearch="showSearch" @queryTable="getDataList" class="ml10" style="float: right; margin-right: 20px" />
				</div>
			</el-row>

			<el-table
				:data="state.dataList"
				v-loading="state.loading"
				style="width: 100%"
				border
				:cell-style="tableStyle.cellStyle"
				:header-cell-style="tableStyle.headerCellStyle"
			>
				<el-table-column type="index" label="#" width="60" />
				<el-table-column prop="albumId" label="相册ID" width="100" />
				<el-table-column prop="shareToken" label="分享Token" show-overflow-tooltip />
				<el-table-column prop="code" label="提取码" width="90" />
				<el-table-column prop="expireAt" label="过期时间" width="180" />
				<el-table-column prop="maxViewCount" label="最大次数" width="100" />
				<el-table-column prop="currentViewCount" label="已访问" width="100" />
				<el-table-column prop="status" label="状态" width="90">
					<template #default="scope">{{ scope.row.status === '0' ? '启用' : '关闭' }}</template>
				</el-table-column>
				<el-table-column label="操作" width="300">
					<template #default="scope">
						<el-button icon="View" text type="primary" @click="openDetail(scope.row)">详情</el-button>
						<el-button icon="Key" text type="primary" @click="openVerify(scope.row)">校验码</el-button>
						<el-button icon="Delete" text type="primary" v-auth="'media_share_del'" @click="handleDisable(scope.row)">关闭</el-button>
					</template>
				</el-table-column>
			</el-table>
			<pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" v-bind="state.pagination" />
		</div>

		<el-dialog v-model="createVisible" title="创建分享" width="500px" draggable>
			<el-form :model="createForm" label-width="110px">
				<el-form-item label="相册ID">
					<el-input-number v-model="createForm.albumId" :min="1" />
				</el-form-item>
				<el-form-item label="提取码">
					<el-input v-model="createForm.code" />
				</el-form-item>
				<el-form-item label="有效小时数">
					<el-input-number v-model="createForm.expireHours" :min="1" />
				</el-form-item>
				<el-form-item label="最大访问次数">
					<el-input-number v-model="createForm.maxViewCount" :min="1" />
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="createVisible = false">取消</el-button>
				<el-button type="primary" @click="submitCreate">创建</el-button>
			</template>
		</el-dialog>

		<el-dialog v-model="verifyVisible" title="校验提取码" width="440px" draggable>
			<el-form :model="verifyForm" label-width="90px">
				<el-form-item label="token">
					<el-input v-model="verifyForm.token" disabled />
				</el-form-item>
				<el-form-item label="提取码">
					<el-input v-model="verifyForm.code" />
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="verifyVisible = false">取消</el-button>
				<el-button type="primary" @click="submitVerify">校验</el-button>
			</template>
		</el-dialog>

		<el-drawer v-model="detailVisible" title="分享详情" size="45%">
			<el-input type="textarea" :rows="18" v-model="detailText" readonly />
		</el-drawer>
	</div>
</template>

<script setup lang="ts" name="mediaShares">
import { BasicTableProps, useTable } from '/@/hooks/table';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { mediaSharePage, createMediaShare, disableMediaShare, shareDetail, shareVerify } from '/@/api/media/share';

const queryRef = ref();
const showSearch = ref(true);

const createVisible = ref(false);
const verifyVisible = ref(false);
const detailVisible = ref(false);
const detailText = ref('');

const createForm = reactive<any>({
	albumId: null,
	code: '',
	expireHours: 24,
	maxViewCount: 1000,
});

const verifyForm = reactive<any>({
	token: '',
	code: '',
});

const state: BasicTableProps = reactive<BasicTableProps>({
	queryForm: {},
	pageList: mediaSharePage,
	descs: ['create_time'],
});

const { getDataList, currentChangeHandle, sizeChangeHandle, tableStyle } = useTable(state);

const resetQuery = () => {
	queryRef.value.resetFields();
	getDataList();
};

const openCreate = () => {
	createForm.albumId = null;
	createForm.code = '';
	createForm.expireHours = 24;
	createForm.maxViewCount = 1000;
	createVisible.value = true;
};

const submitCreate = async () => {
	if (!createForm.albumId) {
		useMessage().warning('请输入相册ID');
		return;
	}
	await createMediaShare(createForm);
	useMessage().success('创建成功');
	createVisible.value = false;
	getDataList(false);
};

const openVerify = (row: any) => {
	verifyForm.token = row.shareToken;
	verifyForm.code = '';
	verifyVisible.value = true;
};

const submitVerify = async () => {
	const res = await shareVerify(verifyForm.token, verifyForm.code);
	if (res.code === 0) {
		useMessage().success('提取码验证通过');
		verifyVisible.value = false;
	}
};

const openDetail = async (row: any) => {
	const res = await shareDetail(row.shareToken);
	detailText.value = JSON.stringify(res.data, null, 2);
	detailVisible.value = true;
};

const handleDisable = async (row: any) => {
	try {
		await useMessageBox().confirm('确认关闭该分享吗？');
	} catch {
		return;
	}
	await disableMediaShare(row.id);
	useMessage().success('已关闭');
	getDataList(false);
};
</script>
