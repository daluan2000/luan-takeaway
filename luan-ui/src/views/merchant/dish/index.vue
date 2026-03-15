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
							<el-option v-for="item in saleStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
					<el-table-column prop="dishName" label="菜品名称" min-width="100" show-overflow-tooltip />
					<el-table-column prop="dishDesc" label="菜品描述" min-width="100" show-overflow-tooltip />
					<el-table-column prop="price" label="价格(元)" width="80" />
					<el-table-column prop="stock" label="库存" width="80" />
					<el-table-column label="销售状态" width="90">
						<template #default="scope">
							<el-tag :type="scope.row.saleStatus === '1' ? 'success' : 'info'">{{ getSaleStatusLabel(scope.row.saleStatus) }}</el-tag>
						</template>
					</el-table-column>
					<el-table-column prop="createTime" label="创建时间" min-width="160" show-overflow-tooltip />
					<el-table-column label="知识库操作" width="170">
						<template #default="scope">
							<el-button text type="primary" :disabled="isKnowledgeActionDisabled(scope.row)" @click="openKnowledgeDialog(scope.row)">查看/编辑</el-button>
							<el-button text type="success" :loading="isKnowledgeGenerating(scope.row)" :disabled="isKnowledgeActionDisabled(scope.row)" @click="handleGenerateKnowledge(scope.row)">AI生成</el-button>
						</template>
					</el-table-column>
					<el-table-column label="操作" width="140" fixed="right">
						<template #default="scope">
							<el-button v-auth="'wm_merchant_dish_edit'" text type="primary" :disabled="isKnowledgeGenerating(scope.row)" @click="openEdit(scope.row)">编辑</el-button>
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
						<el-radio v-for="item in saleStatusOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
					</el-radio-group>
				</el-form-item>

				<el-form-item label="知识文档">
					<el-checkbox v-model="form.autoGenerateKnowledge">保存后自动生成知识文档</el-checkbox>
				</el-form-item>
			</el-form>

			<template #footer>
				<el-button @click="dialogVisible = false">取消</el-button>
				<el-button v-if="form.id" v-auth="'wm_merchant_dish_edit'" type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
				<el-button v-else v-auth="'wm_merchant_dish_add'" type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
			</template>
		</el-dialog>

		<el-dialog v-model="knowledgeDialogVisible" class="knowledge-dialog" title="菜品知识文档" width="860px" destroy-on-close>
			<el-form ref="knowledgeFormRef" class="knowledge-form" :model="knowledgeForm" :rules="knowledgeRules" label-width="120px">
				<el-row :gutter="12">
					<el-col :span="12">
						<el-form-item label="主类别" prop="category">
							<el-select v-model="knowledgeForm.category" filterable clearable>
								<el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
							</el-select>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="分量" prop="portionSize">
							<el-select v-model="knowledgeForm.portionSize" clearable>
								<el-option v-for="item in portionSizeOptions" :key="item" :label="item" :value="item" />
							</el-select>
						</el-form-item>
					</el-col>
				</el-row>

				<el-row :gutter="12">
					<el-col :span="6"><el-form-item label="辣"><el-switch v-model="knowledgeForm.spicy" /></el-form-item></el-col>
					<el-col :span="6"><el-form-item label="清淡"><el-switch v-model="knowledgeForm.lightTaste" /></el-form-item></el-col>
					<el-col :span="6"><el-form-item label="油腻"><el-switch v-model="knowledgeForm.oily" /></el-form-item></el-col>
					<el-col :span="6"><el-form-item label="素食"><el-switch v-model="knowledgeForm.vegetarian" /></el-form-item></el-col>
				</el-row>

				<el-row :gutter="12">
					<el-col :span="8">
						<el-form-item label="辣度" prop="spicyLevel">
							<div class="nutrient-input-wrap">
								<el-input-number v-model="knowledgeForm.spicyLevel" :min="0" :max="5" controls-position="right" class="nutrient-input" />
								<span class="nutrient-unit">级</span>
							</div>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="热量" prop="calories">
							<div class="nutrient-input-wrap">
								<el-input-number v-model="knowledgeForm.calories" :min="0" :max="3000" controls-position="right" class="nutrient-input" />
								<span class="nutrient-unit">kcal</span>
							</div>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="蛋白质" prop="protein">
							<div class="nutrient-input-wrap">
								<el-input-number v-model="knowledgeForm.protein" :min="0" :max="300" controls-position="right" class="nutrient-input" />
								<span class="nutrient-unit">g</span>
							</div>
						</el-form-item>
					</el-col>
				</el-row>

				<el-row :gutter="12">
					<el-col :span="8">
						<el-form-item label="脂肪" prop="fat">
							<div class="nutrient-input-wrap">
								<el-input-number v-model="knowledgeForm.fat" :min="0" :max="300" controls-position="right" class="nutrient-input" />
								<span class="nutrient-unit">g</span>
							</div>
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="碳水化合物" prop="carbohydrate">
							<div class="nutrient-input-wrap">
								<el-input-number v-model="knowledgeForm.carbohydrate" :min="0" :max="500" controls-position="right" class="nutrient-input" />
								<span class="nutrient-unit">g</span>
							</div>
						</el-form-item>
					</el-col>
				</el-row>

				<el-form-item label="适用餐段" prop="mealTime">
					<el-select v-model="knowledgeForm.mealTime" class="knowledge-multi-select" multiple>
						<el-option v-for="item in mealTimeOptions" :key="item" :label="item" :value="item" />
					</el-select>
				</el-form-item>

				<el-form-item label="标签" prop="tags">
					<el-select v-model="knowledgeForm.tags" class="knowledge-multi-select" multiple>
						<el-option v-for="item in semanticTagOptions" :key="item" :label="item" :value="item" />
					</el-select>
				</el-form-item>

				<el-form-item label="推荐场景" prop="suitableScenes">
					<el-select v-model="knowledgeForm.suitableScenes" class="knowledge-multi-select" multiple>
						<el-option v-for="item in suitableSceneOptions" :key="item" :label="item" :value="item" />
					</el-select>
				</el-form-item>

				<el-form-item label="避免场景" prop="avoidScenes">
					<el-select v-model="knowledgeForm.avoidScenes" class="knowledge-multi-select" multiple>
						<el-option v-for="item in avoidSceneOptions" :key="item" :label="item" :value="item" />
					</el-select>
				</el-form-item>

				<el-form-item label="适合人群" prop="suitablePeople">
					<el-select v-model="knowledgeForm.suitablePeople" class="knowledge-multi-select" multiple>
						<el-option v-for="item in suitablePeopleOptions" :key="item" :label="item" :value="item" />
					</el-select>
				</el-form-item>

				<el-form-item label="风味描述" prop="flavorDescription"><el-input v-model="knowledgeForm.flavorDescription" type="textarea" :rows="2" maxlength="255" /></el-form-item>
				<el-form-item label="知识摘要" prop="llmSummary"><el-input v-model="knowledgeForm.llmSummary" type="textarea" :rows="3" maxlength="1000" /></el-form-item>
				<el-form-item label="推荐理由" prop="recommendationReason"><el-input v-model="knowledgeForm.recommendationReason" type="textarea" :rows="3" maxlength="1000" /></el-form-item>
				<el-form-item label="Embedding文本" prop="embeddingText"><el-input v-model="knowledgeForm.embeddingText" type="textarea" :rows="3" maxlength="1500" /></el-form-item>
			</el-form>

			<template #footer>
				<el-button @click="knowledgeDialogVisible = false">取消</el-button>
				<el-button type="primary" :loading="knowledgeSaving" @click="saveKnowledgeDoc">保存知识文档</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup lang="ts" name="merchantDish">
