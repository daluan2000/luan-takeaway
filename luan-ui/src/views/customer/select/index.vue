<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card v-loading="loading">
				<template #header>
					<div class="card-header">
						<span>{{ cardTitle }}</span>
						<el-button link type="primary" @click="loadData">刷新</el-button>
					</div>
				</template>

				<el-alert
					v-if="!currentCity"
					title="未获取到当前所在市，请先在客户信息中设置默认地址"
					type="warning"
					show-icon
					:closable="false"
					class="mb20"
				/>

				<el-empty v-else-if="!merchantList.length" :description="emptyMerchantDescription" />

				<el-collapse v-else @change="handleCollapseChange">
					<el-collapse-item
						v-for="merchant in merchantList"
						:key="merchant.userId || merchant.id"
						:name="String(merchant.userId || merchant.id)"
					>
						<template #title>
							<div class="merchant-title">
								<span class="merchant-name">
									<span class="merchant-label">商家名称：</span>
									{{ merchant.merchantName || `商店#${merchant.id}` }}
								</span>
								<span class="merchant-city">{{ currentCity }}</span>
								<el-tooltip
									v-if="getMerchantAddress(merchant)"
									:content="getMerchantAddress(merchant)"
									placement="top-start"
								>
									<span class="merchant-address">{{ getMerchantAddress(merchant) }}</span>
								</el-tooltip>
								<span v-else class="merchant-address">-</span>
								<span v-if="distanceMap[merchant.userId || merchant.id || 0]" class="merchant-distance">
									{{ distanceMap[merchant.userId || merchant.id || 0] }}
								</span>
							</div>
						</template>

						<div v-if="dishLoadingMap[normalizeId(merchant.userId) || '']" class="dish-loading">
							<el-icon class="is-loading"><Loading /></el-icon>
							<span>加载菜品中...</span>
						</div>

						<el-empty
							v-else-if="!dishMap[normalizeId(merchant.userId) || '']?.length"
							description="该商店暂无在售菜品"
							:image-size="80"
						/>
						<div v-else class="dish-grid">
							<el-card
								v-for="dish in dishMap[merchant.userId || 0]"
								:key="dish.id || dish.dishName"
								shadow="hover"
								class="dish-card"
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
									<div v-else class="dish-image-placeholder">
										图片不存在
									</div>
								</div>

								<div class="dish-content">
									<div class="dish-name" :title="dish.dishName || '-'">{{ dish.dishName || '-' }}</div>
									<div class="dish-desc" :title="dish.dishDesc || '-'">{{ dish.dishDesc || '-' }}</div>
									<div class="dish-meta">
										<span class="dish-price">￥{{ Number(dish.price || 0).toFixed(2) }}</span>
										<span class="dish-stock">库存 {{ Number(dish.stock || 0) }}</span>
									</div>
									<el-input-number
										:model-value="getDishQuantity(merchant.userId || 0, dish.id || 0)"
										:min="0"
										:max="Number(dish.stock || 0)"
										:step="1"
										controls-position="right"
										class="dish-quantity"
										@update:model-value="updateDishQuantity(merchant.userId || 0, dish.id || 0, $event)"
									/>
								</div>
							</el-card>

							<div class="merchant-remark-panel">
								<div class="merchant-remark-title">订单备注</div>
								<el-input
									v-model="remarkMap[merchant.userId || 0]"
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
								已选金额：<span class="selected-total-amount">￥{{ getSelectedTotalAmount(merchant.userId || 0) }}</span>
							</div>
							<el-button
								v-auth="'wm_customer_order_create'"
								type="primary"
								size="small"
								:loading="submittingMap[merchant.userId || 0]"
								:disabled="dishLoadingMap[normalizeId(merchant.userId) || ''] || !dishMap[normalizeId(merchant.userId) || '']?.length"
								@click.stop="submitOrder(merchant)"
							>
								下单
							</el-button>
						</div>
					</el-collapse-item>
				</el-collapse>
			</el-card>
		</div>
	</div>
