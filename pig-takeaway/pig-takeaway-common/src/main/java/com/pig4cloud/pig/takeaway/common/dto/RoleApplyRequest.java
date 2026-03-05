package com.pig4cloud.pig.takeaway.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoleApplyRequest {

	private String roleCode;

	private String merchantName;

	private String contactName;

	private Long storeAddressId;

	private String businessStatus;

	private String merchantAuditStatus;

	private String realName;

	private BigDecimal deliveryScopeKm;

	private String onlineStatus;

	private String employmentStatus;

	private Long defaultAddressId;

}
