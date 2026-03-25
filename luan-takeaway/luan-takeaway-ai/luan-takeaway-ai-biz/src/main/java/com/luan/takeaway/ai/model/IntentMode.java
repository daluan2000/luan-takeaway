package com.luan.takeaway.ai.model;

/**
 * 意图识别标签。
 * <p>
 * 与旧版本不同，这里不再做 TOOL_CALLING/RAG 二选一路由，
 * 而是作为语义标注辅助下游链路。
 * </p>
 * <ul>
 *   <li>{@code TOOL_CALLING}：显式条件居多（品类/预算/辣度等硬约束）</li>
 *   <li>{@code RAG}：语义偏好居多（场景/人群/健康诉求等软约束）</li>
 * </ul>
 * 两种标签可同时存在，实际路由由 {@link QueryUnderstandingService} 统一决策。
 */
public enum IntentMode {

	/**
	 * 偏结构化检索路径（显式条件为主）。
	 */
	TOOL_CALLING,

	/**
	 * 偏知识增强语义路径（语义偏好为主）。
	 */
	RAG

}
