import request from '/@/utils/request';

export function pageList(params?: object) {
	return request({
		url: '/takeaway/user/delivery/order/page',
		method: 'get',
		params,
	});
}

export function saveRider(data?: object) {
	return request({
		url: '/takeaway/user/delivery/rider',
		method: 'post',
		data,
	});
}

export function currentRider() {
	return request({
		url: '/takeaway/user/delivery/rider/current',
		method: 'get',
	});
}

export function acceptOrder(orderId: string | number, deliveryUserId: string | number) {
	return request({
		url: `/takeaway/user/delivery/order/${orderId}/accept/${deliveryUserId}`,
		method: 'post',
	});
}

export function completeOrder(orderId: string | number, deliveryUserId: string | number) {
	return request({
		url: `/takeaway/user/delivery/order/${orderId}/complete/${deliveryUserId}`,
		method: 'post',
	});
}
