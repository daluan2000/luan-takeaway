package com.pig4cloud.pig.takeaway.common.api;

import com.pig4cloud.pig.common.core.constant.ServiceNameConstants;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.feign.annotation.NoToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "remoteTakeawayUserRoleService", value = ServiceNameConstants.UPMS_SERVICE)
public interface RemoteTakeawayUserRoleService {

	@NoToken
	@PostMapping("/user/internal/role/switch")
	R<Boolean> switchRole(@RequestParam("userId") Long userId, @RequestParam("roleCode") String roleCode);

}
