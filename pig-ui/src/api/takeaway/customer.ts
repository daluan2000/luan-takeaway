import request from '/@/utils/request';

export function currentCustomer() {
	return request({
		url: '/takeaway/user/customer/current',
		method: 'get',
	});
}
