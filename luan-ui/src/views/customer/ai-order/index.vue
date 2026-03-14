<template>
	<div class="layout-padding ai-order-page">
		<div class="layout-padding-auto layout-padding-view">
			<el-row :gutter="16" class="page-row">
				<el-col :xs="24" :lg="8">
					<el-card v-loading="contextLoading" class="input-card">
						<template #header>
							<div class="card-header">
								<div>
									<div class="title-main">智能点餐助手</div>
									<div class="title-sub">先输入需求，再按推荐结果直接下单</div>
								</div>
								<el-button link type="primary" @click="loadContext">刷新</el-button>
							</div>
						</template>

						<el-alert
							v-if="!defaultAddressId"
							title="未检测到默认地址，请先在客户信息里设置默认地址，否则无法下单"
							type="warning"
							show-icon
							:closable="false"
							class="mb16"
						/>

						<el-alert
							v-else-if="!currentCity"
							title="未识别到当前市，无法加载商家数据"
							type="warning"
							show-icon
							:closable="false"
							class="mb16"
						/>

						<div class="city-tip" v-if="currentCity">
							当前城市：<el-tag size="small">{{ currentCity }}</el-tag>
							<span class="merchant-count">营业商家 {{ merchantList.length }} 家</span>
						</div>

						<el-form label-position="top" class="input-form">
							<el-form-item label="推荐条数（1-5）">
								<el-input-number v-model="queryForm.limit" :min="1" :max="5" :step="1" controls-position="right" />
							</el-form-item>
							<el-form-item label="需求描述">
								<el-input
									v-model="queryForm.message"
									type="textarea"
									:rows="6"
									maxlength="300"
									show-word-limit
									resize="none"
									placeholder="例如：30元以内、不辣、两个人吃"
								/>
							</el-form-item>
						</el-form>

						<div class="input-actions">
							<el-button type="primary" :loading="recommending" @click="handleRecommend">获取推荐</el-button>
							<el-button :disabled="!hasResult" @click="clearResult">清空结果</el-button>
						</div>
					</el-card>
				</el-col>

				<el-col :xs="24" :lg="16">
					<el-card class="result-card" v-loading="recommending">
						<template #header>
							<div class="card-header">
								<div>推荐结果</div>
								<div class="header-tags" v-if="hasResult">
									<el-tag :type="decisionPathTagType" size="small">{{ decisionPathLabel }}</el-tag>
									<el-tag size="small" effect="plain">推荐 {{ recommendations.length }} 道</el-tag>
								</div>
							</div>
						</template>

						<el-empty v-if="!hasResult" description="输入需求后点击获取推荐" />

						<template v-else>
							<el-alert :title="summary || '推荐已生成'" type="success" :closable="false" show-icon class="mb16" />

							<el-empty v-if="!recommendedMerchantList.length" description="当前推荐未命中可展示商家" :image-size="90" />

							<el-collapse v-else>
								<el-collapse-item
									v-for="merchant in recommendedMerchantList"
									:key="merchant.id || merchant.userId"
									:name="String(merchant.id || merchant.userId)"
								>
									<template #title>
										<div class="merchant-title">
											<span class="merchant-name">{{ merchant.merchantName || `商家#${merchant.userId}` }}</span>
											<span class="merchant-city">{{ currentCity }}</span>
											<span class="merchant-address">{{ getMerchantAddress(merchant) || '-' }}</span>
										</div>
									</template>

									<div class="dish-grid">
										<el-card
											v-for="dish in dishMap[normalizeId(merchant.userId) || ''] || []"
											:key="dish.id || dish.dishName"
											shadow="hover"
											class="dish-card"
											:class="{ 'is-recommended': isRecommendedDish(merchant.userId, dish.id) }"
										>
											<div class="dish-image-wrapper">
												<el-image
													v-if="dish.dishImage"
													:src="resolveDishImageSrc(dish.dishImage)"
													fit="cover"
													:preview-src-list="[resolveDishImageSrc(dish.dishImage)]"
													preview-teleported
													class="dish-image"
												/>
												<div v-else class="dish-image-placeholder">图片不存在</div>
											</div>

											<div class="dish-content">
												<div class="dish-title-row">
													<div class="dish-name" :title="dish.dishName || '-'">{{ dish.dishName || '-' }}</div>
													<el-tooltip
														v-if="isRecommendedDish(merchant.userId, dish.id)"
														:content="getRecommendedReason(merchant.userId, dish.id)"
														placement="top"
													>
														<el-tag type="danger" size="small">AI推荐</el-tag>
													</el-tooltip>
												</div>
												<div class="dish-desc" :title="dish.dishDesc || '-'">{{ dish.dishDesc || '-' }}</div>
												<div class="dish-meta">
													<span class="dish-price">￥{{ formatMoney(dish.price) }}</span>
													<span class="dish-stock">库存 {{ Number(dish.stock || 0) }}</span>
												</div>
												<el-input-number
													:model-value="getDishQuantity(merchant.userId, dish.id)"
													:min="0"
													:max="Number(dish.stock || 99)"
													controls-position="right"
													class="dish-quantity"
													@update:model-value="updateDishQuantity(merchant.userId, dish.id, $event)"
												/>
											</div>
										</el-card>

										<div class="merchant-remark-panel">
											<div class="merchant-remark-title">订单备注</div>
											<el-input
												v-model="remarkMap[normalizeId(merchant.userId) || '']"
												type="textarea"
												:rows="2"
												maxlength="120"
												show-word-limit
												resize="none"
												placeholder="例如：少辣、不要香菜、请尽快配送"
											/>
										</div>
									</div>

									<div class="merchant-actions">
										<div class="selected-total">
											已选金额：<span class="selected-total-amount">￥{{ getSelectedTotalAmount(merchant.userId) }}</span>
										</div>
										<el-button
											v-auth="'wm_customer_order_create'"
											type="primary"
											size="small"
											:loading="submittingMap[normalizeId(merchant.userId) || '']"
											@click.stop="submitOrder(merchant)"
										>
											下单支付
										</el-button>
									</div>
								</el-collapse-item>
							</el-collapse>
						</template>
					</el-card>
				</el-col>
			</el-row>
		</div>
	</div>