</template>

<script setup lang="ts" name="customerSelectIndex">
import { Loading } from '@element-plus/icons-vue';
import { useMessage } from '/@/hooks/message';
import { listAddress } from '/@/api/takeaway/address';
import { currentCustomer } from '/@/api/takeaway/customer';
import { listMerchantByRegion, listMerchantByNearby } from '/@/api/takeaway/merchant';
import { pageList as pageDish } from '/@/api/takeaway/dish';
import { createOrder } from '/@/api/takeaway/order';
import { useUserInfo } from '/@/stores/userInfo';
import { resolveApiResourceUrl } from '/@/utils/url';
import { useDict } from '/@/hooks/dict';

interface AddressItem {
	id?: string | number;
	province?: string;
	city?: string;
	district?: string;
	detailAddress?: string;
	longitude?: number | string;
	latitude?: number | string;
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
	dishList?: DishItem[];
}

interface DishItem {
	id?: string | number;
	dishImage?: string;
	dishName?: string;
	dishDesc?: string;
	price?: number;
	stock?: number;
	saleStatus?: string;
}

const resolveDishImageSrc = (image?: string) => {
	return resolveApiResourceUrl(image);
};

const normalizeId = (value: unknown): string | undefined => {
	if (value === null || value === undefined || value === '') {
		return undefined;
	}
	return String(value);
};

const loading = ref(false);
const currentCity = ref('');
const currentDisplayRegion = ref('');
const merchantList = ref<MerchantItem[]>([]);
const dishMap = reactive<Record<string, DishItem[]>>({});
const dishLoadingMap = reactive<Record<string, boolean>>({});
const distanceMap = reactive<Record<string, string>>({});
const quantityMap = reactive<Record<string, Record<string, number>>>({});
const remarkMap = reactive<Record<string, string>>({});
const submittingMap = reactive<Record<string, boolean>>({});
const currentCustomerUserId = ref<string | undefined>();
const currentDeliveryAddressId = ref<string | undefined>();
const { takeaway_merchant_business_status, takeaway_dish_sale_status } = useDict(
	'takeaway_merchant_business_status',
	'takeaway_dish_sale_status'
);

const businessOpenValue = computed(() => {
	const options = takeaway_merchant_business_status.value || [];
	const target = options.find((item: any) => item.label === '营业' || String(item.value) === '1');
	return String((target || { value: '1' }).value);
});

const dishSaleOnValue = computed(() => {
	const options = takeaway_dish_sale_status.value || [];
	const target = options.find((item: any) => item.label === '上架' || String(item.value) === '1');
	return String((target || { value: '1' }).value);
});

const cardTitle = computed(() => {
	return currentDisplayRegion.value ? `${currentDisplayRegion.value}营业商店与菜品` : '同城营业商店与菜品';
	
});

const emptyMerchantDescription = computed(() => {
	return currentDisplayRegion.value ? `当前${currentDisplayRegion.value}暂无正在营业的商店` : '当前地区暂无正在营业的商店';
});

const clearDishMap = () => {
	Object.keys(dishMap).forEach((key) => {
		delete dishMap[key];
	});
	Object.keys(dishLoadingMap).forEach((key) => {
		delete dishLoadingMap[key];
	});
};

const clearDistanceMap = () => {
	Object.keys(distanceMap).forEach((key) => {
		delete distanceMap[key];
	});
};

const clearQuantityMap = () => {
	Object.keys(quantityMap).forEach((key) => {
		delete quantityMap[key];
	});
};

const clearSubmittingMap = () => {
	Object.keys(submittingMap).forEach((key) => {
		delete submittingMap[key];
	});
};

const clearRemarkMap = () => {
	Object.keys(remarkMap).forEach((key) => {
		delete remarkMap[key];
	});
};

