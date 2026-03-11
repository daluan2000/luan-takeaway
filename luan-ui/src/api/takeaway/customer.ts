import request from '/@/utils/request';

export function createCustomer(data?: object) {
	return request({
		url: '/takeaway/user/customer',
		method: 'post',
		data,
	});
}

export function updateCustomer(data?: object) {
	return request({
		url: '/takeaway/user/customer',
		method: 'put',
		data,
	});
}

export function currentCustomer() {
	return request({
		url: '/takeaway/user/customer/current',
		method: 'get',
	});
}
