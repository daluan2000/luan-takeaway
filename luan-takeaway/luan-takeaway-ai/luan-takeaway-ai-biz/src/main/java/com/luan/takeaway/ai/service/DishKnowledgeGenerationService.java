package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.config.AiAssistantProperties;
import com.luan.takeaway.takeaway.common.constant.DishBusinessDict;
import com.luan.takeaway.takeaway.common.constant.DishSemanticDict;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeGenerateEvent;
import com.luan.takeaway.takeaway.common.dto.DishKnowledgeDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 菜品知识文档生成服务。
 * <p>
 * 交互说明：
 * 1) 输入来自菜品模块事件（DishKnowledgeGenerateEvent）；
 * 2) 优先尝试调用 LLM 生成结构化文档；
 * 3) 失败时回退到启发式规则；
 * 4) 最后按字典做合法化清洗，确保下游检索一致性。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DishKnowledgeGenerationService {

	private final AiAssistantProperties properties;

	private final OpenAiIntentRecognizer openAiIntentRecognizer;

	/**
	 * 生成单个菜品知识文档。
	 */
	public DishKnowledgeDoc generate(DishKnowledgeGenerateEvent event) {
		DishKnowledgeDoc doc;
		if (properties.getLlm().isEnabled()) {
			try {
				doc = openAiIntentRecognizer.generateDishKnowledgeDoc(event);
			}
			catch (Exception ex) {
				log.warn("LLM 生成菜品知识失败，降级启发式规则: {}", ex.getMessage());
				doc = heuristicDoc(event);
			}
		}
		else {
			doc = heuristicDoc(event);
		}

		doc.setDishId(event.getDishId());
		sanitizeByDict(doc);
		if (!StringUtils.hasText(doc.getEmbeddingText())) {
			doc.setEmbeddingText(buildEmbeddingText(doc));
		}
		return doc;
	}

	/**
	 * 启发式兜底方案。
	 * <p>
	 * 当 LLM 不可用或调用失败时，基于菜品名称/描述关键词生成一个可用文档。
	 */
	private DishKnowledgeDoc heuristicDoc(DishKnowledgeGenerateEvent event) {
		DishKnowledgeDoc doc = new DishKnowledgeDoc();
		doc.setDishId(event.getDishId());
		String name = lower(event.getDishName());
		String desc = lower(event.getDishDesc());

		if (containsAny(name, desc, "粥", "汤")) {
			doc.setCategory("porridge");
			doc.setSoupBased(true);
			doc.setLightTaste(true);
			doc.setTags(List.of("清淡", "易消化"));
			doc.setSuitableScenes(List.of("胃不舒服", "工作午餐"));
		}
		else if (containsAny(name, desc, "面", "粉")) {
			doc.setCategory("noodle");
			doc.setTags(List.of("饱腹感强"));
			doc.setSuitableScenes(List.of("快速解决一餐"));
		}
		else if (containsAny(name, desc, "饭", "盖浇", "炒饭")) {
			doc.setCategory("rice");
			doc.setTags(List.of("高碳水"));
			doc.setSuitableScenes(List.of("补充能量"));
		}
		else if (containsAny(name, desc, "奶茶", "可乐", "咖啡")) {
			doc.setCategory("drink");
			doc.setTags(List.of("高热量"));
			doc.setSuitableScenes(List.of("夜宵"));
		}
		else {
			doc.setCategory("snack");
			doc.setTags(List.of("重口味"));
			doc.setSuitableScenes(List.of("快速解决一餐"));
		}

		boolean spicy = containsAny(name, desc, "辣", "麻辣", "川味");
		doc.setSpicy(spicy);
		doc.setSpicyLevel(spicy ? 3 : 0);
		doc.setLightTaste(doc.getLightTaste() != null ? doc.getLightTaste() : !spicy);
		doc.setOily(containsAny(name, desc, "炸", "肥", "红烧"));
		doc.setVegetarian(containsAny(name, desc, "素", "青菜", "菌"));
		doc.setMealTime(List.of("lunch", "dinner"));
		doc.setPortionSize("medium");
		doc.setSuitablePeople(List.of("学生", "办公室"));
		doc.setAvoidScenes(spicy ? List.of("胃不舒服") : List.of());
		doc.setCalories(containsAny(name, desc, "炸", "奶茶") ? 620 : 420);
		doc.setProtein(containsAny(name, desc, "牛", "鸡", "蛋") ? 26 : 12);
		doc.setFat(containsAny(name, desc, "炸", "肥") ? 28 : 14);
		doc.setCarbohydrate(containsAny(name, desc, "饭", "面", "粉") ? 58 : 24);
		doc.setFlavorDescription(spicy ? "香辣浓郁" : "口感温和");
		doc.setLlmSummary(event.getDishName() + " 适合日常正餐，结合当前门店在售数据生成知识摘要。");
		doc.setRecommendationReason("综合菜品口味、场景和营养特征推荐。");
		return doc;
	}

	/**
	 * 按统一字典清洗文档字段，防止出现不可识别值污染索引。
	 */
	private void sanitizeByDict(DishKnowledgeDoc doc) {
		doc.setCategory(pickOrDefault(doc.getCategory(), DishBusinessDict.CATEGORY, "snack"));
		doc.setSpicyLevel(DishBusinessDict.SPICY_LEVEL.contains(doc.getSpicyLevel()) ? doc.getSpicyLevel() : 0);
		doc.setMealTime(filterFromDict(doc.getMealTime(), DishBusinessDict.MEAL_TIME));
		if (doc.getMealTime().isEmpty()) {
			doc.setMealTime(List.of("lunch"));
		}
		doc.setPortionSize(pickOrDefault(doc.getPortionSize(), DishBusinessDict.PORTION_SIZE, "medium"));
		doc.setTags(filterFromDict(doc.getTags(), DishSemanticDict.TAGS));
		doc.setSuitableScenes(filterFromDict(doc.getSuitableScenes(), DishSemanticDict.SUITABLE_SCENES));
		doc.setAvoidScenes(filterFromDict(doc.getAvoidScenes(), DishSemanticDict.AVOID_SCENES));
		doc.setSuitablePeople(filterFromDict(doc.getSuitablePeople(), DishSemanticDict.SUITABLE_PEOPLE));
	}

	/**
	 * 从候选值中过滤出字典允许项。
	 */
	private List<String> filterFromDict(List<String> values, List<String> dict) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		List<String> result = new ArrayList<>();
		for (String value : values) {
			if (StringUtils.hasText(value) && dict.contains(value.trim())) {
				result.add(value.trim());
			}
		}
		return result;
	}

	/**
	 * 字段合法时返回原值，否则回退默认值。
	 */
	private String pickOrDefault(String value, List<String> dict, String defaultValue) {
		if (StringUtils.hasText(value) && dict.contains(value.trim())) {
			return value.trim();
		}
		return defaultValue;
	}

	/**
	 * 组装 embedding 文本，供语义检索或向量化使用。
	 */
	private String buildEmbeddingText(DishKnowledgeDoc doc) {
		StringBuilder builder = new StringBuilder();
		if (StringUtils.hasText(doc.getCategory())) {
			builder.append(doc.getCategory()).append(' ');
		}
		appendList(builder, doc.getTags());
		appendList(builder, doc.getSuitableScenes());
		if (StringUtils.hasText(doc.getLlmSummary())) {
			builder.append(doc.getLlmSummary());
		}
		return builder.toString().trim();
	}

	private void appendList(StringBuilder builder, List<String> values) {
		if (values == null) {
			return;
		}
		for (String value : values) {
			if (StringUtils.hasText(value)) {
				builder.append(value.trim()).append(' ');
			}
		}
	}

	private boolean containsAny(String name, String desc, String... tokens) {
		for (String token : tokens) {
			if (name.contains(token) || desc.contains(token)) {
				return true;
			}
		}
		return false;
	}

	private String lower(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}

}
