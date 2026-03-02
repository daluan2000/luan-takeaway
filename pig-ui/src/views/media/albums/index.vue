<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row v-show="showSearch">
				<el-form :model="state.queryForm" ref="queryRef" :inline="true" @keyup.enter="getDataList">
					<el-form-item label="相册名" prop="name">
						<el-input v-model="state.queryForm.name" placeholder="请输入相册名" clearable />
					</el-form-item>
					<el-form-item>
						<el-button type="primary" icon="Search" @click="getDataList">查询</el-button>
						<el-button icon="Refresh" @click="resetQuery">重置</el-button>
					</el-form-item>
				</el-form>
			</el-row>

			<el-row>
				<div class="mb8" style="width: 100%">
					<el-button type="primary" icon="FolderAdd" class="ml10" v-auth="'media_album_add'" @click="openForm()">新建相册</el-button>
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
				<el-table-column prop="name" label="相册名称" show-overflow-tooltip />
				<el-table-column prop="description" label="描述" show-overflow-tooltip />
				<el-table-column prop="visibleScope" label="可见性" width="120" />
				<el-table-column prop="createTime" label="创建时间" width="180" />
				<el-table-column label="操作" width="300">
					<template #default="scope">
						<el-button icon="Edit" text type="primary" v-auth="'media_album_edit'" @click="openForm(scope.row)">编辑</el-button>
						<el-button icon="Picture" text type="primary" v-auth="'media_album_edit'" @click="openItems(scope.row)">图片管理</el-button>
						<el-button icon="Delete" text type="primary" v-auth="'media_album_del'" @click="handleDelete(scope.row.id)">删除</el-button>
					</template>
				</el-table-column>
			</el-table>
			<pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" v-bind="state.pagination" />
		</div>

		<el-dialog v-model="formVisible" :title="formModel.id ? '编辑相册' : '新建相册'" width="540px" draggable>
			<el-form :model="formModel" label-width="90px">
				<el-form-item label="相册名称">
					<el-input v-model="formModel.name" />
				</el-form-item>
				<el-form-item label="描述">
					<el-input v-model="formModel.description" type="textarea" />
				</el-form-item>
				<el-form-item label="可见性">
					<el-select v-model="formModel.visibleScope" style="width: 100%">
						<el-option label="private" value="private" />
						<el-option label="public" value="public" />
					</el-select>
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="formVisible = false">取消</el-button>
				<el-button type="primary" @click="saveAlbum">保存</el-button>
			</template>
		</el-dialog>

		<el-dialog v-model="itemsVisible" title="相册图片管理" width="980px" draggable>
			<el-row :gutter="12">
				<el-col :span="10">
					<el-card shadow="never" header="可选图片（我的图片）">
						<el-table :data="fileOptions" @selection-change="onFileSelect" height="360" border>
							<el-table-column type="selection" width="40" />
							<el-table-column prop="id" label="ID" width="90" />
							<el-table-column prop="originName" label="文件名" show-overflow-tooltip />
						</el-table>
						<el-button class="mt8" type="primary" @click="handleAddItems">添加到相册</el-button>
					</el-card>
				</el-col>
				<el-col :span="14">
					<el-card shadow="never" header="当前相册图片">
						<el-table :data="albumItems" height="360" border>
							<el-table-column prop="id" label="关联ID" width="90" />
							<el-table-column prop="fileId" label="图片ID" width="90" />
							<el-table-column label="排序" width="120">
								<template #default="scope">
									<el-input-number v-model="scope.row.sortNo" :min="0" />
								</template>
							</el-table-column>
							<el-table-column label="操作" width="120">
								<template #default="scope">
									<el-button type="danger" text @click="handleRemoveItem(scope.row.fileId)">移除</el-button>
								</template>
							</el-table-column>
						</el-table>
						<div class="mt8">
							<el-button type="primary" @click="saveSort">保存排序</el-button>
						</div>
					</el-card>
				</el-col>
			</el-row>
		</el-dialog>
	</div>
