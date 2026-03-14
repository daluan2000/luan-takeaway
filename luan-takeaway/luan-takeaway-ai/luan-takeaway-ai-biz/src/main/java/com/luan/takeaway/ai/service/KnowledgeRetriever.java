package com.luan.takeaway.ai.service;

import com.luan.takeaway.ai.model.KnowledgeSnippet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class KnowledgeRetriever {

	private static final List<String> RAW_KNOWLEDGE = List.of(
			"胃不舒服时建议优先选择温热、清淡、低刺激食物，避免辛辣、油炸和冰冷食物。",
			"上火时建议减少辛辣和油腻摄入，可选择粥、汤、番茄鸡蛋类、青菜类菜品。",
			"晚上不想吃太油时，可以优先选择轻食、汤面、粥类，控制高油高盐菜品。",
			"两人用餐时建议搭配主食+汤类+低刺激热菜，兼顾饱腹感与消化负担。",
			"预算有限时优先筛选价格区间，再在区间内做口味和营养平衡。");

	public List<KnowledgeSnippet> retrieve(String query, int limit) {
		List<KnowledgeSnippet> scored = new ArrayList<>();
		for (String doc : RAW_KNOWLEDGE) {
			double score = score(query, doc);
			if (score > 0) {
				scored.add(KnowledgeSnippet.builder().title("饮食建议").content(doc).score(score).build());
			}
		}
		scored.sort(Comparator.comparing(KnowledgeSnippet::getScore).reversed());
		if (scored.size() > limit) {
			return new ArrayList<>(scored.subList(0, limit));
		}
		return scored;
	}

	private double score(String query, String doc) {
		double score = 0;
		for (String keyword : List.of("胃", "上火", "清淡", "低油", "辣", "粥", "汤", "预算", "两人", "减脂", "油腻")) {
			if (query.contains(keyword) && doc.contains(keyword)) {
				score += 1;
			}
		}
		return score;
	}

}