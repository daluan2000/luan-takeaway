import request from '/@/utils/request';

export function uploadMediaFile(file: File) {
	const formData = new FormData();
	formData.append('file', file);
	return request({
		url: '/media/files/upload',
		method: 'post',
		data: formData,
		headers: {
			'Content-Type': 'multipart/form-data',
		},
	});
}

export function mediaFilePage(query?: Object) {
	return request({
		url: '/media/files/page',
		method: 'get',
		params: query,
	});
}

export function deleteMediaFile(id: number | string) {
	return request({
		url: `/media/files/${id}`,
		method: 'delete',
	});
}

export function getMediaFileDownloadUrl(id: number | string) {
	return request({
		url: `/media/files/${id}/download-url`,
		method: 'get',
	});
}

export function mediaFilePresign(obj?: Object) {
	return request({
		url: '/media/files/presign',
		method: 'post',
		data: obj,
	});
}

export function mediaFileConfirm(obj?: Object) {
	return request({
		url: '/media/files/confirm',
		method: 'post',
		data: obj,
	});
}