</template>

<script setup lang="ts" name="customerAiOrderIndex">
import { recommendAiOrder, type AiAssistantRequestPayload } from '/@/api/takeaway/ai';
import { currentCustomer } from '/@/api/takeaway/customer';
import { listAddress } from '/@/api/takeaway/address';
import { listMerchantByRegion } from '/@/api/takeaway/merchant';
import { pageList as pageDish } from '/@/api/takeaway/dish';
import { createOrder } from '/@/api/takeaway/order';
import { useMessage } from '/@/hooks/message';
import { useUserInfo } from '/@/stores/userInfo';
import { useDict } from '/@/hooks/dict';
import { resolveApiResourceUrl } from '/@/utils/url';

interface AddressItem {
	id?: string | number;
	province?: string;
	city?: string;
	district?: string;
	detailAddress?: string;
}

interface MerchantItem {
	id?: string | number;
	userId?: string | number;
	merchantName?: string;
	businessStatus?: string;
	address?: AddressItem;
	province?: string;
	city?: string;
	district?: string;
	detailAddress?: string;
}

interface DishItem {
	id?: string | number;
	merchantUserId?: string | number;
	dishName?: string;
	dishDesc?: string;
	dishImage?: string;
	price?: string | number;
	stock?: string | number;
	saleStatus?: string | number;
}

interface IntentView {
	route?: string;
	originalQuery?: string;
	category?: string;
	priceMax?: number;
	spicy?: boolean;
	people?: number;
	preferLight?: boolean;
	keywords?: string[];
}

interface RecommendationItem {
	dishId?: number;
	merchantUserId?: number;
	dishName?: string;
	dishDesc?: string;
	price?: number;
	tags?: string[];
	reason?: string;
	score?: number;
}

const { takeaway_merchant_business_status, takeaway_dish_sale_status } = useDict(
	'takeaway_merchant_business_status',
	'takeaway_dish_sale_status'
);

const queryForm = reactive({
	limit: 3,
	message: '',
});

const contextLoading = ref(false);
const recommending = ref(false);
const currentCity = ref('');

