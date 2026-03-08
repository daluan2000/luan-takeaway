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
						<el-table v-else :data="dishMap[merchant.userId || 0]" size="small" border>
							<el-table-column prop="dishName" label="菜品名称" min-width="160" show-overflow-tooltip />
							<el-table-column prop="dishDesc" label="菜品描述" min-width="220" show-overflow-tooltip />
							<el-table-column label="价格(元)" width="120">
								<template #default="scope">
									{{ Number(scope.row.price || 0).toFixed(2) }}
								</template>
							</el-table-column>
							<el-table-column prop="stock" label="库存" width="100" />
							<el-table-column label="数量" width="180">
								<template #default="scope">
									<el-input-number
										:model-value="getDishQuantity(merchant.userId || 0, scope.row.id || 0)"
										:min="0"
										:max="Number(scope.row.stock || 0)"
										:step="1"
										controls-position="right"
										style="width: 100%"
										@update:model-value="updateDishQuantity(merchant.userId || 0, scope.row.id || 0, $event)"
									/>
								</template>
							</el-table-column>
						</el-table>

						<div class="merchant-actions">
							<el-button
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
			items,
		});
		const merchantQuantity = ensureMerchantQuantityMap(merchantUserId);
		Object.keys(merchantQuantity).forEach((key) => {
			merchantQuantity[key] = 0;
		});
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
	justify-content: flex-end;
	margin-top: 12px;
}
</style>
