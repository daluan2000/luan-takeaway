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

				<el-collapse v-else>
					<el-collapse-item
						v-for="merchant in merchantList"
						:key="merchant.id || merchant.userId"
						:name="String(merchant.id || merchant.userId)"
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
								<span v-if="distanceMap[merchant.id || merchant.userId || 0]" class="merchant-distance">
									{{ distanceMap[merchant.id || merchant.userId || 0] }}
								</span>
							</div>
						</template>

						<el-empty
							v-if="!dishMap[merchant.userId || 0]?.length"
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
										:src="dish.dishImage"
										fit="cover"
										:preview-src-list="[dish.dishImage]"
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
								:disabled="!dishMap[merchant.userId || 0]?.length"
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
import { useMessage } from '/@/hooks/message';
import { listAddress } from '/@/api/takeaway/address';
import { currentCustomer } from '/@/api/takeaway/customer';
import { pageList as pageDish } from '/@/api/takeaway/dish';
import { listMerchantByRegion } from '/@/api/takeaway/merchant';
import { createOrder } from '/@/api/takeaway/order';
import { useUserInfo } from '/@/stores/userInfo';

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
const distanceMap = reactive<Record<string, string>>({});
const quantityMap = reactive<Record<string, Record<string, number>>>({});
const remarkMap = reactive<Record<string, string>>({});
const submittingMap = reactive<Record<string, boolean>>({});
const currentCustomerUserId = ref<string | undefined>();
const currentDeliveryAddressId = ref<string | undefined>();

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
		const queryCity = String(currentAddress?.city || '').trim();
		if (!queryCity) {
			return;
		}

		currentCity.value = queryCity;
		currentDisplayRegion.value = resolveDisplayRegion(currentAddress);
		const merchantRes = await listMerchantByRegion({
			province: currentAddress.province,
			city: queryCity,
		});

		const allMerchants: MerchantItem[] = merchantRes?.data || [];
		const openMerchants = allMerchants.filter((item) => item.businessStatus === '1');
		merchantList.value = openMerchants;

		openMerchants.forEach((merchant) => {
			const key = normalizeId(merchant.id) || normalizeId(merchant.userId);
			if (!key) return;
			distanceMap[key] = calcDistanceKm(currentAddress, merchant.address);
		});

		await Promise.all(
			openMerchants.map(async (merchant) => {
				const merchantUserId = normalizeId(merchant.userId);
				if (!merchantUserId) {
					return;
				}
				const dishRes = await pageDish({
					current: 1,
					size: 500,
					merchantUserId,
				});
				const records: DishItem[] = dishRes?.data?.records || [];
				dishMap[merchantUserId] = records.filter((item) => item.saleStatus === '1');
				remarkMap[merchantUserId] = '';
				const merchantQuantity = ensureMerchantQuantityMap(merchantUserId);
				dishMap[merchantUserId].forEach((dish) => {
					const dishId = normalizeId(dish.id);
					if (dishId) {
						merchantQuantity[dishId] = 0;
					}
				});
			})
		);
	} catch (error: any) {
		useMessage().error(error?.msg || error?.response?.data?.msg || '加载失败');
	} finally {
		loading.value = false;
	}
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
	grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
	gap: 10px;
	margin-top: 8px;
}

.dish-card {
	border-radius: 8px;
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