const ensureMerchantQuantityMap = (merchantUserId: string | number) => {
	const merchantKey = normalizeId(merchantUserId);
	if (!merchantKey) {
		return {} as Record<string, number>;
	}
	if (!quantityMap[merchantKey]) {
		quantityMap[merchantKey] = {};
	}
	return quantityMap[merchantKey];
};

const getDishQuantity = (merchantUserId: string | number, dishId: string | number) => {
	const merchantKey = normalizeId(merchantUserId);
	const dishKey = normalizeId(dishId);
	if (!merchantKey || !dishKey) return 0;
	const merchantQuantity = ensureMerchantQuantityMap(merchantUserId);
	return merchantQuantity[dishKey] ?? 0;
};

const updateDishQuantity = (merchantUserId: string | number, dishId: string | number, value: number | null | undefined) => {
	const merchantKey = normalizeId(merchantUserId);
	const dishKey = normalizeId(dishId);
	if (!merchantKey || !dishKey) return;
	const merchantQuantity = ensureMerchantQuantityMap(merchantUserId);
	merchantQuantity[dishKey] = Math.max(0, Math.floor(Number(value ?? 0)));
};

const getSelectedTotalAmount = (merchantUserId: string | number) => {
	const merchantKey = normalizeId(merchantUserId);
	if (!merchantKey) return '0.00';
	const dishes = dishMap[merchantKey] || [];
	const total = dishes.reduce((sum, dish) => {
		const dishId = normalizeId(dish.id);
		if (!dishId) return sum;
		const quantity = getDishQuantity(merchantKey, dishId);
		if (quantity <= 0) return sum;
		return sum + Number(dish.price || 0) * quantity;
	}, 0);
	return total.toFixed(2);
};

const calcDistanceKm = (userAddress: AddressItem, storeAddress?: AddressItem) => {
	if (!storeAddress) return '';
	const userLng = Number(userAddress.longitude);
	const userLat = Number(userAddress.latitude);
	const storeLng = Number(storeAddress.longitude);
	const storeLat = Number(storeAddress.latitude);

	if ([userLng, userLat, storeLng, storeLat].some((item) => Number.isNaN(item))) {
		return '';
	}

	const avgLatRad = ((userLat + storeLat) / 2) * (Math.PI / 180);
	const dxKm = (storeLng - userLng) * 111 * Math.cos(avgLatRad);
	const dyKm = (storeLat - userLat) * 111;
	return `${Math.sqrt(dxKm * dxKm + dyKm * dyKm).toFixed(2)} km`;
};

