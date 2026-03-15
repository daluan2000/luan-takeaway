<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-card v-loading="loading">
				<template #header>
					<div class="card-header">
						<span>商家扩展信息</span>
					</div>
				</template>

				<el-alert
					v-if="isCreateMode"
					title="当前用户暂无商家扩展信息，请先新增"
					type="warning"
					show-icon
					:closable="false"
					class="mb20"
				/>

				<el-alert
					v-if="!isCreateMode && form.auditStatus === '0'"
					:title="auditPendingAlertText"
					type="info"
					show-icon
					:closable="false"
					class="mb20"
				/>

				<el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 700px">
					<el-form-item label="商家名称" prop="merchantName">
						<el-input v-model="form.merchantName" maxlength="128" placeholder="请输入商家名称" />
					</el-form-item>

					<el-form-item label="联系人" prop="contactName">
						<el-input v-model="form.contactName" maxlength="64" placeholder="请输入联系人" />
					</el-form-item>

					<el-form-item label="门店地址ID" prop="storeAddressId">
						<el-select v-model="form.storeAddressId" placeholder="请选择门店地址" filterable clearable style="width: 100%">
							<el-option v-for="item in addressOptions" :key="item.value" :label="item.label" :value="item.value" />
						</el-select>
						<div v-if="!addressOptions.length" class="address-empty-tip">
							<el-alert title="暂无可用地址，请先新增地址" type="warning" :closable="false" show-icon />
							<el-button v-auth="'wm_address_add'" type="primary" link @click="goAddressPage">去新增地址</el-button>
						</div>
					</el-form-item>

					<el-form-item label="营业状态" prop="businessStatus">
						<el-select v-model="form.businessStatus" placeholder="请选择营业状态" style="width: 100%">
							<el-option v-for="item in businessStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
						</el-select>
					</el-form-item>

					<el-form-item label="审核状态" v-if="!isCreateMode">
						<el-tag :type="getAuditStatusTagType(form.auditStatus)">{{ getAuditStatusLabel(form.auditStatus) }}</el-tag>
					</el-form-item>

					<el-form-item>
						<el-button v-if="isCreateMode" v-auth="'wm_merchant_info_add'" type="primary" :loading="submitting" @click="handleSubmit">新增</el-button>
						<el-button v-else v-auth="'wm_merchant_info_edit'" type="primary" :loading="submitting" @click="handleSubmit">修改</el-button>
						<el-button @click="loadCurrent">刷新</el-button>
					</el-form-item>
				</el-form>
			</el-card>
		</div>
	</div>
</template>

<script setup lang="ts" name="merchantInfo">
import { useMessage } from '/@/hooks/message';
import { listAddress } from '/@/api/takeaway/address';
import { applyMerchant, currentMerchant, updateMerchant } from '/@/api/takeaway/merchant';
import { getPublicValue } from '/@/api/admin/param';
import { useMsg } from '/@/stores/msg';
import { useUserInfo } from '/@/stores/userInfo';
import { useRouter } from 'vue-router';
import { useDict } from '/@/hooks/dict';

const PARAM_MERCHANT_AUTO_AUDIT_DELAY_MIN_SECONDS = 'TAKEAWAY_MERCHANT_AUTO_AUDIT_DELAY_MIN_SECONDS';
const PARAM_MERCHANT_AUTO_AUDIT_DELAY_MAX_SECONDS = 'TAKEAWAY_MERCHANT_AUTO_AUDIT_DELAY_MAX_SECONDS';
const DEFAULT_AUDIT_DELAY_MIN_SECONDS = 3;
const DEFAULT_AUDIT_DELAY_MAX_SECONDS = 10;

const formRef = ref();
const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const isCreateMode = ref(true);
const addressOptions = ref<Array<{ label: string; value: string }>>([]);
const msgStore = useMsg();
const normalizeId = (value: unknown): string | undefined => {
	if (value === null || value === undefined || value === '') {
		return undefined;
	}
	const text = String(value).trim();
	return text ? text : undefined;
};

const currentMerchantUserId = computed(() => normalizeId(useUserInfo().userInfos?.user?.userId));
const { takeaway_merchant_business_status, takeaway_merchant_audit_status } = useDict(
	'takeaway_merchant_business_status',
	'takeaway_merchant_audit_status'
);

