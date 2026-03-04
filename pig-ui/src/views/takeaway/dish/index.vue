<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row class="ml10" v-show="showSearch">
				<el-form :inline="true" :model="state.queryForm" ref="queryRef" @keyup.enter="getDataList">
					<el-form-item label="商家用户ID" prop="merchantUserId">
						<el-input v-model="state.queryForm.merchantUserId" placeholder="请输入商家用户ID" style="max-width: 180px" clearable />
					</el-form-item>
					<el-form-item label="菜品名称" prop="dishName">
						<el-input v-model="state.queryForm.dishName" placeholder="请输入菜品名称" style="max-width: 180px" clearable />
					</el-form-item>
					<el-form-item label="上架状态" prop="saleStatus">
						<el-select v-model="state.queryForm.saleStatus" placeholder="请选择上架状态" style="max-width: 180px" clearable>
							<el-option label="下架" value="0" />
							<el-option label="上架" value="1" />
						</el-select>
					</el-form-item>
					<el-form-item>
						<el-button type="primary" icon="Search" @click="getDataList">查询</el-button>
						<el-button icon="Refresh" @click="resetQuery">重置</el-button>
					</el-form-item>
				</el-form>
			</el-row>
			<el-row>
				<div class="mb8" style="width: 100%">
					<el-button type="primary" class="ml10" icon="Plus" v-auth="'wm_dish_add'" @click="openAdd">菜品新增</el-button>
					<right-toolbar class="ml10" style="float: right; margin-right: 20px" v-model:showSearch="showSearch" @queryTable="getDataList"></right-toolbar>
				</div>
			</el-row>
			<el-table
				:data="state.dataList"
				v-loading="state.loading"
				border
				style="width: 100%"
				:cell-style="tableStyle.cellStyle"
				:header-cell-style="tableStyle.headerCellStyle"
			>
				<el-table-column label="ID" prop="id" width="190" show-overflow-tooltip />
				<el-table-column label="商家用户ID" prop="merchantUserId" width="150" />
				<el-table-column label="菜品名称" prop="dishName" min-width="160" show-overflow-tooltip />
				<el-table-column label="菜品描述" prop="dishDesc" min-width="180" show-overflow-tooltip />
				<el-table-column label="价格" prop="price" width="100" />
				<el-table-column label="库存" prop="stock" width="90" />
				<el-table-column label="上架状态" prop="saleStatus" width="110">
					<template #default="scope">
						<el-tag v-if="scope.row.saleStatus === '1'" type="success">上架</el-tag>
						<el-tag v-else type="info">下架</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="创建时间" prop="createTime" min-width="170" show-overflow-tooltip />
				<el-table-column label="操作" width="340" fixed="right">
					<template #default="scope">
						<el-button text type="primary" icon="EditPen" v-auth="'wm_dish_edit'" @click="openEdit(scope.row)">编辑</el-button>
						<el-button text type="primary" icon="Delete" v-auth="'wm_dish_del'" @click="onDelete(scope.row)">删除</el-button>
						<el-button text type="primary" icon="Top" v-auth="'wm_dish_sale_on'" @click="onSaleOn(scope.row)">上架</el-button>
						<el-button text type="primary" icon="Bottom" v-auth="'wm_dish_sale_off'" @click="onSaleOff(scope.row)">下架</el-button>
					</template>
				</el-table-column>
			</el-table>
			<pagination @size-change="sizeChangeHandle" @current-change="currentChangeHandle" v-bind="state.pagination" />
		</div>

		<el-dialog v-model="formDialogVisible" :title="dishForm.id ? '编辑菜品' : '新增菜品'" width="660px" destroy-on-close>
			<el-form :model="dishForm" :rules="dishRules" ref="dishFormRef" label-width="110px">
				<el-form-item label="商家用户ID" prop="merchantUserId">
					<el-input v-model="dishForm.merchantUserId" placeholder="请输入商家用户ID" />
				</el-form-item>
				<el-form-item label="菜品名称" prop="dishName">
					<el-input v-model="dishForm.dishName" placeholder="请输入菜品名称" />
				</el-form-item>
				<el-form-item label="菜品描述" prop="dishDesc">
					<el-input type="textarea" v-model="dishForm.dishDesc" :rows="3" placeholder="请输入菜品描述" />
				</el-form-item>
				<el-form-item label="价格" prop="price">
					<el-input v-model="dishForm.price" placeholder="请输入价格" />
				</el-form-item>
				<el-form-item label="库存" prop="stock">
					<el-input v-model="dishForm.stock" placeholder="请输入库存" />
				</el-form-item>
				<el-form-item label="上架状态" prop="saleStatus">
					<el-select v-model="dishForm.saleStatus" style="width: 100%">
						<el-option label="下架" value="0" />
						<el-option label="上架" value="1" />
					</el-select>
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="formDialogVisible = false">取消</el-button>
				<el-button type="primary" @click="submitDish">保存</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup lang="ts" name="takeawayDishIndex">
import { BasicTableProps, useTable } from '/@/hooks/table';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { FormInstance, FormRules } from 'element-plus';
import { addObj, delObj, pageList, putObj, saleOff, saleOn } from '/@/api/takeaway/dish';

const queryRef = ref();
const showSearch = ref(true);
const formDialogVisible = ref(false);
const dishFormRef = ref<FormInstance>();

const state: BasicTableProps = reactive<BasicTableProps>({
	queryForm: {},
	pageList,
	descs: ['create_time'],
});

const { getDataList, sizeChangeHandle, currentChangeHandle, tableStyle } = useTable(state);

const dishForm = reactive<any>({
	id: '',
	merchantUserId: '',
	dishName: '',
	dishDesc: '',
	price: '',
	stock: '',
	saleStatus: '1',
});

const dishRules: FormRules = {
	merchantUserId: [{ required: true, message: '请输入商家用户ID', trigger: 'blur' }],
	dishName: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
	price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
	stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
};

const resetQuery = () => {
	queryRef.value?.resetFields();
	getDataList();
};

const resetDishForm = () => {
	dishForm.id = '';
	dishForm.merchantUserId = '';
	dishForm.dishName = '';
	dishForm.dishDesc = '';
	dishForm.price = '';
	dishForm.stock = '';
	dishForm.saleStatus = '1';
};

const openAdd = () => {
	resetDishForm();
	formDialogVisible.value = true;
};

const openEdit = (row: any) => {
	dishForm.id = row.id;
	dishForm.merchantUserId = row.merchantUserId;
	dishForm.dishName = row.dishName;
	dishForm.dishDesc = row.dishDesc;
	dishForm.price = row.price;
	dishForm.stock = row.stock;
	dishForm.saleStatus = row.saleStatus;
	formDialogVisible.value = true;
};

const submitDish = async () => {
	await dishFormRef.value?.validate();
	if (dishForm.id) {
		await putObj({ ...dishForm });
		useMessage().success('菜品已更新');
	} else {
		await addObj({ ...dishForm });
		useMessage().success('菜品已新增');
	}
	formDialogVisible.value = false;
	getDataList(false);
};

const onDelete = async (row: any) => {
	try {
		await useMessageBox().confirm(`确认删除菜品【${row.dishName}】吗？`);
	} catch {
		return;
	}
	await delObj(row.id);
	useMessage().success('删除成功');
	getDataList(false);
};

const onSaleOn = async (row: any) => {
	await saleOn(row.id);
	useMessage().success('上架成功');
	getDataList(false);
};

const onSaleOff = async (row: any) => {
	await saleOff(row.id);
	useMessage().success('下架成功');
	getDataList(false);
};
</script>
