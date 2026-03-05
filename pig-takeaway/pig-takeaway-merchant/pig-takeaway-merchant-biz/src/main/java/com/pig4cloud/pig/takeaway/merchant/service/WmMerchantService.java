package com.pig4cloud.pig.takeaway.merchant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.takeaway.common.entity.WmMerchantUserExt;
import com.pig4cloud.pig.takeaway.merchant.dto.WmMerchantDTO;

public interface WmMerchantService {

	WmMerchantDTO createMerchant(WmMerchantDTO merchantDTO);

	boolean apply(WmMerchantUserExt merchant);

	boolean audit(Long id, String auditStatus);

	boolean updateMerchant(WmMerchantDTO merchantDTO);

	Page<WmMerchantUserExt> page(Page<WmMerchantUserExt> page, Long userId, String auditStatus, String businessStatus);

	boolean updateBusinessStatus(Long id, String businessStatus);

	boolean acceptOrder(Long orderId);

}