const businessStatusOptions = computed(() => takeaway_merchant_business_status.value || []);
const auditStatusOptions = computed(() => takeaway_merchant_audit_status.value || []);
const auditDelayMinSeconds = ref(DEFAULT_AUDIT_DELAY_MIN_SECONDS);
const auditDelayMaxSeconds = ref(DEFAULT_AUDIT_DELAY_MAX_SECONDS);

const auditPendingAlertText = computed(() => {
	return `当前为待审状态，系统将于${auditDelayMinSeconds.value}-${auditDelayMaxSeconds.value}秒内自动审批，页面会在收到通知后自动刷新`;
});

const getAuditStatusLabel = (status?: string) => {
	const target = auditStatusOptions.value.find((item: any) => String(item.value) === String(status || ''));
	return target?.label || status || '-';
};

const getAuditStatusTagType = (status?: string) => {
	if (status === '1') return 'success';
	if (status === '2') return 'danger';
	return 'warning';
};

const form = reactive({
	id: undefined as string | number | undefined,
	userId: undefined as string | number | undefined,
	merchantName: '',
	contactName: '',
	storeAddressId: '',
	businessStatus: '1',
	auditStatus: '0',
});

const rules = reactive({
	merchantName: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
	contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
	storeAddressId: [{ required: true, message: '请选择门店地址', trigger: 'change' }],
	businessStatus: [{ required: true, message: '请选择营业状态', trigger: 'change' }],
});

interface MerchantAuditWsMessage {
	category?: string;
	businessType?: string;
	eventType?: string;
	status?: string;
	title?: string;
	content?: string;
	merchantId?: string | number;
	userId?: string | number;
	auditStatus?: string;
}

const validateAddressField = async () => {
	await nextTick();
	formRef.value?.validateField('storeAddressId');
};

const buildAddressOptionLabel = (item: any) => {
	const addressText = [item?.province, item?.city, item?.district, item?.detailAddress].filter(Boolean).join(' ');
	if (addressText) {
		return `${addressText}（ID:${item.id}）`;
	}
	return `地址ID: ${item.id}`;
};

const loadAddressOptions = async () => {
	try {
		const res = await listAddress();
		const list = res?.data || [];
		addressOptions.value = list
			.filter((item: any) => item?.id !== undefined && item?.id !== null)
			.map((item: any) => ({
				label: buildAddressOptionLabel(item),
				value: String(item.id),
			}));
	} catch {
		addressOptions.value = [];
		useMessage().warning('地址列表加载失败，请稍后重试');
	}
};

const goAddressPage = async () => {
	const candidates = ['/address/index'];
	for (const path of candidates) {
		try {
			await router.push(path);
			return;
		} catch {
			// 尝试下一个候选路由
		}
	}
	useMessage().warning('未找到地址管理页面，请联系管理员配置菜单路由');
};

const resetForm = () => {
	form.id = undefined;
	form.userId = undefined;
	form.merchantName = '';
	form.contactName = '';
	form.storeAddressId = '';
	form.businessStatus = '1';
	form.auditStatus = '0';
};

const normalizePositiveInt = (value: unknown, fallback: number) => {
	const num = Number(value);
	if (!Number.isFinite(num) || num <= 0) {
		return fallback;
	}
	return Math.floor(num);
};

const loadAuditDelayRange = async () => {
	try {
		const [minRes, maxRes] = await Promise.all([
			getPublicValue(PARAM_MERCHANT_AUTO_AUDIT_DELAY_MIN_SECONDS),
			getPublicValue(PARAM_MERCHANT_AUTO_AUDIT_DELAY_MAX_SECONDS),
		]);

		const minValue = normalizePositiveInt(minRes?.data, DEFAULT_AUDIT_DELAY_MIN_SECONDS);
		const maxValue = normalizePositiveInt(maxRes?.data, DEFAULT_AUDIT_DELAY_MAX_SECONDS);

		auditDelayMinSeconds.value = minValue;
		auditDelayMaxSeconds.value = maxValue < minValue ? minValue : maxValue;
	} catch {
		auditDelayMinSeconds.value = DEFAULT_AUDIT_DELAY_MIN_SECONDS;
		auditDelayMaxSeconds.value = DEFAULT_AUDIT_DELAY_MAX_SECONDS;
	}
};

