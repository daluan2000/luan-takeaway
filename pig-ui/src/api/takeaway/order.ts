import request from '/@/utils/request';

export function pageList(params?: object) {
	return request({
		url: '/takeaway/order/page',
		method: 'get',
		params,
	});
}

export function createOrder(data?: object) {
	return request({
		url: '/takeaway/order',
		method: 'post',
		data,
	});
}

export function getOrderDetail(orderId: string | number) {
	return request({
		url: `/takeaway/order/${orderId}`,
		method: 'get',
	});
}

export function cancelOrder(orderId: string | number) {
	return request({
		url: `/takeaway/order/${orderId}/cancel`,
		method: 'post',
	});
}
