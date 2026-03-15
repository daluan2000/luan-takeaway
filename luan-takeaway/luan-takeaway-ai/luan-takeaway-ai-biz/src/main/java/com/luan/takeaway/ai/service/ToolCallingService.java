package com.luan.takeaway.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luan.takeaway.ai.api.dto.RecommendationItem;
import com.luan.takeaway.takeaway.dish.api.RemoteDishService;
import com.luan.takeaway.takeaway.merchant.api.RemoteMerchantService;
import com.luan.takeaway.takeaway.order.api.RemoteOrderService;
import com.luan.takeaway.ai.model.IntentResult;
import com.luan.takeaway.common.core.util.R;
import com.luan.takeaway.common.security.service.PigUser;
import com.luan.takeaway.common.security.util.SecurityUtils;
import com.luan.takeaway.takeaway.common.constant.TakeawayStatusConstants;
import com.luan.takeaway.takeaway.common.dto.OrderDTO;
import com.luan.takeaway.takeaway.common.entity.WmDish;
import com.luan.takeaway.takeaway.common.entity.WmOrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Tool Calling 推荐服务。
 * <p>
 * 跨模块交互说明：
 * 1) 通过 {@link RemoteDishService} 拉取在售菜品候选；
 * 2) 通过 {@link RemoteMerchantService} 过滤营业中商家；
 * 3) 通过 {@link RemoteOrderService} 拉取用户历史订单构建偏好权重；
 * 4) 最终调用 LLM 对候选做二次筛选排序。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolCallingService {

	private final RemoteDishService dishApi;

	private final RemoteMerchantService remoteMerchantService;

	private final RemoteOrderService orderApi;

	private final OpenAiIntentRecognizer openAiIntentRecognizer;

	/**
	 * 结构化推荐主入口。
	 * <p>
	 * strictMode=true 时对预算/口味等条件更严格，命中冲突会直接剔除。
	 */
	public List<RecommendationItem> recommend(IntentResult intent, Long merchantUserId, int limit, boolean strictMode) {
		List<WmDish> candidates = searchDishCandidates(intent, merchantUserId);
		Map<String, Integer> historyPreference = loadUserHistoryKeywordWeight();
		List<RecommendationItem> scored = new ArrayList<>(candidates.size());

		for (WmDish dish : candidates) {
			Double score = scoreDish(dish, intent, historyPreference, strictMode);
			if (score < 0D) {
				continue;
			}
			RecommendationItem item = toRecommendation(dish, score, intent, historyPreference);
			scored.add(item);
		}

		scored.sort(Comparator.comparing(RecommendationItem::getScore).reversed());
		if (scored.isEmpty()) {
			return scored;
		}

		int rerankPoolSize = Math.min(scored.size(), Math.max(limit * 4, 12));
		List<RecommendationItem> rerankPool = new ArrayList<>(scored.subList(0, rerankPoolSize));
		try {
			List<RecommendationItem> reranked = openAiIntentRecognizer.rerankRecommendations(intent, rerankPool, limit);
			log.debug("LLM 二次筛选完成, poolSize={}, finalSize={}", rerankPool.size(), reranked.size());
			return reranked;
		}
		catch (Exception e) {
			String reason = e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage()
					: e.getClass().getSimpleName();
			throw new IllegalStateException("LLM 二次筛选失败: " + reason, e);
		}
	}

	/**
	 * 候选召回。
	 * <p>
	 * 先用品类/关键词分页拉取，再在无结果时降级全量在售兜底，最后过滤非营业商家。
	 */
	private List<WmDish> searchDishCandidates(IntentResult intent, Long merchantUserId) {
		List<WmDish> result = new ArrayList<>();
		Set<Long> dedup = new LinkedHashSet<>();

		for (long page = 1; page <= 3; page++) {
			R<Page<WmDish>> response = dishApi.page(page, 30,
					intent.getCategory() != null ? intent.getCategory() : firstKeyword(intent.getKeywords()), merchantUserId,
					TakeawayStatusConstants.Dish.SALE_ON);
			if (response == null || response.getData() == null || response.getData().getRecords() == null) {
				continue;
			}
			for (WmDish dish : response.getData().getRecords()) {
				if (dish != null && dish.getId() != null && dedup.add(dish.getId())) {
					result.add(dish);
				}
			}
		}

		if (result.isEmpty()) {
			for (long page = 1; page <= 2; page++) {
				R<Page<WmDish>> response = dishApi.page(page, 30, null, merchantUserId,
						TakeawayStatusConstants.Dish.SALE_ON);
				if (response == null || response.getData() == null || response.getData().getRecords() == null) {
					continue;
				}
				for (WmDish dish : response.getData().getRecords()) {
					if (dish != null && dish.getId() != null && dedup.add(dish.getId())) {
						result.add(dish);
					}
				}
			}
		}

		return retainOpenMerchantDishes(result);
	}

	private String firstKeyword(List<String> keywords) {
		if (keywords == null || keywords.isEmpty()) {
			return null;
		}
		return keywords.get(0);
	}

	/**
	 * 只保留当前营业中的商家菜品。
	 */
	private List<WmDish> retainOpenMerchantDishes(List<WmDish> dishes) {
		if (dishes == null || dishes.isEmpty()) {
			return List.of();
		}

		Map<Long, Boolean> openStatusCache = new HashMap<>();
		List<WmDish> filtered = new ArrayList<>(dishes.size());
		for (WmDish dish : dishes) {
			if (dish == null || dish.getMerchantUserId() == null) {
				continue;
			}
			Long merchantId = dish.getMerchantUserId();
			boolean isOpen = openStatusCache.computeIfAbsent(merchantId, this::isMerchantOpen);
			if (isOpen) {
				filtered.add(dish);
			}
		}
		return filtered;
	}

	/**
	 * 调商家模块分页接口判断商家是否营业。
	 */
	private boolean isMerchantOpen(Long merchantUserId) {
		try {
			R<Page<com.luan.takeaway.takeaway.common.entity.WmMerchantUserExt>> response = remoteMerchantService.page(1, 1,
					merchantUserId, null, TakeawayStatusConstants.Merchant.BUSINESS_OPEN, false);
			return response != null
					&& response.getData() != null
					&& response.getData().getRecords() != null
					&& !response.getData().getRecords().isEmpty();
		}
		catch (Exception ex) {
			log.warn("校验商家营业状态失败, merchantUserId={}", merchantUserId, ex);
			return false;
		}
	}

	/**
	 * 单菜品打分。
	 * <p>
	 * 由预算、品类、辣度、清淡偏好、关键词命中、历史偏好共同构成。
	 */
	private Double scoreDish(WmDish dish, IntentResult intent, Map<String, Integer> historyPreference, boolean strictMode) {
		double score = 0;
		String name = normalize(dish.getDishName());
		String desc = normalize(dish.getDishDesc());

		if (intent.getPriceMax() != null && dish.getPrice() != null
				&& dish.getPrice().compareTo(intent.getPriceMax()) > 0 && strictMode) {
			return -1D;
		}
		if (intent.getPriceMax() != null && dish.getPrice() != null) {
			score += dish.getPrice().compareTo(intent.getPriceMax()) <= 0 ? 2D : -0.5D;
		}

		if (intent.getCategory() != null && (name.contains(intent.getCategory()) || desc.contains(intent.getCategory()))) {
			score += 3D;
		}

		if (Boolean.FALSE.equals(intent.getSpicy()) && isSpicy(name, desc)) {
			return strictMode ? -1D : score - 2D;
		}
		if (Boolean.TRUE.equals(intent.getSpicy()) && isSpicy(name, desc)) {
			score += 1.5D;
		}

		if (Boolean.TRUE.equals(intent.getPreferLight())) {
			score += isLightFood(name, desc) ? 2D : -0.5D;
		}

		if (intent.getKeywords() != null) {
			for (String keyword : intent.getKeywords()) {
				if (keyword == null || keyword.isBlank()) {
					continue;
				}
				if (name.contains(keyword) || desc.contains(keyword)) {
					score += 1D;
				}
			}
		}

		for (Map.Entry<String, Integer> entry : historyPreference.entrySet()) {
			if (name.contains(entry.getKey()) || desc.contains(entry.getKey())) {
				score += Math.min(2, entry.getValue()) * 0.3D;
			}
		}

		return score;
	}

	/**
	 * 把内部菜品实体转换为对外推荐项。
	 */
	private RecommendationItem toRecommendation(WmDish dish, Double score, IntentResult intent,
			Map<String, Integer> historyPreference) {
		RecommendationItem item = new RecommendationItem();
		item.setDishId(dish.getId());
		item.setMerchantUserId(dish.getMerchantUserId());
		item.setDishName(dish.getDishName());
		item.setDishDesc(dish.getDishDesc());
		item.setPrice(dish.getPrice());
		item.setScore(score);

		List<String> tags = new ArrayList<>();
		if (isSpicy(normalize(dish.getDishName()), normalize(dish.getDishDesc()))) {
			tags.add("辣");
		}
		else {
			tags.add("不辣");
		}
		if (isLightFood(normalize(dish.getDishName()), normalize(dish.getDishDesc()))) {
			tags.add("清淡");
		}
		if (dish.getPrice() != null && intent.getPriceMax() != null && dish.getPrice().compareTo(intent.getPriceMax()) <= 0) {
			tags.add("预算内");
		}
		item.setTags(tags);

		String reason = "匹配你的口味偏好";
		if (intent.getCategory() != null && dish.getDishName() != null && dish.getDishName().contains(intent.getCategory())) {
			reason = "命中你指定的品类";
		}
		if (!historyPreference.isEmpty() && dish.getDishName() != null
				&& historyPreference.keySet().stream().anyMatch(dish.getDishName()::contains)) {
			reason = reason + "，并结合你的历史点单偏好";
		}
		item.setReason(reason);
		return item;
	}

	/**
	 * 加载当前登录用户历史订单关键词权重。
	 * <p>
	 * 依赖订单模块接口，提取历史菜名分词频次，用于个性化加分。
	 */
	private Map<String, Integer> loadUserHistoryKeywordWeight() {
		Map<String, Integer> counter = new HashMap<>();
		PigUser user = SecurityUtils.getUser();
		if (user == null || user.getId() == null) {
			return counter;
		}
		try {
			R<Page<OrderDTO>> response = orderApi.page(1, 10, user.getId(), null, null, null);
			if (response == null || response.getData() == null || response.getData().getRecords() == null) {
				return counter;
			}
			for (OrderDTO order : response.getData().getRecords()) {
				if (order.getOrderItems() == null) {
					continue;
				}
				for (WmOrderItem item : order.getOrderItems()) {
					if (item.getDishName() == null || item.getDishName().isBlank()) {
						continue;
					}
					for (String token : splitTokens(item.getDishName())) {
						counter.merge(token, 1, Integer::sum);
					}
				}
			}
		}
		catch (Exception e) {
			log.debug("加载历史订单偏好失败: {}", e.getMessage());
		}
		return counter;
	}

	private List<String> splitTokens(String value) {
		List<String> tokens = new ArrayList<>();
		if (value == null) {
			return tokens;
		}
		String v = value.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", " ");
		for (String token : v.split("\\s+")) {
			if (token.length() >= 2) {
				tokens.add(token.toLowerCase(Locale.ROOT));
			}
		}
		return tokens;
	}

	private String normalize(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}

	private boolean isSpicy(String name, String desc) {
		return containsAny(name, "辣", "麻辣", "香辣", "川味") || containsAny(desc, "辣", "麻辣", "香辣", "川味");
	}

	private boolean isLightFood(String name, String desc) {
		return containsAny(name, "粥", "汤", "番茄", "鸡蛋", "轻食", "沙拉")
				|| containsAny(desc, "清淡", "低油", "低脂", "易消化", "温热");
	}

	private boolean containsAny(String source, String... candidates) {
		for (String candidate : candidates) {
			if (source.contains(candidate)) {
				return true;
			}
		}
		return false;
	}

}