const currentCustomerUserId = ref<string | undefined>();
const defaultAddressId = ref<string | undefined>();

const merchantList = ref<MerchantItem[]>([]);
const dishMap = reactive<Record<string, DishItem[]>>({});
const quantityMap = reactive<Record<string, Record<string, number>>>({});
const remarkMap = reactive<Record<string, string>>({});
const submittingMap = reactive<Record<string, boolean>>({});

const decisionPath = ref('');
const summary = ref('');
const intentView = ref<IntentView>();
const knowledgeEvidence = ref<string[]>([]);
const recommendations = ref<RecommendationItem[]>([]);

const recommendedReasonMap = reactive<Record<string, string>>({});

const hasResult = computed(() => recommendations.value.length > 0 || !!summary.value || knowledgeEvidence.value.length > 0);

const decisionPathLabel = computed(() => {
	if (decisionPath.value === 'rag') return 'RAG 语义推荐';
	if (decisionPath.value === 'tool-calling') return '结构化工具调用';
	return '推荐结果';
});

const decisionPathTagType = computed(() => {
	if (decisionPath.value === 'rag') return 'success';
	if (decisionPath.value === 'tool-calling') return 'warning';
	return 'info';
});

const businessOpenValue = computed(() => {
	const options = takeaway_merchant_business_status.value || [];
	const openItem = options.find((item: any) => item.label === '营业' || String(item.value) === '1');
	return openItem ? String(openItem.value) : '';
});

const dishSaleOnValue = computed(() => {
	const options = takeaway_dish_sale_status.value || [];
	const onItem = options.find((item: any) => item.label === '上架' || String(item.value) === '1');
	return onItem ? String(onItem.value) : '';
});

const normalizeId = (value: unknown) => {
	if (value === null || value === undefined || value === '') return undefined;
	return String(value);
};

const buildReasonKey = (merchantUserId: unknown, dishId: unknown) => {
	const m = normalizeId(merchantUserId) || '';
	const d = normalizeId(dishId) || '';
	return `${m}-${d}`;
};

const recommendedMerchantSet = computed(() => {
	const ids = new Set<string>();
	recommendations.value.forEach((item) => {
		const merchantUserId = normalizeId(item.merchantUserId);
		if (merchantUserId) ids.add(merchantUserId);
	});
	return ids;
});

const recommendedMerchantList = computed(() => {
	if (!recommendations.value.length) return [];
	return merchantList.value.filter((item) => {
		const merchantUserId = normalizeId(item.userId);
		return !!merchantUserId && recommendedMerchantSet.value.has(merchantUserId);
	});
});

const clearDishRelatedState = () => {
	Object.keys(dishMap).forEach((key) => delete dishMap[key]);
	Object.keys(quantityMap).forEach((key) => delete quantityMap[key]);
	Object.keys(remarkMap).forEach((key) => delete remarkMap[key]);
	Object.keys(submittingMap).forEach((key) => delete submittingMap[key]);
};

const clearRecommendationState = () => {
	decisionPath.value = '';
	summary.value = '';
	intentView.value = undefined;
	knowledgeEvidence.value = [];
	recommendations.value = [];
	Object.keys(recommendedReasonMap).forEach((key) => delete recommendedReasonMap[key]);
};

const resolveDishImageSrc = (image?: string) => resolveApiResourceUrl(image || '');

const formatMoney = (value: unknown) => {
	const amount = Number(value ?? 0);
	if (Number.isNaN(amount)) return '0.00';
	return amount.toFixed(2);
};

const getMerchantAddress = (merchant: MerchantItem) => {
	const address = merchant.address || {};
	return [
		address.province || merchant.province,
		address.city || merchant.city,
		address.district || merchant.district,
		address.detailAddress || merchant.detailAddress,
	]
		.filter(Boolean)
		.join('');
};

const ensureMerchantQuantityMap = (merchantUserId: unknown) => {
	const merchantId = normalizeId(merchantUserId);
	if (!merchantId) return undefined;
	if (!quantityMap[merchantId]) {
		quantityMap[merchantId] = {};
	}
	return quantityMap[merchantId];
};

