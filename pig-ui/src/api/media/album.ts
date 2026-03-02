import request from '/@/utils/request';

export function mediaAlbumPage(query?: Object) {
	return request({
		url: '/media/albums/page',
		method: 'get',
		params: query,
	});
}

export function createMediaAlbum(obj?: Object) {
	return request({
		url: '/media/albums',
		method: 'post',
		data: obj,
	});
}

export function updateMediaAlbum(id: number | string, obj?: Object) {
	return request({
		url: `/media/albums/${id}`,
		method: 'put',
		data: obj,
	});
}

export function deleteMediaAlbum(id: number | string) {
	return request({
		url: `/media/albums/${id}`,
		method: 'delete',
	});
}

export function albumItems(id: number | string) {
	return request({
		url: `/media/albums/${id}/items`,
		method: 'get',
	});
}

export function addAlbumItems(id: number | string, fileIds: Array<number | string>) {
	return request({
		url: `/media/albums/${id}/items`,
		method: 'post',
		data: { fileIds },
	});
}

export function removeAlbumItem(id: number | string, fileId: number | string) {
	return request({
		url: `/media/albums/${id}/items/${fileId}`,
		method: 'delete',
	});
}

export function sortAlbumItems(id: number | string, list: Array<{ id: number | string; sortNo: number }>) {
	return request({
		url: `/media/albums/${id}/items/sort`,
		method: 'put',
		data: list,
	});
}
