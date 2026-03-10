const ORDER_TIME_FIELD_CONFIG = [
	{ label: '下单', keys: ['createTime'] },
	{ label: '支付', keys: ['payTime', 'paymentTime'] },
	{ label: '接单', keys: ['merchantAcceptTime', 'acceptTime'] },
	{ label: '配送', keys: ['deliveryStartTime'] },
	{ label: '完成', keys: ['finishTime', 'deliveryFinishTime'] },
] as const;

const getTextValue = (value: unknown): string => {
	if (value === null || value === undefined) {
		return '-';
	}
	const text = String(value).trim();
	return text || '-';
};

const getOrderFieldValue = (row: Record<string, unknown>, keys: readonly string[]): string => {
	for (const key of keys) {
		const value = row[key];
		if (value !== null && value !== undefined && String(value).trim()) {
			return String(value).trim();
		}
	}
	return '-';
};

export const getOrderTimelineLines = (row: Record<string, unknown>): string[] => {
	return ORDER_TIME_FIELD_CONFIG.map((item) => `${item.label}：${getOrderFieldValue(row, item.keys)}`);
};

export const getOrderTimelineText = (row: Record<string, unknown>): string => {
	return getTextValue(getOrderTimelineLines(row).join('\n'));
};
