// 订单时间轴展示配置。
// keys 支持兼容多个后端字段命名，便于不同接口返回结构复用同一渲染逻辑。
const ORDER_TIME_FIELD_CONFIG = [
	{ label: '下单', keys: ['createTime'] },
	{ label: '支付', keys: ['payTime', 'paymentTime'] },
	{ label: '接单', keys: ['merchantAcceptTime', 'acceptTime'] },
	{ label: '配送', keys: ['deliveryStartTime'] },
	{ label: '完成', keys: ['finishTime', 'deliveryFinishTime'] },
	{ label: '取消', keys: ['cancelTime'] },
] as const;

const getTextValue = (value: unknown): string => {
	// 统一把空值渲染为 "-"，避免页面出现 undefined/null。
	if (value === null || value === undefined) {
		return '-';
	}
	const text = String(value).trim();
	return text || '-';
};

const getOrderFieldValue = (row: Record<string, unknown>, keys: readonly string[]): string => {
	// 按优先级逐个尝试字段，命中第一个有值字段即返回。
	for (const key of keys) {
		const value = row[key];
		if (value !== null && value !== undefined && String(value).trim()) {
			return String(value).trim();
		}
	}
	return '-';
};

export const getOrderTimelineLines = (row: Record<string, unknown>): string[] => {
	// 组装成“标签：值”结构，供列表单行/tooltip 复用。
	return ORDER_TIME_FIELD_CONFIG.map((item) => `${item.label}：${getOrderFieldValue(row, item.keys)}`);
};

export const getOrderTimelineText = (row: Record<string, unknown>): string => {
	// 默认使用换行连接，适配 tooltip 的多行展示。
	return getTextValue(getOrderTimelineLines(row).join('\n'));
};
