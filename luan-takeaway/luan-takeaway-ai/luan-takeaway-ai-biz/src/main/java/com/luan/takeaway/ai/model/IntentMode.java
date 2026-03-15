package com.luan.takeaway.ai.model;

/**
 * 意图路由模式。
 */
public enum IntentMode {

	/** 偏结构化检索路径。 */
	TOOL_CALLING,

	/** 偏知识增强语义路径。 */
	RAG

}