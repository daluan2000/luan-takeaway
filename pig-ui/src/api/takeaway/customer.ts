import request from '/@/utils/request';

export function currentCustomer() {
	return request({
		url: '/takeaway/customer/current',
		method: 'get',
	});
}