import { useTable, type BasicTableProps } from '/@/hooks/table';
import { addObj, delObj, pageList, putObj, saleOff, saleOn, getKnowledgeDoc, updateKnowledgeDoc, generateKnowledgeDoc } from '/@/api/takeaway/dish';
import { useMessage, useMessageBox } from '/@/hooks/message';
import { useUserInfo } from '/@/stores/userInfo';
import { resolveApiResourceUrl } from '/@/utils/url';
import { useDict } from '/@/hooks/dict';

interface DishForm {
	id?: string | number;
	merchantUserId?: string | number;
	dishImage: string;
	dishName: string;
	dishDesc: string;
	price: number;
	stock: number;
	saleStatus: string;
	autoGenerateKnowledge: boolean;
}

interface DishKnowledgeForm {
	dishId?: string | number;
	category?: string;
	spicy?: boolean;
	spicyLevel?: number;
	lightTaste?: boolean;
	oily?: boolean;
	soupBased?: boolean;
	vegetarian?: boolean;
	calories?: number;
	protein?: number;
	fat?: number;
	carbohydrate?: number;
	mealTime: string[];
	portionSize?: string;
	tags: string[];
	suitableScenes: string[];
	avoidScenes: string[];
	suitablePeople: string[];
	flavorDescription?: string;
	llmSummary?: string;
	recommendationReason?: string;
	embeddingText?: string;
}