const resolveDisplayRegion = (address?: AddressItem | null) => {
	if (!address) return '';
	const city = String(address.city || '').trim();
	return city;
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

const getCurrentAddress = async () => {
	const customerRes = await currentCustomer();
	const customer = customerRes?.data || {};
	const defaultAddressId = normalizeId(customer?.defaultAddressId);
	currentCustomerUserId.value = normalizeId(customer?.userId);
	currentDeliveryAddressId.value = defaultAddressId;
	if (!defaultAddressId) return null;

	const addrRes = await listAddress();
	const addresses: AddressItem[] = addrRes?.data || [];
	return addresses.find((item) => String(item.id) === String(defaultAddressId)) || null;
};

const getCurrentOrderIdentity = async () => {
	const customerRes = await currentCustomer();
	const customer = customerRes?.data || {};
	const customerUserId = normalizeId(customer?.userId);
	const defaultAddressId = normalizeId(customer?.defaultAddressId);
	if (customerUserId) {
		currentCustomerUserId.value = customerUserId;
	}
	if (defaultAddressId) {
		currentDeliveryAddressId.value = defaultAddressId;
	}
	return {
		customerUserId,
		defaultAddressId,
	};
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
		const identity = await getCurrentOrderIdentity();
		latestCustomerUserId = identity.customerUserId;
		latestDefaultAddressId = identity.defaultAddressId;
	} catch (error) {
		// Fallback to cached data when refreshing customer info fails.
		latestCustomerUserId = currentCustomerUserId.value;
		latestDefaultAddressId = currentDeliveryAddressId.value;
	}

	const deliveryAddressId = latestDefaultAddressId || currentDeliveryAddressId.value;
	if (!deliveryAddressId) {
		useMessage().warning('请先在客户信息中设置默认地址');
		return;
	}

	const customerUserId = latestCustomerUserId || currentCustomerUserId.value || normalizeId(useUserInfo().userInfos?.user?.userId);
	if (!customerUserId) {
		useMessage().warning('未获取到当前用户信息，无法下单');
		return;
	}

	const dishes = dishMap[merchantUserId] || [];
	const remark = String(remarkMap[merchantUserId] || '').trim();
	const items = dishes
		.map((dish) => {
			const dishId = normalizeId(dish.id);
			const quantity = dishId ? getDishQuantity(merchantUserId, dishId) : 0;
			if (!dishId || quantity <= 0) {
				return null;
			}
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
			customerUserId,
			merchantUserId,
			deliveryAddressId,
			remark,
			items,
		});
		const merchantQuantity = ensureMerchantQuantityMap(merchantUserId);
		Object.keys(merchantQuantity).forEach((key) => {
			merchantQuantity[key] = 0;
		});
		remarkMap[merchantUserId] = '';
		useMessage().success('下单成功');
		await loadData();
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '下单失败');
	} finally {
		submittingMap[merchantUserId] = false;
	}
};

const loadData = async () => {
	loading.value = true;
	merchantList.value = [];
	clearDishMap();
	clearDistanceMap();
	clearQuantityMap();
	clearRemarkMap();
	clearSubmittingMap();
	currentCity.value = '';
	currentDisplayRegion.value = '';
	currentCustomerUserId.value = undefined;
	currentDeliveryAddressId.value = undefined;

	try {
		const currentAddress = await getCurrentAddress();
		if (!currentAddress) {
			return;
		}

		const userLng = Number(currentAddress.longitude);
		const userLat = Number(currentAddress.latitude);
		const hasValidCoordinates = !Number.isNaN(userLng) && !Number.isNaN(userLat) && userLng !== 0 && userLat !== 0;

		const queryCity = String(currentAddress?.city || '').trim();

		let allMerchants: MerchantItem[] = [];

		if (hasValidCoordinates) {
			// 有有效经纬度时，使用附近商家接口
			currentCity.value = queryCity || '附近';
			currentDisplayRegion.value = '附近';
			const merchantRes = await listMerchantByNearby({
				longitude: userLng,
				latitude: userLat,
			});
			allMerchants = merchantRes?.data || [];
		}
		else if (queryCity) {
			// 无有效经纬度但有城市信息时，降级使用城市查询
			currentCity.value = queryCity;
			currentDisplayRegion.value = resolveDisplayRegion(currentAddress);
			const merchantRes = await listMerchantByRegion({
				province: currentAddress.province,
				city: queryCity,
			});
			allMerchants = merchantRes?.data || [];
		}
		else {
			// 既没有经纬度也没有城市信息，无法查询
			return;
		}

		const openMerchants = allMerchants.filter((item) => String(item.businessStatus) === businessOpenValue.value);
		merchantList.value = openMerchants;

		if (hasValidCoordinates) {
			openMerchants.forEach((merchant) => {
				const key = normalizeId(merchant.userId) || normalizeId(merchant.id);
				if (!key) return;
				distanceMap[key] = calcDistanceKm(currentAddress, merchant.address);
			});
		}
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '加载失败');
	} finally {
		loading.value = false;
	}
};