const loadCurrent = async (showLoading = true) => {
	if (showLoading) {
		loading.value = true;
	}
	try {
		const res = await currentMerchant();
		const data = res?.data || {};
		const noExist = !!data.noExist || !data.id;
		if (noExist) {
			isCreateMode.value = true;
			resetForm();
			return;
		}

		isCreateMode.value = false;
		form.id = data.id;
		form.userId = data.userId;
		form.merchantName = data.merchantName || '';
		form.contactName = data.contactName || '';
		form.storeAddressId = data.storeAddressId ? String(data.storeAddressId) : '';
		form.businessStatus = data.businessStatus || '1';
		form.auditStatus = data.auditStatus || '0';
	} finally {
		if (showLoading) {
			loading.value = false;
			await validateAddressField();
		}
	}
};

const parseWsPayload = (raw: unknown): MerchantAuditWsMessage | null => {
	if (typeof raw !== 'string' || !raw.trim().startsWith('{')) {
		return null;
	}
	try {
		return JSON.parse(raw) as MerchantAuditWsMessage;
	} catch {
		return null;
	}
};

const isCurrentMerchantAuditApprovedMessage = (payload: MerchantAuditWsMessage | null): boolean => {
	if (!payload) return false;
	if (payload.category !== 'BUSINESS') return false;
	if (payload.businessType !== 'MERCHANT') return false;
	if (payload.eventType !== 'AUDIT_RESULT') return false;
	if (payload.status !== 'SUCCESS') return false;
	if (payload.auditStatus !== '1') return false;

	const loginUserId = currentMerchantUserId.value;
	if (payload.userId && loginUserId && normalizeId(payload.userId) !== loginUserId) {
		return false;
	}
	if (payload.merchantId && form.id && normalizeId(payload.merchantId) !== normalizeId(form.id)) {
		return false;
	}
	return true;
};

const buildWsTipText = (payload: MerchantAuditWsMessage) => {
	const parts = [payload.title, payload.content].filter((item) => !!item && item.trim().length > 0);
	return parts.join(' - ');
};

watch(
	() => msgStore.msgArray.length,
	async (len, prevLen) => {
		if (len <= prevLen) {
			return;
		}
		const newMessages = msgStore.msgArray.slice(prevLen, len);
		for (const item of newMessages) {
			const rawText = (item as any)?.value;
			const payload = parseWsPayload(rawText);
			if (!isCurrentMerchantAuditApprovedMessage(payload)) {
				continue;
			}
			await loadCurrent(false);
			if (payload) {
				const tipText = buildWsTipText(payload);
				if (tipText) {
					useMessage().success(tipText);
				}
			}
			break;
		}
	}
);

const handleSubmit = async () => {
	await formRef.value.validate(async (valid: boolean) => {
		if (!valid) return;

		submitting.value = true;
		try {
			const currentUserId = useUserInfo().userInfos?.user?.userId;
			const payload: any = {
				merchantName: form.merchantName,
				contactName: form.contactName,
				storeAddressId: form.storeAddressId,
				businessStatus: form.businessStatus,
			};

			if (isCreateMode.value) {
				payload.userId = currentUserId;
				await applyMerchant(payload);
				useMessage().success('已提交入驻申请，状态已置为待审，系统将自动审批');
			} else {
				payload.id = form.id;
				payload.userId = form.userId || currentUserId;
				await updateMerchant(payload);
				useMessage().success('已提交更新，状态已重置为待审，系统将自动审批');
			}

			await loadCurrent();
		} catch (error: any) {
			useMessage().error(error?.msg || error?.response?.data?.msg || '提交失败');
		} finally {
			submitting.value = false;
		}
	});
};

onMounted(async () => {
	loadAuditDelayRange();
	loadAddressOptions();
	await loadCurrent();
});
</script>

<style scoped>
.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.address-empty-tip {
	margin-top: 8px;
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12px;
}

.address-empty-tip :deep(.el-alert) {
	flex: 1;
}
</style>
