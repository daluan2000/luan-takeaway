package com.luan.takeaway.takeaway.dish.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.feign.annotation.NoToken;
import com.luan.takeaway.takeaway.common.api.TakeawayApiConstants;
import com.luan.takeaway.takeaway.common.constant.TakeawayServiceNameConstants;
import com.luan.takeaway.takeaway.common.dto.DeductStockRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeUpsertRequest;
import com.luan.takeaway.takeaway.common.dto.HybridDishCandidateDTO;
import com.luan.takeaway.takeaway.common.dto.HybridDishSearchRequest;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 菜品服务 Feign 接口
 *
 * @author pig
 */
@FeignClient(contextId = "remoteDishService", value = TakeawayServiceNameConstants.TAKEAWAY_DISH_SERVICE)
public interface RemoteDishService {

	/**
	 * 扣减库存
	 * @param request 扣减库存请求
	 * @return 是否成功
	 */
	@PostMapping(TakeawayApiConstants.DISH_PATH + "/service/stock/deduct")
	R<Boolean> deductStock(@RequestBody DeductStockRequest request);

	/**
	 * 按ID列表查询菜品
	 * @param merchantUserId 商家用户ID
	 * @param ids 菜品ID列表
	 * @return 菜品列表
	 */
	@GetMapping(TakeawayApiConstants.DISH_PATH + "/service/ids")
	R<List<WmDish>> listByIds(@RequestParam("merchantUserId") Long merchantUserId,
			@RequestParam("ids") List<Long> ids);

	/**
	 * 分页查询菜品列表
	 * @param current 当前页
	 * @param size 每页大小
	 * @param dishName 菜品名称（模糊）
	 * @param merchantUserId 商家用户ID
	 * @param saleStatus 上架状态
	 * @return 菜品分页结果
	 */
	@GetMapping("/dish/page")
	R<Page<WmDish>> page(@RequestParam("current") long current, @RequestParam("size") long size,
			@RequestParam(value = "dishName", required = false) String dishName,
			@RequestParam(value = "merchantUserId", required = false) Long merchantUserId,
			@RequestParam(value = "saleStatus", required = false) String saleStatus);

	/**
	 * 混合检索候选菜品
	 * @param request 结构化检索条件
	 * @return 候选集合（含知识文档）
	 */
	@PostMapping(TakeawayApiConstants.DISH_PATH + "/service/hybrid/candidates")
	R<List<HybridDishCandidateDTO>> searchHybridCandidates(@RequestBody HybridDishSearchRequest request);

	/**
	 * 查询菜品知识文档
	 * @param dishId 菜品ID
	 * @return 知识文档
	 */
	@GetMapping(TakeawayApiConstants.DISH_PATH + "/service/knowledge/doc")
	R<DishKnowledgeDoc> getKnowledgeDoc(@RequestParam("dishId") Long dishId);

	/**
	 * 新增或更新菜品知识文档
	 *
	 * 该接口会被 AI 服务的 MQ 消费线程调用（非 Servlet 请求线程）。
	 * common-feign 默认会通过 PigOAuthRequestInterceptor 从当前 HttpServletRequest
	 * 中解析并透传 Authorization；而 MQ 场景没有 request 上下文，会导致无法取到 token。
	 *
	 * 加上 @NoToken 后，PigFeignInnerRequestInterceptor 会自动添加 from=Y，
	 * 从而让 PigOAuthRequestInterceptor 跳过 token 透传逻辑，适配内部异步调用。
	 * @param request upsert 请求
	 * @return 是否成功
	 */
	@NoToken
	@PostMapping(TakeawayApiConstants.DISH_PATH + "/service/knowledge/upsert")
	R<Boolean> upsertKnowledgeDoc(@RequestBody DishKnowledgeUpsertRequest request);

}