const loadMerchantDishes = async (merchantUserId: string) => {
	const key = normalizeId(merchantUserId);
	if (!key || dishMap[key] || dishLoadingMap[key]) {
		return;
	}
	dishLoadingMap[key] = true;
	try {
		const dishRes = await pageDish({
			current: 1,
			size: 500,
			merchantUserId,
		});
		const records: DishItem[] = dishRes?.data?.records || [];
		dishMap[key] = records.filter((item) => String(item.saleStatus) === dishSaleOnValue.value);
		remarkMap[key] = '';
		const merchantQuantity = ensureMerchantQuantityMap(key);
		dishMap[key].forEach((dish) => {
			const dishId = normalizeId(dish.id);
			if (dishId) {
				merchantQuantity[dishId] = 0;
			}
		});
	} catch (error: any) {
		useMessage().error(`加载菜品失败: ${error?.msg || error?.response?.data?.msg || '未知错误'}`);
	} finally {
		dishLoadingMap[key] = false;
	}
};

const expandedMerchants = ref<Set<string>>(new Set());

const handleCollapseChange = (names: string | string[]) => {
	const nameList = Array.isArray(names) ? names : [names];
	const currentIds = new Set(nameList.filter(Boolean));
	// 处理新增展开的商家
	currentIds.forEach((id) => {
		if (!expandedMerchants.value.has(id)) {
			expandedMerchants.value.add(id);
			loadMerchantDishes(id);
		}
	});
	// 处理收起展开的商家
	expandedMerchants.value.forEach((id) => {
		if (!currentIds.has(id)) {
			expandedMerchants.value.delete(id);
		}
	});
};

onMounted(() => {
	loadData();
});
</script>

<style scoped>
.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.merchant-title {
	display: grid;
	grid-template-columns: minmax(220px, 2fr) 80px minmax(260px, 3fr) 90px;
	align-items: center;
	column-gap: 12px;
	width: 100%;
	overflow: hidden;
}

.merchant-name {
	display: flex;
	align-items: center;
	min-width: 0;
	font-weight: 700;
	font-size: 16px;
	line-height: 1.4;
}

.merchant-label {
	font-size: 13px;
	font-weight: 500;
	color: var(--el-text-color-secondary);
	margin-right: 4px;
	flex-shrink: 0;
}

.merchant-city {
	font-size: 12px;
	color: var(--el-text-color-secondary);
}

.merchant-address {
	max-width: 100%;
	min-width: 0;
	font-size: 12px;
	color: var(--el-text-color-regular);
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.merchant-distance {
	font-size: 12px;
	color: var(--el-color-primary);
	justify-self: end;
	text-align: right;
}

@media (max-width: 768px) {
	.merchant-title {
		grid-template-columns: minmax(180px, 1fr) auto;
		row-gap: 4px;
	}

	.merchant-address {
		grid-column: 1 / 3;
	}
}

.merchant-actions {
	display: flex;
	align-items: center;
	justify-content: flex-end;
	gap: 8px;
	flex-wrap: wrap;
	margin-top: 12px;
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

.dish-loading {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8px;
	padding: 40px 0;
	color: var(--el-text-color-secondary);
	font-size: 14px;
}

.dish-loading .el-icon {
	font-size: 20px;
	color: var(--el-color-primary);
}

@media (max-width: 768px) {
	.merchant-actions {
		justify-content: flex-end;
		gap: 6px;
	}

	.selected-total {
		white-space: nowrap;
	}
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

.merchant-remark-panel {
	grid-column: 1 / -1;
	padding: 12px 14px;
	border-radius: 12px;
	border: 1px solid var(--el-border-color-lighter);
	background: linear-gradient(120deg, var(--el-fill-color-light) 0%, var(--el-bg-color) 100%);
	box-shadow: 0 4px 14px rgb(0 0 0 / 4%);
}

.merchant-remark-title {
	font-size: 13px;
	font-weight: 600;
	color: var(--el-text-color-primary);
	margin-bottom: 8px;
	letter-spacing: 0.2px;
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
	align-items: center;
	justify-content: space-between;
	gap: 8px;
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

@media (max-width: 768px) {
	.dish-grid {
		grid-template-columns: 1fr;
	}
}
</style>
