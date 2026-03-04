package com.pig4cloud.pig.takeaway.merchant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.takeaway.common.entity.WmMerchantUserExt;

public interface WmMerchantService {

	boolean apply(WmMerchantUserExt merchant);

	boolean audit(Long id, String auditStatus);

	boolean updateMerchant(WmMerchantUserExt merchant);

	Page<WmMerchantUserExt> page(Page<WmMerchantUserExt> page, Long userId, String auditStatus, String businessStatus);

	boolean updateBusinessStatus(Long id, String businessStatus);

	boolean acceptOrder(Long orderId);

}
