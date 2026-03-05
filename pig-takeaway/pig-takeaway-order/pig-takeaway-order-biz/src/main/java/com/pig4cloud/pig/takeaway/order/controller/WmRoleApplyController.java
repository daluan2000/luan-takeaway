package com.pig4cloud.pig.takeaway.order.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.takeaway.common.api.TakeawayApiConstants;
import com.pig4cloud.pig.takeaway.common.dto.RoleApplyRequest;
import com.pig4cloud.pig.takeaway.order.service.WmRoleApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Tag(name = "角色申请服务")
public class WmRoleApplyController {

	private final WmRoleApplyService wmRoleApplyService;

	@PostMapping(TakeawayApiConstants.ROLE_PATH + "/apply")
	@Operation(summary = "申请成为商家/骑手/客户")
	@SysLog("申请成为商家/骑手/客户")
	public R<Boolean> apply(@RequestBody RoleApplyRequest request) {
		return R.ok(wmRoleApplyService.apply(request));
	}

}