const getDishQuantity = (merchantUserId: unknown, dishId: unknown) => {
	const merchantId = normalizeId(merchantUserId);
	const dishNum = normalizeId(dishId);
	if (!merchantId || !dishNum) return 0;
	return Number(quantityMap[merchantId]?.[dishNum] || 0);
};

const updateDishQuantity = (merchantUserId: unknown, dishId: unknown, value: number | null | undefined) => {
	const merchantId = normalizeId(merchantUserId);
	const dishNum = normalizeId(dishId);
	if (!merchantId || !dishNum) return;
	const map = ensureMerchantQuantityMap(merchantId);
	if (!map) return;
	map[dishNum] = Math.max(0, Math.floor(Number(value || 0)));
};

const isRecommendedDish = (merchantUserId: unknown, dishId: unknown) => {
	return !!recommendedReasonMap[buildReasonKey(merchantUserId, dishId)];
};

const getRecommendedReason = (merchantUserId: unknown, dishId: unknown) => {
	return recommendedReasonMap[buildReasonKey(merchantUserId, dishId)] || 'AI 推荐';
};

const getSelectedTotalAmount = (merchantUserId: unknown) => {
	const merchantId = normalizeId(merchantUserId);
	if (!merchantId) return '0.00';
	const list = dishMap[merchantId] || [];
	const total = list.reduce((sum, dish) => {
		const dishId = normalizeId(dish.id);
		if (!dishId) return sum;
		const qty = getDishQuantity(merchantId, dishId);
		if (qty <= 0) return sum;
		return sum + Number(dish.price || 0) * qty;
	}, 0);
	return formatMoney(total);
};

const resolveCurrentCustomerIdentity = async () => {
	const customerRes = await currentCustomer();
	const customer = customerRes?.data || {};
	const customerUserId = normalizeId(customer.userId) || normalizeId(useUserInfo().userInfos?.user?.userId);
	const currentDefaultAddressId = normalizeId(customer.defaultAddressId);
	return {
		customerUserId,
		defaultAddressId: currentDefaultAddressId,
	};
};

const loadContext = async () => {
	contextLoading.value = true;
	currentCity.value = '';
	merchantList.value = [];
	currentCustomerUserId.value = undefined;
	defaultAddressId.value = undefined;
	clearDishRelatedState();
	clearRecommendationState();

	try {
		const identity = await resolveCurrentCustomerIdentity();
		currentCustomerUserId.value = identity.customerUserId;
		defaultAddressId.value = identity.defaultAddressId;
		if (!identity.defaultAddressId) return;

		const addressRes = await listAddress();
		const addresses: AddressItem[] = addressRes?.data || [];
		const defaultAddress = addresses.find((item) => String(item.id) === String(identity.defaultAddressId));
		if (!defaultAddress) return;

		const city = String(defaultAddress.city || '').trim();
		if (!city) return;
		currentCity.value = city;

		const merchantRes = await listMerchantByRegion({
			province: defaultAddress.province,
			city,
		});
		const allMerchants: MerchantItem[] = merchantRes?.data || [];
		const openMerchants = allMerchants.filter((item) => String(item.businessStatus || '') === businessOpenValue.value);
		merchantList.value = openMerchants;

		await Promise.all(
			openMerchants.map(async (merchant) => {
				const merchantUserId = normalizeId(merchant.userId);
				if (!merchantUserId) return;
				const dishRes = await pageDish({
					current: 1,
					size: 500,
					merchantUserId,
				});
				const records: DishItem[] = dishRes?.data?.records || [];
				dishMap[merchantUserId] = records.filter((item) => String(item.saleStatus || '') === dishSaleOnValue.value);
				remarkMap[merchantUserId] = '';
				const map = ensureMerchantQuantityMap(merchantUserId);
				if (!map) return;
				dishMap[merchantUserId].forEach((dish) => {
					const dishId = normalizeId(dish.id);
					if (dishId) map[dishId] = 0;
				});
			})
		);
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '加载上下文失败');
	} finally {
		contextLoading.value = false;
	}
};

