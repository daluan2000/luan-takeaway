import request from '/@/utils/request';

export function mediaSharePage(query?: Object) {
	return request({
		url: '/media/shares/page',
		method: 'get',
		params: query,
	});
}

export function createMediaShare(obj?: Object) {
	return request({
		url: '/media/shares',
		method: 'post',
		data: obj,
	});
}

export function disableMediaShare(id: number | string) {
	return request({
		url: `/media/shares/${id}`,
		method: 'delete',
	});
}

export function shareDetail(token: string) {
	return request({
		url: `/media/shares/${token}`,
		method: 'get',
		headers: {
			skipToken: true,
		},
	});
}

export function shareVerify(token: string, code?: string) {
	return request({
		url: `/media/shares/${token}/verify`,
		method: 'post',
		data: { code },
		headers: {
			skipToken: true,
		},
	});
}
