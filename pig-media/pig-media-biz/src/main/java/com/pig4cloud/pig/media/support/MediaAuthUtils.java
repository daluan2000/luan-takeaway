package com.pig4cloud.pig.media.support;

import com.pig4cloud.pig.common.security.service.PigUser;
import com.pig4cloud.pig.common.security.util.SecurityUtils;

/**
 * 图床业务鉴权上下文工具
 */
public final class MediaAuthUtils {

	private MediaAuthUtils() {
	}

	public static Long currentUserId() {
		PigUser user = SecurityUtils.getUser();
		if (user == null || user.getId() == null) {
			throw new IllegalStateException("未获取到当前登录用户");
		}
		return user.getId();
	}

}
