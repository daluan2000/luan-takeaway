package com.luan.takeaway.takeaway.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.takeaway.user.dto.WmMerchantDTO;

import java.util.List;

public interface WmMerchantService {

	WmMerchantDTO createMerchant(WmMerchantDTO merchantDTO);

	boolean apply(WmMerchantDTO merchant);

	boolean audit(Long id, String auditStatus);

	boolean updateMerchant(WmMerchantDTO merchantDTO);

	WmMerchantDTO current();

	Page<WmMerchantDTO> page(Page<WmMerchantDTO> page, Long userId, String auditStatus, String businessStatus,
			boolean includeDishList);

	List<WmMerchantDTO> listByRegion(String province, String city, String district, boolean includeDishList);

	boolean updateBusinessStatus(Long id, String businessStatus);

}
