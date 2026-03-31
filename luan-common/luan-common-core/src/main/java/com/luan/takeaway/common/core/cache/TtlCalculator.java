package com.luan.takeaway.common.core.cache;

/**
 * TTL 计算器接口。
 *
 * <p>用于根据业务特征动态计算缓存 TTL，实现缓存策略的灵活扩展。
 *
 * <p>设计原则：
 * <ul>
 *   <li>通用模块只定义接口，不包含任何业务逻辑</li>
 *   <li>业务模块负责实现具体的 TTL 计算策略</li>
 *   <li>通过依赖注入实现解耦，避免模块间循环依赖</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * // 通用模块定义接口
 * public interface TtlCalculator {
 *     long calculateTtl(String cacheKey, Object... businessParams);
 * }
 *
 * // 业务模块实现热点 TTL
 * public class HotKeyTtlCalculator implements TtlCalculator {
 *     public long calculateTtl(String cacheKey, Object... params) {
 *         // 根据热点检测结果返回不同 TTL
 *     }
 * }
 * }
 * </pre>
 */
public interface TtlCalculator {

	/**
	 * 计算缓存 TTL。
	 *
	 * @param cacheKey 缓存 key
	 * @param businessParams 业务参数（如热点 ID、类型等）
	 * @return TTL 秒数
	 */
	long calculateTtl(String cacheKey, Object... businessParams);

}
