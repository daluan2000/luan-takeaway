import request from '/@/utils/request';

export function listAddress() {
	return request({
		url: '/takeaway/user/address/list',
		method: 'get',
	});
}

export function getAddress(id: string | number) {
	return request({
		url: `/takeaway/user/address/${id}`,
		method: 'get',
	});
}

export function addAddress(data?: object) {
	return request({
		url: '/takeaway/user/address',
		method: 'post',
		data,
	});
}

export function updateAddress(data?: object) {
	return request({
		url: '/takeaway/user/address',
		method: 'put',
		data,
	});
}

export function delAddress(id: string | number) {
	return request({
		url: `/takeaway/user/address/${id}`,
		method: 'delete',
	});
}
