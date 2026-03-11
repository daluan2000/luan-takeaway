package com.luan.takeaway.takeaway.dish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.entity.WmDish;

import java.util.List;

public interface WmDishService extends IService<WmDish> {

	boolean deductStock(DeductStockRequest request);

	List<WmDish> listByMerchantAndIds(Long merchantUserId, List<Long> ids);

}
