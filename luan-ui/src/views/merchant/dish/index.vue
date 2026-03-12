<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card>
				<template #header>
					<div class="card-header">
						<span>我的菜品</span>
						<el-button v-auth="'wm_merchant_dish_add'" type="primary" @click="openCreate">新增菜品</el-button>
					</div>
				</template>

				<el-form ref="queryRef" :inline="true" :model="state.queryForm" @keyup.enter="getDataList">
					<el-form-item label="菜品名称" prop="dishName">
						<el-input v-model="state.queryForm.dishName" placeholder="请输入菜品名称" clearable style="width: 220px" />
					</el-form-item>
					<el-form-item label="销售状态" prop="saleStatus">
						<el-select v-model="state.queryForm.saleStatus" placeholder="请选择状态" clearable style="width: 180px">
							<el-option label="上架" value="1" />
							<el-option label="下架" value="0" />
						</el-select>
					</el-form-item>
					<el-form-item>
						<el-button type="primary" icon="Search" @click="getDataList">查询</el-button>
						<el-button icon="Refresh" @click="resetQuery">重置</el-button>
					</el-form-item>
				</el-form>

				<el-table :data="state.dataList" v-loading="state.loading" border style="width: 100%" :cell-style="tableStyle.cellStyle" :header-cell-style="tableStyle.headerCellStyle">
					<el-table-column label="菜品图片" width="100">
						<template #default="scope">
							<el-image
								v-if="scope.row.dishImage"
								:src="resolveImageSrc(scope.row.dishImage)"
								fit="cover"
								:preview-src-list="[resolveImageSrc(scope.row.dishImage)]"
								preview-teleported
								class="dish-image"
							/>
							<span v-else class="no-image">未上传</span>
						</template>
					</el-table-column>
					<el-table-column prop="dishName" label="菜品名称" min-width="120" show-overflow-tooltip />
					<el-table-column prop="dishDesc" label="菜品描述" min-width="140" show-overflow-tooltip />
					<el-table-column prop="price" label="价格(元)" width="80" />
					<el-table-column prop="stock" label="库存" width="80" />
					<el-table-column label="销售状态" width="90">
						<template #default="scope">
							<el-tag v-if="scope.row.saleStatus === '1'" type="success">上架</el-tag>
							<el-tag v-else type="info">下架</el-tag>
						</template>
					</el-table-column>
					<el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
					<el-table-column label="操作" width="160" fixed="right">
						<template #default="scope">
							<el-button v-auth="'wm_merchant_dish_edit'" text type="primary" @click="openEdit(scope.row)">编辑</el-button>
							<el-button v-if="scope.row.saleStatus === '1'" v-auth="'wm_merchant_dish_sale_off'" text type="primary" @click="handleToggleSale(scope.row)">下架</el-button>
							<el-button v-else v-auth="'wm_merchant_dish_sale_on'" text type="primary" @click="handleToggleSale(scope.row)">上架</el-button>
							<el-button v-auth="'wm_merchant_dish_del'" text type="danger" @click="handleDelete(scope.row)">删除</el-button>
						</template>
					</el-table-column>
				</el-table>

				<pagination @current-change="currentChangeHandle" @size-change="sizeChangeHandle" v-bind="state.pagination" />
			</el-card>
		</div>

		<el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜品' : '新增菜品'" width="640px" destroy-on-close>
			<el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
				<el-form-item label="菜品图片" prop="dishImage">
					<UploadImg v-model="form.dishImage" :limit="1" :file-size="10" :is-show-tip="true" />
				</el-form-item>

				<el-form-item label="菜品名称" prop="dishName">
					<el-input v-model="form.dishName" maxlength="128" placeholder="请输入菜品名称" />
				</el-form-item>

				<el-form-item label="菜品描述" prop="dishDesc">
					<el-input v-model="form.dishDesc" maxlength="255" type="textarea" :rows="3" placeholder="请输入菜品描述" />
				</el-form-item>

				<el-row :gutter="12">
					<el-col :span="12">
						<el-form-item label="价格(元)" prop="price">
							<el-input-number v-model="form.price" :min="0" :max="999999" :precision="2" :step="0.1" controls-position="right" style="width: 100%" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="库存" prop="stock">
							<el-input-number v-model="form.stock" :min="0" :max="999999" :step="1" controls-position="right" style="width: 100%" />
						</el-form-item>
					</el-col>
				</el-row>

				<el-form-item label="销售状态" prop="saleStatus">
					<el-radio-group v-model="form.saleStatus">
						<el-radio label="1">上架</el-radio>
						<el-radio label="0">下架</el-radio>
					</el-radio-group>
				</el-form-item>
			</el-form>

			<template #footer>
				<el-button @click="dialogVisible = false">取消</el-button>
				<el-button v-if="form.id" v-auth="'wm_merchant_dish_edit'" type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
				<el-button v-else v-auth="'wm_merchant_dish_add'" type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup lang="ts" name="merchantDish">
