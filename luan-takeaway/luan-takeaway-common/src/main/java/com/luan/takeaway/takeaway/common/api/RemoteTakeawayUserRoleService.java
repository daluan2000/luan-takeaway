package com.luan.takeaway.takeaway.common.api;

import com.luan.takeaway.common.core.constant.ServiceNameConstants;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.feign.annotation.NoToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "remoteTakeawayUserRoleService", value = ServiceNameConstants.UPMS_SERVICE)
public interface RemoteTakeawayUserRoleService {

	@NoToken
	@PostMapping("/user/internal/role/switch")
	R<Boolean> switchRole(@RequestParam("userId") Long userId, @RequestParam("roleCode") String roleCode);

}