const handleRecommend = async () => {
	const message = String(queryForm.message || '').trim();
	if (!message) {
		useMessage().warning('请先输入你的点餐需求');
		return;
	}
	if (!currentCity.value) {
		useMessage().warning('当前城市未识别，无法推荐');
		return;
	}

	recommending.value = true;
	try {
		clearRecommendationState();
		const payload: AiAssistantRequestPayload = {
			message,
			limit: Math.max(1, Math.min(5, Number(queryForm.limit || 3))),
		};
		const res = await recommendAiOrder(payload);
		const data = res?.data || {};
		decisionPath.value = String(data.decisionPath || '');
		summary.value = String(data.summary || '');
		intentView.value = data.intent;
		knowledgeEvidence.value = Array.isArray(data.knowledgeEvidence) ? data.knowledgeEvidence : [];
		recommendations.value = Array.isArray(data.recommendations) ? data.recommendations : [];

		recommendations.value.forEach((item) => {
			const reasonKey = buildReasonKey(item.merchantUserId, item.dishId);
			recommendedReasonMap[reasonKey] = String(item.reason || 'AI 推荐');
			const merchantId = normalizeId(item.merchantUserId);
			const dishId = normalizeId(item.dishId);
			if (merchantId && dishId) {
				const map = ensureMerchantQuantityMap(merchantId);
				if (map) {
					map[dishId] = Math.max(1, Number(map[dishId] || 0));
				}
			}
		});
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '智能推荐失败');
	} finally {
		recommending.value = false;
	}
};

const submitOrder = async (merchant: MerchantItem) => {
	const merchantUserId = normalizeId(merchant.userId);
	if (!merchantUserId) {
		useMessage().warning('商家用户信息缺失，无法下单');
		return;
	}

	let latestCustomerUserId: string | undefined;
	let latestDefaultAddressId: string | undefined;
	try {
		const identity = await resolveCurrentCustomerIdentity();
		latestCustomerUserId = identity.customerUserId;
		latestDefaultAddressId = identity.defaultAddressId;
	} catch {
		latestCustomerUserId = currentCustomerUserId.value;
		latestDefaultAddressId = defaultAddressId.value;
	}

	if (!latestDefaultAddressId) {
		useMessage().warning('请先在客户信息中设置默认地址');
		return;
	}
	if (!latestCustomerUserId) {
		useMessage().warning('未获取到当前用户信息，无法下单');
		return;
	}

	const dishes = dishMap[merchantUserId] || [];
	const items = dishes
		.map((dish) => {
			const dishId = normalizeId(dish.id);
			const quantity = dishId ? getDishQuantity(merchantUserId, dishId) : 0;
			if (!dishId || quantity <= 0) return null;
			return {
				dishId,
				quantity,
			};
		})
		.filter(Boolean);

	if (!items.length) {
		useMessage().warning('请至少选择一个数量大于0的菜品');
		return;
	}

	submittingMap[merchantUserId] = true;
	try {
		await createOrder({
			customerUserId: latestCustomerUserId,
			merchantUserId,
			deliveryAddressId: latestDefaultAddressId,
			remark: String(remarkMap[merchantUserId] || '').trim(),
			items,
		});

		const map = ensureMerchantQuantityMap(merchantUserId);
		if (map) {
			Object.keys(map).forEach((key) => {
				map[key] = 0;
			});
		}
		remarkMap[merchantUserId] = '';
		useMessage().success('下单成功');
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '下单失败');
	} finally {
		submittingMap[merchantUserId] = false;
	}
};

const clearResult = () => {
	clearRecommendationState();
};

onMounted(() => {
	loadContext();
});
</script>

<style scoped>
.ai-order-page {
	--ai-accent: #0f766e;
	--ai-soft: #f0fdfa;
	height: 100%;
}

.ai-order-page > .layout-padding-auto,
.ai-order-page > .layout-padding-auto > .layout-padding-view {
	height: 100%;
}

.page-row {
	align-items: stretch;
	height: 100%;
}

.page-row > .el-col {
	height: 100%;
	min-height: 0;
	display: flex;
}

.input-card,
.result-card {
	height: 100%;
	width: 100%;
	display: flex;
	flex-direction: column;
	min-height: 0;
}

