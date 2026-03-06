import request from '/@/utils/request';

export function pageList(params?: object) {
	return request({
		url: '/takeaway/user/merchant/page',
		method: 'get',
		params,
	});
}

export function applyMerchant(data?: object) {
	return request({
		url: '/takeaway/user/merchant/apply',
		method: 'post',
		data,
	});
}

export function updateMerchant(data?: object) {
	return request({
		url: '/takeaway/user/merchant',
		method: 'put',
		data,
	});
}

export function currentMerchant() {
	return request({
		url: '/takeaway/user/merchant/current',
		method: 'get',
	});
}

export function auditMerchant(id: string | number, auditStatus: string) {
	return request({
		url: `/takeaway/user/merchant/${id}/audit/${auditStatus}`,
		method: 'post',
	});
}

export function updateBusinessStatus(id: string | number, businessStatus: string) {
	return request({
		url: `/takeaway/user/merchant/${id}/business/${businessStatus}`,
		method: 'post',
	});
}

export function acceptMerchantOrder(orderId: string | number) {
	return request({
		url: `/takeaway/user/merchant/order/${orderId}/accept`,
		method: 'post',
	});
}
