package com.luan.takeaway.auth.support.core;

import com.luan.takeaway.common.core.constant.SecurityConstants;
import com.luan.takeaway.common.security.service.PigUser;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * OAuth2 Token 自定义增强实现类
 *
 * @author lengleng
 * @date 2025/05/30
 */
public class CustomeOAuth2TokenCustomizer implements OAuth2TokenCustomizer<OAuth2TokenClaimsContext> {
	// customize 方法会在生成 OAuth2 Token时被调用，可以在这个方法中对 Token 的属性进行自定义增强，比如添加一些自定义的 Claim，或者修改一些默认的 Claim。
	/**
	 * 自定义OAuth 2.0 Token属性
	 * @param context 包含OAuth 2.0 Token属性的上下文
	 */
	@Override
	public void customize(OAuth2TokenClaimsContext context) {
		OAuth2TokenClaimsSet.Builder claims = context.getClaims();
		claims.claim(SecurityConstants.DETAILS_LICENSE, SecurityConstants.PROJECT_LICENSE);
		String clientId = context.getAuthorizationGrant().getName();
		claims.claim(SecurityConstants.CLIENT_ID, clientId);
		// 客户端模式不返回具体用户信息
		if (SecurityConstants.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType().getValue())) {
			return;
		}

		PigUser pigUser = (PigUser) context.getPrincipal().getPrincipal();
		claims.claim(SecurityConstants.DETAILS_USER, pigUser);
		claims.claim(SecurityConstants.DETAILS_USER_ID, pigUser.getId());
		claims.claim(SecurityConstants.USERNAME, pigUser.getUsername());
	}

}
