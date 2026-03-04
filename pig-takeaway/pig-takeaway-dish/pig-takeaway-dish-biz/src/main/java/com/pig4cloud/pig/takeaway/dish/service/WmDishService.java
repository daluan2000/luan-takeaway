package com.pig4cloud.pig.takeaway.dish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.takeaway.common.dto.DeductStockRequest;
import com.pig4cloud.pig.takeaway.common.entity.WmDish;

import java.util.List;

public interface WmDishService extends IService<WmDish> {

	boolean deductStock(DeductStockRequest request);

	List<WmDish> listByMerchantAndIds(Long merchantUserId, List<Long> ids);

}
