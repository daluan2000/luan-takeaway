<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card v-loading="loading">
				<template #header>
					<div class="card-header">
						<span>同城营业商店与菜品</span>
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

				<el-empty v-else-if="!merchantList.length" description="当前城市暂无正在营业的商店" />

				<el-collapse v-else>
					<el-collapse-item
						v-for="merchant in merchantList"
						:key="merchant.id || merchant.userId"
						:name="String(merchant.id || merchant.userId)"
					>
						<template #title>
							<div class="merchant-title">
								<span>{{ merchant.merchantName || `商店#${merchant.id}` }}</span>
								<span class="merchant-city">{{ currentCity }}</span>
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
						</el-table>
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

interface AddressItem {
	id?: number;
	province?: string;
	city?: string;
	district?: string;
	longitude?: number | string;
	latitude?: number | string;
}

interface MerchantItem {
	id?: number;
	userId?: number;
	merchantName?: string;
	businessStatus?: string;
	address?: AddressItem;
}

interface DishItem {
	id?: number;
	dishName?: string;
	dishDesc?: string;
	price?: number;
	stock?: number;
	saleStatus?: string;
}

const loading = ref(false);
const currentCity = ref('');
const merchantList = ref<MerchantItem[]>([]);
const dishMap = reactive<Record<number, DishItem[]>>({});
const distanceMap = reactive<Record<number, string>>({});

const clearDishMap = () => {
	Object.keys(dishMap).forEach((key) => {
		delete dishMap[Number(key)];
	});
};

const clearDistanceMap = () => {
	Object.keys(distanceMap).forEach((key) => {
		delete distanceMap[Number(key)];
	});
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

const getCurrentAddress = async () => {
	const customerRes = await currentCustomer();
	const customer = customerRes?.data || {};
	const defaultAddressId = customer?.defaultAddressId;
	if (!defaultAddressId) return null;

	const addrRes = await listAddress();
	const addresses: AddressItem[] = addrRes?.data || [];
	return addresses.find((item) => String(item.id) === String(defaultAddressId)) || null;
};

const loadData = async () => {
	loading.value = true;
	merchantList.value = [];
	clearDishMap();
	clearDistanceMap();
	currentCity.value = '';

	try {
		const currentAddress = await getCurrentAddress();
		if (!currentAddress?.city) {
			return;
		}

		currentCity.value = currentAddress.city;
		const merchantRes = await listMerchantByRegion({
			province: currentAddress.province,
			city: currentAddress.city,
		});

		const allMerchants: MerchantItem[] = merchantRes?.data || [];
		const openMerchants = allMerchants.filter((item) => item.businessStatus === '1');
		merchantList.value = openMerchants;

		openMerchants.forEach((merchant) => {
			const key = merchant.id || merchant.userId;
			if (!key) return;
			distanceMap[key] = calcDistanceKm(currentAddress, merchant.address);
		});

		await Promise.all(
			openMerchants.map(async (merchant) => {
				const merchantUserId = merchant.userId;
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
	display: flex;
	align-items: center;
	gap: 12px;
}

.merchant-city {
	font-size: 12px;
	color: var(--el-text-color-secondary);
}

.merchant-distance {
	font-size: 12px;
	color: var(--el-color-primary);
}
</style>
