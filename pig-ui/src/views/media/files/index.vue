<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row v-show="showSearch">
				<el-form :model="state.queryForm" ref="queryRef" :inline="true" @keyup.enter="getDataList">
					<el-form-item label="原始文件名" prop="originName">
						<el-input v-model="state.queryForm.originName" placeholder="请输入文件名" clearable />
					</el-form-item>
					<el-form-item>
						<el-button type="primary" icon="Search" @click="getDataList">查询</el-button>
						<el-button icon="Refresh" @click="resetQuery">重置</el-button>
					</el-form-item>
				</el-form>
			</el-row>

			<el-row>
				<div class="mb8" style="width: 100%">
					<el-upload :show-file-list="false" :http-request="handleUploadRequest" accept="image/*">
						<el-button icon="Upload" type="primary" class="ml10" v-auth="'media_file_upload'">上传图片</el-button>
					</el-upload>
					<el-button :disabled="multiple" icon="Delete" type="primary" class="ml10" v-auth="'media_file_del'" @click="handleDelete(selectObjs)">
						批量删除
					</el-button>
					<right-toolbar v-model:showSearch="showSearch" @queryTable="getDataList" class="ml10" style="float: right; margin-right: 20px" />
				</div>
			</el-row>

			<el-row>
				<el-card class="w-full mb8" shadow="never">
					<template #header>预签名/确认接口联调</template>
					<el-form :inline="true" :model="presignForm">
						<el-form-item label="文件名">
							<el-input v-model="presignForm.fileName" placeholder="demo.png" />
						</el-form-item>
						<el-form-item label="类型">
							<el-input v-model="presignForm.contentType" placeholder="image/png" />
						</el-form-item>
						<el-form-item>
							<el-button type="primary" @click="handlePresign">获取预签名参数</el-button>
						</el-form-item>
					</el-form>
					<el-form :inline="true" :model="confirmForm">
						<el-form-item label="objectKey">
							<el-input v-model="confirmForm.objectKey" style="width: 280px" />
						</el-form-item>
						<el-form-item label="原文件名">
							<el-input v-model="confirmForm.originName" />
						</el-form-item>
						<el-form-item label="大小">
							<el-input-number v-model="confirmForm.fileSize" :min="1" />
						</el-form-item>
						<el-form-item>
							<el-button @click="handleConfirm">确认入库</el-button>
						</el-form-item>
					</el-form>
					<el-input v-model="debugResult" type="textarea" :rows="3" readonly />
				</el-card>
			</el-row>

			<el-table
				:data="state.dataList"
				v-loading="state.loading"
				style="width: 100%"
				@selection-change="handleSelectionChange"
				border
				:cell-style="tableStyle.cellStyle"
				:header-cell-style="tableStyle.headerCellStyle"
			>
				<el-table-column type="selection" width="40" align="center" />
				<el-table-column type="index" label="#" width="60" />
				<el-table-column prop="originName" label="原始文件名" show-overflow-tooltip />
				<el-table-column prop="objectKey" label="对象键" show-overflow-tooltip />
				<el-table-column prop="contentType" label="类型" width="140" />
				<el-table-column prop="fileSize" label="大小" width="100" />
				<el-table-column prop="createTime" label="创建时间" width="180" />
				<el-table-column label="操作" width="220">
					<template #default="scope">
						<el-button icon="Delete" text type="primary" v-auth="'media_file_del'" @click="handleDelete([scope.row.id])">删除</el-button>
						<el-button icon="Download" text type="primary" @click="handleDownload(scope.row.id)">下载</el-button>
					</template>
				</el-table-column>
			</el-table>
			<pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" v-bind="state.pagination" />
		</div>
	</div>
</template>

<script setup lang="ts" name="mediaFiles">
import { BasicTableProps, useTable } from '/@/hooks/table';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { mediaFilePage, uploadMediaFile, deleteMediaFile, getMediaFileDownloadUrl, mediaFilePresign, mediaFileConfirm } from '/@/api/media/file';

const showSearch = ref(true);
const queryRef = ref();
const selectObjs = ref([]) as any;
const multiple = ref(true);
const debugResult = ref('');

const presignForm = reactive({
	fileName: 'demo.png',
	contentType: 'image/png',
});

const confirmForm = reactive({
	objectKey: '',
	originName: 'demo.png',
	contentType: 'image/png',
	fileSize: 1,
	md5: '',
});

const state: BasicTableProps = reactive<BasicTableProps>({
	queryForm: {},
	pageList: mediaFilePage,
	descs: ['create_time'],
});

const { getDataList, currentChangeHandle, sizeChangeHandle, tableStyle } = useTable(state);

const resetQuery = () => {
	queryRef.value.resetFields();
	getDataList();
};

const handleUploadRequest = async (options: any) => {
	try {
		await uploadMediaFile(options.file as File);
		useMessage().success('上传成功');
		getDataList();
	} catch (err: any) {
		useMessage().error(err.msg || '上传失败');
	}
};

const handleSelectionChange = (rows: { id: number }[]) => {
	selectObjs.value = rows.map((row) => row.id);
	multiple.value = !rows.length;
};

const handleDelete = async (ids: number[]) => {
	if (!ids.length) return;
	try {
		await useMessageBox().confirm('确认删除所选图片吗？');
	} catch {
		return;
	}
	for (const id of ids) {
		await deleteMediaFile(id);
	}
	useMessage().success('删除成功');
	getDataList();
};

const handleDownload = async (id: number) => {
	try {
		const res = await getMediaFileDownloadUrl(id);
		window.open(`${import.meta.env.VITE_API_URL}${res.data}`, '_blank');
	} catch (err: any) {
		useMessage().error(err.msg || '下载失败');
	}
};

const handlePresign = async () => {
	try {
		const res = await mediaFilePresign(presignForm);
		debugResult.value = JSON.stringify(res.data, null, 2);
		if (res.data?.objectKey) {
			confirmForm.objectKey = res.data.objectKey;
		}
	} catch (err: any) {
		useMessage().error(err.msg || '获取失败');
	}
};

const handleConfirm = async () => {
	try {
		const res = await mediaFileConfirm(confirmForm);
		debugResult.value = JSON.stringify(res.data, null, 2);
		useMessage().success('确认成功');
		getDataList();
	} catch (err: any) {
		useMessage().error(err.msg || '确认失败');
	}
};
</script>
