import request from '/@/utils/request';

export const pageList = (params?: Object) => {
	return request({
		url: '/admin/user/page',
		method: 'get',
		params,
	});
};

export const addObj = (obj: Object) => {
	return request({
		url: '/admin/user',
		method: 'post',
		data: obj,
	});
};

export const getObj = (id: String) => {
	return request({
		url: '/admin/user/details/' + id,
		method: 'get',
	});
};

export const delObj = (ids: Object) => {
	return request({
		url: '/admin/user',
		method: 'delete',
		data: ids,
	});
};

export const putObj = (obj: Object) => {
	return request({
		url: '/admin/user',
		method: 'put',
		data: obj,
	});
};

export const updateUserRole = (obj: Object) => {
	return request({
		url: '/admin/user/role',
		method: 'put',
		data: obj,
	});
};

export function getDetails(obj: Object) {
	return request({
		url: '/admin/user/details',
		method: 'get',
		params: obj,
	});
}

// 更改个人信息
export function editInfo(obj: Object) {
	return request({
		url: '/admin/user/edit',
		method: 'put',
		data: obj,
	});
}

export function password(obj: Object) {
	return request({
		url: '/admin/user/password',
		method: 'put',
		data: obj,
	});
}

export function UnbindingUser(type) {
	return request({
		url: '/admin/user/unbinding',
		method: 'post',
		params: {
			type,
		},
	});
}

export function checkPassword(password: string) {
	return request({
		url: '/admin/user/check',
		method: 'post',
		params: {
			password,
		},
	});
}

/**
 * 注册用户
 */
export const registerUser = (userInfo: object) => {
	return request({
		url: '/admin/register/user',
		method: 'post',
		data: userInfo,
	});
};

export function validateUsername(rule: any, value: any, callback: any, isEdit: boolean) {
	const flag = /^(?!-)[A-Za-z0-9-_]+$/.test(value) && !value.endsWith('-');
	if (!flag) {
		callback(new Error('用户名仅支持英文大小写、数字、连字符(-)、下划线(_)，且不能以连字符开头或结尾'));
		return;
	}

	if (isEdit) {
		return callback();
	}

	getDetails({ username: value }).then((response) => {
		const result = response.data;
		if (result !== null) {
			callback(new Error('用户名已经存在'));
		} else {
			callback();
		}
	});
}

export function validatePhone(rule: any, value: any, callback: any, isEdit: boolean) {
	if (isEdit) {
		return callback();
	}
	getDetails({ phone: value }).then((response) => {
		const result = response.data;
		if (result !== null) {
			callback(new Error('手机号已经存在'));
		} else {
			callback();
		}
	});
}

/**
 * 批量注册用户（通过JSON文件）
 */
export const batchRegister = (users: object) => {
	return request({
		url: '/admin/user/batch/register',
		method: 'post',
		data: users,
	});
};
