package com.luan.takeaway.common.core.cache;

import com.luan.takeaway.common.core.cache.HotKeyType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自适应智能缓存注解。
 *
 * <p>整合了以下缓存保护能力：
 * <ul>
 *   <li>【防穿透】空值缓存，避免 DB 无数据被反复查询</li>
 *   <li>【防击穿】互斥锁，避免热点 key 过期时大量请求打爆 DB</li>
 *   <li>【防雪崩】随机 TTL 抖动，避免大量 key 同时过期</li>
 *   <li>【热点自适应】热点数据自动延长 TTL，减少回源压力</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * {@code
 * // 通用场景（无热点检测）
 * @SmartCache(name = "menu", key = "#roleId")
 * public List<SysMenu> getMenusByRole(Long roleId) {
 *     return menuMapper.selectByRoleId(roleId);
 * }
 *
 * // 热点自适应场景（自动根据访问频率调整 TTL）
 * @SmartCache(
 *     name = "dish:item",
 *     key = "#merchantUserId + ':' + #dishId",
 *     hotKeyType = HotKeyType.DISH,
 *     hotKeyIdExpression = "#dishId",
 *     baseTtlSeconds = 300,
 *     hotTtlSeconds = 1800
 * )
 * public WmDish getDish(Long merchantUserId, Long dishId) {
 *     return dishMapper.selectById(dishId);
 * }
 * }
 * </pre>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SmartCache {

	/**
	 * 缓存名称，对应 Redis 的 key 前缀。
	 */
	String name();

	/**
	 * 缓存 key，支持 SpEL 表达式。
	 * 默认取所有参数拼接。
	 */
	String key() default "";

	/**
	 * 热点类型，用于动态 TTL 计算。
	 * - NONE：不进行热点检测
	 * - DISH：菜品热点检测
	 * - SHOP：商家热点检测
	 */
	HotKeyType hotKeyType() default HotKeyType.NONE;

	/**
	 * 热点 ID 的 SpEL 表达式，用于记录热点访问。
	 * 例如：#dishId 或 #merchantUserId
	 */
	String hotKeyIdExpression() default "";

	/**
	 * 基础 TTL 秒数（普通数据）。
	 * 默认 5 分钟。
	 */
	long baseTtlSeconds() default 300;

	/**
	 * 热点数据 TTL 秒数。
	 * 默认 30 分钟。
	 * 业务模块可通过注入 TtlCalculator 实现使用此 TTL。
	 */
	long hotTtlSeconds() default 1800;

	/**
	 * 空值缓存 TTL 秒数（用于防穿透）。
	 * 默认 2 分钟。
	 */
	long nullTtlSeconds() default 120;

	/**
	 * 互斥锁 TTL 秒数（用于防击穿）。
	 * 默认 10 秒。
	 */
	long lockTtlSeconds() default 10;

	/**
	 * 防击穿重试次数。
	 * 默认 3 次。
	 */
	int retryTimes() default 3;

	/**
	 * 防击穿重试间隔（毫秒）。
	 * 默认 50ms。
	 */
	long retrySleepMillis() default 50;

}
