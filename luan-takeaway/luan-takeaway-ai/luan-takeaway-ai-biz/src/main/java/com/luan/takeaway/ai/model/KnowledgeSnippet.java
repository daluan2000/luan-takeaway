package com.luan.takeaway.ai.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class KnowledgeSnippet {

	String title;

	String content;

	double score;

}