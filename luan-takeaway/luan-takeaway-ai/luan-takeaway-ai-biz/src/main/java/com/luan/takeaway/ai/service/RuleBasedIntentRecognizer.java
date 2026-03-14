package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.model.IntentMode;
import com.luan.takeaway.ai.model.IntentResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuleBasedIntentRecognizer {

	private static final Pattern PRICE_PATTERN = Pattern.compile("([0-9]{1,4})(?:\\s*)(?:元|块)(?:以内|以下|之内)?");

	private static final Pattern PEOPLE_PATTERN = Pattern.compile("([0-9一二三四五六七八九十两]+)(?:\\s*)人");

	private static final Map<String, Integer> CN_NUM = Map.ofEntries(Map.entry("一", 1), Map.entry("二", 2),
			Map.entry("三", 3), Map.entry("四", 4), Map.entry("五", 5), Map.entry("六", 6), Map.entry("七", 7),
			Map.entry("八", 8), Map.entry("九", 9), Map.entry("十", 10), Map.entry("两", 2));

	private static final List<String> CATEGORY_CANDIDATES = List.of("麻辣烫", "火锅", "粥", "面", "米饭", "盖饭", "沙拉", "鸡胸", "轻食", "汤");

	private static final List<String> FUZZY_HINTS = List.of("胃不舒服", "上火", "太油", "油腻", "清淡", "减脂", "低热量", "不想吃辣", "养胃", "控糖");

	public IntentResult recognize(String query) {
		IntentResult result = new IntentResult();
		result.setOriginalQuery(query);

		Matcher priceMatcher = PRICE_PATTERN.matcher(query);
		if (priceMatcher.find()) {
			result.setPriceMax(new BigDecimal(priceMatcher.group(1)));
		}

		Matcher peopleMatcher = PEOPLE_PATTERN.matcher(query);
		if (peopleMatcher.find()) {
			result.setPeople(parsePeople(peopleMatcher.group(1)));
		}

		for (String category : CATEGORY_CANDIDATES) {
			if (query.contains(category)) {
				result.setCategory(category);
				break;
			}
		}

		if (query.contains("不要辣") || query.contains("不辣") || query.contains("清淡") || query.contains("胃不舒服")) {
			result.setSpicy(false);
		}
		else if (query.contains("麻辣") || query.contains("辣")) {
			result.setSpicy(true);
		}

		if (query.contains("清淡") || query.contains("低热量") || query.contains("减脂") || query.contains("少油")
				|| query.contains("养胃")) {
			result.setPreferLight(true);
		}

		boolean fuzzy = FUZZY_HINTS.stream().anyMatch(query::contains);
		if (fuzzy && result.getPriceMax() == null && result.getCategory() == null) {
			result.setMode(IntentMode.RAG);
		}

		Set<String> keywords = new LinkedHashSet<>();
		if (result.getCategory() != null) {
			keywords.add(result.getCategory());
		}
		if (Boolean.TRUE.equals(result.getPreferLight())) {
			keywords.add("清淡");
		}
		if (Boolean.FALSE.equals(result.getSpicy())) {
			keywords.add("不辣");
		}
		keywords.addAll(extractKnownKeywords(query));
		result.setKeywords(new ArrayList<>(keywords));
		return result;
	}

	private Integer parsePeople(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		if (raw.matches("[0-9]+")) {
			return Integer.parseInt(raw);
		}
		Integer direct = CN_NUM.get(raw);
		if (direct != null) {
			return direct;
		}
		if (raw.length() == 2 && raw.startsWith("十")) {
			Integer tail = CN_NUM.get(raw.substring(1));
			return tail == null ? 10 : 10 + tail;
		}
		return null;
	}

	private List<String> extractKnownKeywords(String query) {
		List<String> result = new ArrayList<>();
		for (String token : List.of("清淡", "低热量", "减脂", "养胃", "汤", "粥", "面", "米饭", "鸡蛋", "番茄")) {
			if (query.contains(token)) {
				result.add(token);
			}
		}
		return result;
	}

}