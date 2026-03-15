package com.luan.takeaway.takeaway.dish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.dto.HybridDishCandidateDTO;
import com.luan.takeaway.takeaway.common.dto.HybridDishSearchRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import com.luan.takeaway.takeaway.common.entity.WmDish;

import java.util.List;
import java.util.Map;

public interface WmDishService extends IService<WmDish> {

	Page<WmDish> pageByQuery(Page<WmDish> page, WmDish query);

	boolean deductStock(DeductStockRequest request);

	boolean asyncDeductStockToDb(Map<Long, Integer> buyCountMap, Long merchantUserId);

	String buildConsumeDoneKey(String orderNo);

	List<WmDish> listByMerchantAndIds(Long merchantUserId, List<Long> ids);

	DishKnowledgeDoc getKnowledgeDoc(Long dishId);

	boolean upsertKnowledgeDoc(Long dishId, DishKnowledgeDoc doc);

	List<HybridDishCandidateDTO> searchHybridCandidates(HybridDishSearchRequest request);

	DishKnowledgeDoc generateKnowledgeDocSync(WmDish dish);

}