import { useTable, type BasicTableProps } from '/@/hooks/table';
import { addObj, delObj, pageList, putObj, saleOff, saleOn } from '/@/api/takeaway/dish';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { useUserInfo } from '/@/stores/userInfo';
import { resolveApiResourceUrl } from '/@/utils/url';

interface DishForm {
	id?: number;
	merchantUserId?: number;
	dishImage: string;
	dishName: string;
	dishDesc: string;
	price: number;
	stock: number;
	saleStatus: string;
}

const queryRef = ref();
const formRef = ref();
const dialogVisible = ref(false);
const submitting = ref(false);

const form = reactive<DishForm>({
	id: undefined,
	merchantUserId: undefined,
	dishImage: '',
	dishName: '',
	dishDesc: '',
	price: 0,
	stock: 0,
	saleStatus: '1',
});

const rules = reactive({
	dishName: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
	price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
	stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
	saleStatus: [{ required: true, message: '请选择销售状态', trigger: 'change' }],
});

const state: BasicTableProps = reactive<BasicTableProps>({
	createdIsNeed: false,
	queryForm: {
		merchantUserId: undefined,
		dishName: '',
		saleStatus: '',
	},
	pageList,
});

const { getDataList, currentChangeHandle, sizeChangeHandle, tableStyle } = useTable(state);

const resolveImageSrc = (image?: string) => {
	return resolveApiResourceUrl(image);
};

const currentMerchantUserId = computed(() => {
	return useUserInfo().userInfos?.user?.userId as number | undefined;
});

const resetForm = () => {
	form.id = undefined;
	form.merchantUserId = currentMerchantUserId.value;
	form.dishImage = '';
	form.dishName = '';
	form.dishDesc = '';
	form.price = 0;
	form.stock = 0;
	form.saleStatus = '1';
};

const resetQuery = () => {
	queryRef.value?.resetFields();
	state.queryForm.merchantUserId = currentMerchantUserId.value;
	getDataList();
};

const openCreate = () => {
	resetForm();
	dialogVisible.value = true;
};

const openEdit = (row: any) => {
	form.id = row.id;
	form.merchantUserId = row.merchantUserId;
	form.dishImage = row.dishImage || '';
	form.dishName = row.dishName || '';
	form.dishDesc = row.dishDesc || '';
	form.price = Number(row.price || 0);
	form.stock = Number(row.stock || 0);
	form.saleStatus = row.saleStatus || '1';
	dialogVisible.value = true;
};

const handleSubmit = async () => {
	await formRef.value.validate(async (valid: boolean) => {
		if (!valid) return;

		submitting.value = true;
		try {
			const payload: any = {
				merchantUserId: form.merchantUserId || currentMerchantUserId.value,
				dishImage: form.dishImage,
				dishName: form.dishName,
				dishDesc: form.dishDesc,
				price: form.price,
				stock: form.stock,
				saleStatus: form.saleStatus,
			};

			if (form.id) {
				payload.id = form.id;
				await putObj(payload);
				useMessage().success('菜品修改成功');
			} else {
				await addObj(payload);
				useMessage().success('菜品新增成功');
			}

			dialogVisible.value = false;
			getDataList(false);
		} catch (error: any) {
			useMessage().error(error?.msg || error?.response?.data?.msg || '提交失败');
		} finally {
			submitting.value = false;
		}
	});
};

const handleDelete = async (row: any) => {
	try {
		await useMessageBox().confirm(`确认删除菜品「${row.dishName}」吗？`);
	} catch {
		return;
	}

	try {
		await delObj(row.id);
		useMessage().success('删除成功');
		getDataList(false);
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '删除失败');
	}
};

const handleToggleSale = async (row: any) => {
	const isOn = row.saleStatus === '1';
	try {
		if (isOn) {
			await saleOff(row.id);
			useMessage().success('下架成功');
		} else {
			await saleOn(row.id);
			useMessage().success('上架成功');
		}
		getDataList(false);
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '状态更新失败');
	}
};

onMounted(() => {
	state.queryForm.merchantUserId = currentMerchantUserId.value;
	getDataList();
});
</script>

<style scoped>
.card-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.dish-image {
	width: 48px;
	height: 48px;
	border-radius: 6px;
	border: 1px solid var(--el-border-color-light);
}

.no-image {
	font-size: 12px;
	color: var(--el-text-color-secondary);
}
</style>
