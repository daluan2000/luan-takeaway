import request from '/@/utils/request';

export function pageList(params?: object) {
	return request({
		url: '/takeaway/merchant/page',
		method: 'get',
		params,
	});
}

export function applyMerchant(data?: object) {
	return request({
		url: '/takeaway/merchant/apply',
		method: 'post',
		data,
	});
}

export function updateMerchant(data?: object) {
	return request({
		url: '/takeaway/merchant',
		method: 'put',
		data,
	});
}

export function auditMerchant(id: string | number, auditStatus: string) {
	return request({
		url: `/takeaway/merchant/${id}/audit/${auditStatus}`,
		method: 'post',
	});
}

export function updateBusinessStatus(id: string | number, businessStatus: string) {
	return request({
		url: `/takeaway/merchant/${id}/business/${businessStatus}`,
		method: 'post',
	});
}

export function acceptMerchantOrder(orderId: string | number) {
	return request({
		url: `/takeaway/merchant/order/${orderId}/accept`,
		method: 'post',
	});
}