</template>

<script setup lang="ts" name="mediaAlbums">
import { BasicTableProps, useTable } from '/@/hooks/table';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { mediaAlbumPage, createMediaAlbum, updateMediaAlbum, deleteMediaAlbum, albumItems as fetchAlbumItems, addAlbumItems, removeAlbumItem, sortAlbumItems } from '/@/api/media/album';
import { mediaFilePage } from '/@/api/media/file';

const queryRef = ref();
const showSearch = ref(true);
const formVisible = ref(false);
const itemsVisible = ref(false);

const currentAlbumId = ref<number | null>(null);
const albumItemsData = ref<any[]>([]);
const fileOptions = ref<any[]>([]);
const selectedFileIds = ref<number[]>([]);

const state: BasicTableProps = reactive<BasicTableProps>({
	queryForm: {},
	pageList: mediaAlbumPage,
	descs: ['create_time'],
});

const { getDataList, currentChangeHandle, sizeChangeHandle, tableStyle } = useTable(state);

const formModel = reactive<any>({
	id: null,
	name: '',
	description: '',
	visibleScope: 'private',
});

const albumItems = computed(() => albumItemsData.value);

const resetQuery = () => {
	queryRef.value.resetFields();
	getDataList();
};

const openForm = (row?: any) => {
	if (!row) {
		formModel.id = null;
		formModel.name = '';
		formModel.description = '';
		formModel.visibleScope = 'private';
	} else {
		formModel.id = row.id;
		formModel.name = row.name;
		formModel.description = row.description;
		formModel.visibleScope = row.visibleScope || 'private';
	}
	formVisible.value = true;
};

const saveAlbum = async () => {
	if (!formModel.name) {
		useMessage().warning('请输入相册名称');
		return;
	}
	try {
		if (formModel.id) {
			await updateMediaAlbum(formModel.id, formModel);
		} else {
			await createMediaAlbum(formModel);
		}
		formVisible.value = false;
		useMessage().success('保存成功');
		getDataList(false);
	} catch (err: any) {
		useMessage().error(err.msg || '保存失败');
	}
};

const handleDelete = async (id: number) => {
	try {
		await useMessageBox().confirm('确认删除该相册吗？');
	} catch {
		return;
	}
	await deleteMediaAlbum(id);
	useMessage().success('删除成功');
	getDataList(false);
};

const openItems = async (row: any) => {
	currentAlbumId.value = row.id;
	itemsVisible.value = true;
	await loadAlbumItems();
	await loadFileOptions();
};

const loadAlbumItems = async () => {
	if (!currentAlbumId.value) return;
	const res = await fetchAlbumItems(currentAlbumId.value);
	albumItemsData.value = res.data || [];
};

const loadFileOptions = async () => {
	const res = await mediaFilePage({ current: 1, size: 200 });
	fileOptions.value = res.data?.records || [];
};

const onFileSelect = (rows: any[]) => {
	selectedFileIds.value = rows.map((row) => row.id);
};

const handleAddItems = async () => {
	if (!currentAlbumId.value || !selectedFileIds.value.length) {
		useMessage().warning('请选择图片');
		return;
	}
	await addAlbumItems(currentAlbumId.value, selectedFileIds.value);
	useMessage().success('添加成功');
	await loadAlbumItems();
};

const handleRemoveItem = async (fileId: number) => {
	if (!currentAlbumId.value) return;
	await removeAlbumItem(currentAlbumId.value, fileId);
	useMessage().success('移除成功');
	await loadAlbumItems();
};

const saveSort = async () => {
	if (!currentAlbumId.value) return;
	const payload = albumItemsData.value.map((row) => ({ id: row.id, sortNo: row.sortNo || 0 }));
	await sortAlbumItems(currentAlbumId.value, payload);
	useMessage().success('排序保存成功');
	await loadAlbumItems();
};
</script>