const queryRef = ref();
const formRef = ref();
const knowledgeFormRef = ref();
const dialogVisible = ref(false);
const knowledgeDialogVisible = ref(false);
const submitting = ref(false);
const knowledgeSaving = ref(false);
const generatingKnowledgeMap = reactive<Record<string, boolean>>({});
const knowledgeSyncLockedDishMap = reactive<Record<string, boolean>>({});
const knowledgeSyncLockedSignatureMap = reactive<Record<string, boolean>>({});

const { takeaway_dish_sale_status } = useDict('takeaway_dish_sale_status');

const form = reactive<DishForm>({
	id: undefined,
	merchantUserId: undefined,
	dishImage: '',
	dishName: '',
	dishDesc: '',
	price: 0,
	stock: 0,
	saleStatus: '1',
	autoGenerateKnowledge: true,
});

const editingDishId = ref<string | number | undefined>(undefined);
const knowledgeForm = reactive<DishKnowledgeForm>({
	dishId: undefined,
	mealTime: [],
	tags: [],
	suitableScenes: [],
	avoidScenes: [],
	suitablePeople: [],
});

const categoryOptions = ['rice', 'noodle', 'porridge', 'hotpot', 'drink', 'snack', 'dessert'];
const mealTimeOptions = ['breakfast', 'lunch', 'dinner', 'midnight'];
const portionSizeOptions = ['small', 'medium', 'large'];
const semanticTagOptions = ['低脂', '高蛋白', '高碳水', '高纤维', '清淡', '重口味', '暖胃', '解腻', '低糖', '高热量', '易消化', '饱腹感强'];
const suitableSceneOptions = ['减脂', '健身恢复', '胃不舒服', '工作午餐', '夜宵', '两人分享', '聚餐', '快速解决一餐', '补充能量', '天气寒冷'];
const avoidSceneOptions = ['睡前', '空腹', '肠胃敏感', '上火', '减脂期间', '运动前'];
const suitablePeopleOptions = ['学生', '办公室', '健身', '老人', '儿童', '熬夜人群', '重体力劳动'];

const rules = reactive({
	dishName: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
	price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
	stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
	saleStatus: [{ required: true, message: '请选择销售状态', trigger: 'change' }],
});

const requireNumber = (label: string) => {
	return {
		validator: (_rule: any, value: unknown, callback: (error?: Error) => void) => {
			if (value === null || value === undefined || value === '') {
				callback(new Error(`请输入${label}`));
				return;
			}
			callback();
		},
		trigger: ['blur', 'change'],
	};
};

const requireArray = (label: string) => {
	return {
		validator: (_rule: any, value: unknown, callback: (error?: Error) => void) => {
			if (!Array.isArray(value) || value.length === 0) {
				callback(new Error(`请选择${label}`));
				return;
			}
			callback();
		},
		trigger: 'change',
	};
};

const requireText = (label: string) => {
	return {
		validator: (_rule: any, value: unknown, callback: (error?: Error) => void) => {
			if (typeof value !== 'string' || !value.trim()) {
				callback(new Error(`请输入${label}`));
				return;
			}
			callback();
		},
		trigger: 'blur',
	};
};