.input-card :deep(.el-card__body),
.result-card :deep(.el-card__body) {
	flex: 1;
	min-height: 0;
	overflow-y: auto;
}

.card-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12px;
}

.title-main {
	font-size: 18px;
	font-weight: 700;
}

.title-sub {
	font-size: 12px;
	color: var(--el-text-color-secondary);
	margin-top: 2px;
}

.city-tip {
	margin-bottom: 12px;
	font-size: 13px;
	display: flex;
	align-items: center;
	gap: 8px;
	flex-wrap: wrap;
}

.merchant-count {
	color: var(--el-text-color-secondary);
}

.input-actions {
	display: flex;
	gap: 8px;
	justify-content: flex-end;
	margin-top: 10px;
}

.header-tags {
	display: flex;
	gap: 6px;
}

.merchant-title {
	display: grid;
	grid-template-columns: minmax(160px, 2fr) 80px minmax(180px, 3fr);
	align-items: center;
	gap: 10px;
	width: 100%;
}

.merchant-name {
	font-size: 15px;
	font-weight: 700;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.merchant-city,
.merchant-address {
	font-size: 12px;
	color: var(--el-text-color-secondary);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.dish-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
	gap: 10px;
	margin-top: 8px;
}

.dish-card {
	border-radius: 8px;
	min-height: 248px;
}

.dish-card :deep(.el-card__body) {
	padding: 12px;
	height: 100%;
	display: flex;
	flex-direction: column;
}

.dish-card.is-recommended {
	border: 1px solid var(--el-color-danger);
	box-shadow: 0 0 0 2px rgb(245 34 45 / 10%);
}

.dish-image-wrapper {
	width: 100%;
	height: 116px;
	margin-bottom: 8px;
}

.dish-image {
	width: 100%;
	height: 100%;
	border-radius: 8px;
	display: block;
}

.dish-image-placeholder {
	width: 100%;
	height: 100%;
	border-radius: 8px;
	border: 1px dashed var(--el-border-color);
	background: var(--el-fill-color-light);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 13px;
	color: var(--el-text-color-secondary);
}

.dish-content {
	display: flex;
	flex-direction: column;
	gap: 6px;
	flex: 1;
}

.dish-title-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 6px;
}

.dish-name {
	font-size: 14px;
	font-weight: 700;
	color: var(--el-text-color-primary);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.dish-desc {
	font-size: 11px;
	line-height: 1.4;
	color: var(--el-text-color-secondary);
	height: 30px;
	overflow: hidden;
	display: -webkit-box;
	line-clamp: 2;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
}

.dish-meta {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.dish-price {
	font-size: 16px;
	font-weight: 700;
	color: var(--el-color-danger);
}

.dish-stock {
	font-size: 11px;
	color: var(--el-text-color-secondary);
}

.dish-quantity {
	width: 100%;
}

.merchant-remark-panel {
	grid-column: 1 / -1;
	padding: 12px 14px;
	border-radius: 12px;
	border: 1px solid var(--el-border-color-lighter);
	background: linear-gradient(120deg, var(--ai-soft) 0%, var(--el-bg-color) 100%);
}

.merchant-remark-title {
	font-size: 13px;
	font-weight: 600;
	margin-bottom: 8px;
}

.merchant-actions {
	display: flex;
	align-items: center;
	justify-content: flex-end;
	gap: 8px;
	margin-top: 12px;
	flex-wrap: wrap;
}

.selected-total {
	font-size: 13px;
	color: var(--el-text-color-regular);
}

.selected-total-amount {
	font-size: 18px;
	font-weight: 700;
	color: var(--el-color-danger);
}

@media (max-width: 991px) {
	.page-row,
	.page-row > .el-col,
	.input-card,
	.result-card {
		height: auto;
	}

	.input-card :deep(.el-card__body),
	.result-card :deep(.el-card__body) {
		overflow-y: visible;
	}

	.input-actions {
		justify-content: flex-start;
	}

	.merchant-title {
		grid-template-columns: 1fr;
		gap: 2px;
	}

	.dish-grid {
		grid-template-columns: 1fr;
	}
}
</style>
