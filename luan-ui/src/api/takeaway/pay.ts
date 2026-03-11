import request from '/@/utils/request';

export function pageList(params?: object) {
	return request({
		url: '/takeaway/pay/page',
		method: 'get',
		params,
	});
}

export function mockPay(data?: object) {
	return request({
		url: '/takeaway/pay/mock',
		method: 'post',
		data,
	});
}