const knowledgeRules = reactive({
	category: [{ required: true, message: '请选择主类别', trigger: 'change' }],
	portionSize: [{ required: true, message: '请选择分量', trigger: 'change' }],
	spicyLevel: [requireNumber('辣度')],
	calories: [requireNumber('热量')],
	protein: [requireNumber('蛋白质')],
	fat: [requireNumber('脂肪')],
	carbohydrate: [requireNumber('碳水化合物')],
	mealTime: [requireArray('适用餐段')],
	tags: [requireArray('标签')],
	suitableScenes: [requireArray('推荐场景')],
	avoidScenes: [requireArray('避免场景')],
	suitablePeople: [requireArray('适合人群')],
	flavorDescription: [requireText('风味描述')],
	llmSummary: [requireText('知识摘要')],
	recommendationReason: [requireText('推荐理由')],
	embeddingText: [requireText('Embedding文本')],
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

const saleStatusOptions = computed(() => takeaway_dish_sale_status.value || []);

const getSaleStatusLabel = (status: string) => {
	const target = saleStatusOptions.value.find((item: any) => String(item.value) === String(status));
	return target?.label || status || '-';
};

const resolveImageSrc = (image?: string) => {
	return resolveApiResourceUrl(image);
};

const getDishIdKey = (dishId: unknown) => {
	if (dishId === null || dishId === undefined || dishId === '') {
		return '';
	}
	return String(dishId);
};

const getKnowledgeSyncSignature = (dish: any) => {
	if (!dish) {
		return '';
	}
	const merchantUserId = getDishIdKey(dish.merchantUserId);
	const dishName = String(dish.dishName || '').trim();
	if (!merchantUserId || !dishName) {
		return '';
	}
	const price = String(dish.price ?? '');
	const stock = String(dish.stock ?? '');
	return `${merchantUserId}::${dishName}::${price}::${stock}`;
};

const setKnowledgeSyncLocked = (dish: any, locked: boolean) => {
	const key = getDishIdKey(dish?.id);
	if (key) {
		knowledgeSyncLockedDishMap[key] = locked;
	}
	const signature = getKnowledgeSyncSignature(dish);
	if (signature) {
		knowledgeSyncLockedSignatureMap[signature] = locked;
	}
};

const isKnowledgeSyncLocked = (row: any) => {
	const key = getDishIdKey(row?.id);
	if (key && knowledgeSyncLockedDishMap[key]) {
		return true;
	}
	const signature = getKnowledgeSyncSignature(row);
	return !!(signature && knowledgeSyncLockedSignatureMap[signature]);
};

const isKnowledgeGenerating = (row: any) => {
	const key = getDishIdKey(row?.id);
	return !!(key && generatingKnowledgeMap[key]);
};

const isKnowledgeActionDisabled = (row: any) => {
	return isKnowledgeGenerating(row) || isKnowledgeSyncLocked(row);
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
	form.autoGenerateKnowledge = true;
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
	form.autoGenerateKnowledge = false;
	dialogVisible.value = true;
};

const buildLocalDishRow = (dish: any) => {
	const now = new Date();
	const fallbackId = `temp-${now.getTime()}`;
	return {
		id: dish?.id ?? fallbackId,
		merchantUserId: dish?.merchantUserId,
		dishImage: dish?.dishImage || '',
		dishName: dish?.dishName || '',
		dishDesc: dish?.dishDesc || '',
		price: dish?.price ?? 0,
		stock: dish?.stock ?? 0,
		saleStatus: dish?.saleStatus || '1',
		createTime: dish?.createTime || now.toLocaleString('zh-CN', { hour12: false }),
	};
};

const upsertLocalDishRow = (dish: any) => {
	const nextRow = buildLocalDishRow(dish);
	const targetKey = getDishIdKey(nextRow.id);
	const sourceList = Array.isArray((state as any).dataList) ? ((state as any).dataList as any[]) : [];
	let replaced = false;
	const nextList = sourceList.map((item) => {
		if (targetKey && getDishIdKey(item?.id) === targetKey) {
			replaced = true;
			return {
				...item,
				...nextRow,
			};
		}
		return item;
	});
	(state as any).dataList = replaced ? nextList : [nextRow, ...nextList];
	if (!replaced && (state as any).pagination) {
		const total = Number((state as any).pagination.total || 0);
		(state as any).pagination.total = total + 1;
	}
};

const handleSubmit = async () => {
	await formRef.value.validate(async (valid: boolean) => {
		if (!valid) return;

		submitting.value = true;
		try {
			const sourceDish = {
				id: form.id,
				merchantUserId: form.merchantUserId || currentMerchantUserId.value,
				dishImage: form.dishImage,
				dishName: form.dishName,
				dishDesc: form.dishDesc,
				price: form.price,
				stock: form.stock,
				saleStatus: form.saleStatus,
				createTime: undefined,
			};
			const editingKey = getDishIdKey(form.id);
			if (editingKey) {
				const currentRow = ((state as any).dataList || []).find((item: any) => getDishIdKey(item?.id) === editingKey);
				sourceDish.createTime = currentRow?.createTime;
			}

			const payload: any = {
				merchantUserId: form.merchantUserId || currentMerchantUserId.value,
				dishImage: form.dishImage,
				dishName: form.dishName,
				dishDesc: form.dishDesc,
				price: form.price,
				stock: form.stock,
				saleStatus: form.saleStatus,
				autoGenerateKnowledge: form.autoGenerateKnowledge,
			};
			let savedDishId: string | number | undefined = form.id;

			if (form.id) {
				payload.id = form.id;
				await putObj(payload);
				dialogVisible.value = false;
				sourceDish.id = form.id;
				upsertLocalDishRow(sourceDish);
				setKnowledgeSyncLocked(sourceDish, form.autoGenerateKnowledge);
				useMessage().success('菜品修改成功');
			} else {
				const res: any = await addObj(payload);
				savedDishId = res?.data?.id || res?.data?.dishId || res?.data?.dish?.id;
				dialogVisible.value = false;
				sourceDish.id = savedDishId;
				upsertLocalDishRow(sourceDish);
				setKnowledgeSyncLocked(sourceDish, form.autoGenerateKnowledge);
				useMessage().success('菜品新增成功');
			}
			if (savedDishId) {
				const key = getDishIdKey(savedDishId);
				if (key && form.autoGenerateKnowledge) {
					knowledgeSyncLockedDishMap[key] = true;
				}
			}
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

const resetKnowledgeForm = () => {
	knowledgeForm.dishId = undefined;
	knowledgeForm.category = undefined;
	knowledgeForm.spicy = false;
	knowledgeForm.spicyLevel = 0;
	knowledgeForm.lightTaste = false;
	knowledgeForm.oily = false;
	knowledgeForm.soupBased = false;
	knowledgeForm.vegetarian = false;
	knowledgeForm.calories = undefined;
	knowledgeForm.protein = undefined;
	knowledgeForm.fat = undefined;
	knowledgeForm.carbohydrate = undefined;
	knowledgeForm.mealTime = [];
	knowledgeForm.portionSize = undefined;
	knowledgeForm.tags = [];
	knowledgeForm.suitableScenes = [];
	knowledgeForm.avoidScenes = [];
	knowledgeForm.suitablePeople = [];
	knowledgeForm.flavorDescription = '';
	knowledgeForm.llmSummary = '';
	knowledgeForm.recommendationReason = '';
	knowledgeForm.embeddingText = '';
};

const openKnowledgeDialog = async (row: any) => {
	const dishId = row?.id;
	if (!dishId) {
		useMessage().warning('菜品ID缺失');
		return;
	}
	if (isKnowledgeGenerating(row)) {
		useMessage().warning('知识文档正在生成中，请稍候');
		return;
	}
	if (isKnowledgeSyncLocked(row)) {
		useMessage().warning('该菜品已选择同步知识文档，当前不可操作');
		return;
	}
	editingDishId.value = dishId;
	resetKnowledgeForm();
	knowledgeForm.dishId = dishId;
	knowledgeDialogVisible.value = true;
	try {
		const res = await getKnowledgeDoc(dishId);
		const data = res?.data;
		if (!data) {
			useMessage().warning('知识文档为空，编辑保存后自动新增');
			return;
		}
		Object.assign(knowledgeForm, {
			dishId,
			category: data.category,
			spicy: data.spicy,
			spicyLevel: data.spicyLevel,
			lightTaste: data.lightTaste,
			oily: data.oily,
			soupBased: data.soupBased,
			vegetarian: data.vegetarian,
			calories: data.calories,
			protein: data.protein,
			fat: data.fat,
			carbohydrate: data.carbohydrate,
			mealTime: Array.isArray(data.mealTime) ? data.mealTime : [],
			portionSize: data.portionSize,
			tags: Array.isArray(data.tags) ? data.tags : [],
			suitableScenes: Array.isArray(data.suitableScenes) ? data.suitableScenes : [],
			avoidScenes: Array.isArray(data.avoidScenes) ? data.avoidScenes : [],
			suitablePeople: Array.isArray(data.suitablePeople) ? data.suitablePeople : [],
			flavorDescription: data.flavorDescription || '',
			llmSummary: data.llmSummary || '',
			recommendationReason: data.recommendationReason || '',
			embeddingText: data.embeddingText || '',
		});
	} catch (error: any) {
		useMessage().warning(error?.msg || error?.response?.data?.msg || '未读取到知识文档，可直接编辑后保存');
	}
};

const handleGenerateKnowledge = async (row: any) => {
	const dishId = row?.id;
	if (!dishId) {
		useMessage().warning('菜品ID缺失');
		return;
	}
	const key = getDishIdKey(dishId);
	if (!key) {
		useMessage().warning('菜品ID缺失');
		return;
	}
	if (isKnowledgeSyncLocked(row)) {
		useMessage().warning('该菜品已选择同步知识文档，当前不可操作');
		return;
	}
	if (generatingKnowledgeMap[key]) {
		return;
	}
	generatingKnowledgeMap[key] = true;
	try {
		const res = await generateKnowledgeDoc(dishId);
		if (!res?.data) {
			useMessage().warning('知识文档生成完成，但未返回文档内容');
			return;
		}
		useMessage().success('知识文档生成成功');
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '触发生成失败');
	} finally {
		generatingKnowledgeMap[key] = false;
	}
};

const saveKnowledgeDoc = async () => {
	if (!editingDishId.value) {
		useMessage().warning('未识别到菜品ID');
		return;
	}
	try {
		await knowledgeFormRef.value?.validate();
	} catch {
		useMessage().warning('请先完善所有知识文档输入项');
		return;
	}
	knowledgeSaving.value = true;
	try {
		const payload: any = {
			category: knowledgeForm.category,
			spicy: knowledgeForm.spicy,
			spicyLevel: knowledgeForm.spicyLevel,
			lightTaste: knowledgeForm.lightTaste,
			oily: knowledgeForm.oily,
			soupBased: knowledgeForm.soupBased,
			vegetarian: knowledgeForm.vegetarian,
			calories: knowledgeForm.calories,
			protein: knowledgeForm.protein,
			fat: knowledgeForm.fat,
			carbohydrate: knowledgeForm.carbohydrate,
			mealTime: knowledgeForm.mealTime,
			portionSize: knowledgeForm.portionSize,
			tags: knowledgeForm.tags,
			suitableScenes: knowledgeForm.suitableScenes,
			avoidScenes: knowledgeForm.avoidScenes,
			suitablePeople: knowledgeForm.suitablePeople,
			flavorDescription: knowledgeForm.flavorDescription,
			llmSummary: knowledgeForm.llmSummary,
			recommendationReason: knowledgeForm.recommendationReason,
			embeddingText: knowledgeForm.embeddingText,
		};
		await updateKnowledgeDoc(editingDishId.value, payload);
		knowledgeDialogVisible.value = false;
		useMessage().success('知识文档保存成功');
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '知识文档保存失败');
	} finally {
		knowledgeSaving.value = false;
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

.nutrient-input-wrap {
	display: flex;
	align-items: center;
	gap: 8px;
	width: 100%;
}

.nutrient-input {
	flex: 1;
	min-width: 0;
}

.nutrient-unit {
	min-width: 34px;
	font-size: 12px;
	color: var(--el-text-color-secondary);
	white-space: nowrap;
}

.knowledge-multi-select {
	width: 100%;
}

.knowledge-dialog :deep(.el-select__selection) {
	align-items: flex-start;
	max-height: 86px;
	overflow-y: auto;
}

.knowledge-form :deep(.el-form-item) {
	margin-bottom: 18px;
}

.knowledge-form :deep(.el-row) {
	margin-bottom: 4px;
}

.knowledge-dialog :deep(.el-select__selected-item) {
	max-width: 100%;
}

.knowledge-dialog :deep(.el-tag__content) {
	max-width: none;
}
</style>
