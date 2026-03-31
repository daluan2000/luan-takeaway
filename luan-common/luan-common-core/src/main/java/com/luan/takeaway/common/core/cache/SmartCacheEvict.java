package com.luan.takeaway.common.core.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 智能缓存清除注解。
 *
 * <p>配合 @SmartCache 使用，用于在数据变更时清除缓存。
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * @SmartCacheEvict(name = "menu", key = "#roleId")
 * public void updateRoleMenu(Long roleId, List<Long> menuIds) {
 *     // ...
 * }
 *
 * @SmartCacheEvict(name = "dish", allEntries = true)
 * public void clearAllDishCache() {
 *     // ...
 * }
 *
 * // 多个缓存清除
 * @SmartCacheEvicts({
 *     @SmartCacheEvict(name = "dish:item", key = "#entity.id"),
 *     @SmartCacheEvict(name = "dish:list", allEntries = true)
 * })
 * public void updateDish(WmDish entity) {
 *     // ...
 * }
 * }
 * </pre>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(SmartCacheEvicts.class)
public @interface SmartCacheEvict {

	/**
	 * 缓存名称，对应 Redis 的 key 前缀。
	 */
	String name();

	/**
	 * 缓存 key，支持 SpEL 表达式。
	 * 如果为空且 allEntries 为 false，则使用所有参数。
	 */
	String key() default "";

	/**
	 * 是否清除所有该名称下的缓存。
	 * 如果为 true，则忽略 key 参数。
	 */
	boolean allEntries() default false;

}
