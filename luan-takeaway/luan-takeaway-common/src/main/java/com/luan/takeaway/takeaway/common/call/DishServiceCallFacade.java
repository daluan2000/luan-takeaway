package com.luan.takeaway.takeaway.common.call;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeUpsertRequest;
import com.luan.takeaway.takeaway.common.dto.HybridDishCandidateDTO;
import com.luan.takeaway.takeaway.common.dto.HybridDishSearchRequest;
import com.luan.takeaway.takeaway.common.entity.WmDish;

import java.util.List;

/**
 * 菜品服务调用门面接口
 * <p>
 * 定义菜品服务的所有调用方法，由具体实现类提供本地调用或远程调用
 *
 * @author luan
 */
public interface DishServiceCallFacade {

	/**
	 * 扣减库存
	 * @param request 扣减库存请求
	 * @return 是否成功
	 */
	R<Boolean> deductStock(DeductStockRequest request);

	/**
	 * 按ID列表查询菜品
	 * @param merchantUserId 商家用户ID
	 * @param ids 菜品ID列表
	 * @return 菜品列表
	 */
	R<List<WmDish>> listByIds(Long merchantUserId, List<Long> ids);

	/**
	 * 分页查询菜品列表
	 * @param current 当前页
	 * @param size 每页大小
	 * @param dishName 菜品名称（模糊）
	 * @param merchantUserId 商家用户ID
	 * @param saleStatus 上架状态
	 * @return 菜品分页结果
	 */
	R<Page<WmDish>> pageDish(long current, long size, String dishName, Long merchantUserId, String saleStatus);

	/**
	 * 内部分页查询菜品列表（免用户令牌）
	 * @param current 当前页
	 * @param size 每页大小
	 * @param dishName 菜品名称（模糊）
	 * @param merchantUserId 商家用户ID
	 * @param saleStatus 上架状态
	 * @return 菜品分页结果
	 */
	R<Page<WmDish>> servicePageDish(long current, long size, String dishName, Long merchantUserId, String saleStatus);

	/**
	 * 混合检索候选菜品
	 * @param request 结构化检索条件
	 * @return 候选集合（含知识文档）
	 */
	R<List<HybridDishCandidateDTO>> searchHybridCandidates(HybridDishSearchRequest request);

	/**
	 * 查询菜品知识文档
	 * @param dishId 菜品ID
	 * @return 知识文档
	 */
	R<DishKnowledgeDoc> getKnowledgeDoc(Long dishId);

	/**
	 * 新增或更新菜品知识文档
	 * @param request upsert 请求
	 * @return 是否成功
	 */
	R<Boolean> upsertKnowledgeDoc(DishKnowledgeUpsertRequest request);

}
