import request from '/@/utils/request';

export function pageList(params?: object) {
	return request({
		url: '/takeaway/dish/page',
		method: 'get',
		params,
	});
}

export function addObj(data?: object) {
	return request({
		url: '/takeaway/dish',
		method: 'post',
		data,
	});
}

export function putObj(data?: object) {
	return request({
		url: '/takeaway/dish',
		method: 'put',
		data,
	});
}

export function delObj(id: string | number) {
	return request({
		url: `/takeaway/dish/${id}`,
		method: 'delete',
	});
}

export function saleOn(id: string | number) {
	return request({
		url: `/takeaway/dish/${id}/sale-on`,
		method: 'post',
	});
}

export function saleOff(id: string | number) {
	return request({
		url: `/takeaway/dish/${id}/sale-off`,
		method: 'post',
	});
}
