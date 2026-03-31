package com.luan.takeaway.common.core.cache;

import com.luan.takeaway.common.core.cache.HotKeyType;
import lombok.Getter;

/**
 * 智能缓存配置。
 *
 * <p>用于配置 @SmartCache 注解的行为。
 *
 * <p>设计原则：
 * <ul>
 *   <li>TTL 计算策略通过 TtlCalculator 接口抽象</li>
 *   <li>业务模块实现具体的 TTL 计算逻辑</li>
 *   <li>通用模块不依赖任何业务特定类型</li>
 * </ul>
 */
@Getter
public class SmartCacheConfig {

	/** 基础 TTL 秒数（普通数据）。 */
	private final long baseTtlSeconds;

	/** 热点数据 TTL 秒数。 */
	private final long hotTtlSeconds;

	/** 空值缓存 TTL 秒数（用于防穿透）。 */
	private final long nullTtlSeconds;

	/** 互斥锁 TTL 秒数（用于防击穿）。 */
	private final long lockTtlSeconds;

	/** 防击穿重试次数。 */
	private final int retryTimes;

	/** 防击穿重试间隔（毫秒）。 */
	private final long retrySleepMillis;

	/** 随机抖动 TTL 秒数（用于防雪崩）。 */
	private final long jitterTtlSeconds;

	/** TTL 计算器（业务模块注入）。 */
	private TtlCalculator ttlCalculator;

	/** 热点参数：hotKeyId */
	private Long hotKeyId;

	/** 热点参数：hotKeyType */
	private HotKeyType hotKeyType;

	private SmartCacheConfig() {
		this.baseTtlSeconds = 300;
		this.hotTtlSeconds = 1800;
		this.nullTtlSeconds = 120;
		this.lockTtlSeconds = 10;
		this.retryTimes = 3;
		this.retrySleepMillis = 50;
		this.jitterTtlSeconds = 60;
	}

	private SmartCacheConfig(long baseTtlSeconds, long hotTtlSeconds, long nullTtlSeconds, long lockTtlSeconds,
			int retryTimes, long retrySleepMillis, long jitterTtlSeconds) {
		this.baseTtlSeconds = baseTtlSeconds;
		this.hotTtlSeconds = hotTtlSeconds;
		this.nullTtlSeconds = nullTtlSeconds;
		this.lockTtlSeconds = lockTtlSeconds;
		this.retryTimes = retryTimes;
		this.retrySleepMillis = retrySleepMillis;
		this.jitterTtlSeconds = jitterTtlSeconds;
	}

	/**
	 * 构建配置。
	 */
	public static SmartCacheConfig of(long baseTtlSeconds, long hotTtlSeconds, long nullTtlSeconds, long lockTtlSeconds,
			int retryTimes, long retrySleepMillis, long jitterTtlSeconds) {
		return new SmartCacheConfig(baseTtlSeconds, hotTtlSeconds, nullTtlSeconds, lockTtlSeconds, retryTimes,
				retrySleepMillis, jitterTtlSeconds);
	}

	/**
	 * 从 @SmartCache 注解构建配置。
	 */
	public static SmartCacheConfig fromAnnotation(SmartCache annotation) {
		SmartCacheConfig config = new SmartCacheConfig(annotation.baseTtlSeconds(), annotation.hotTtlSeconds(),
				annotation.nullTtlSeconds(), annotation.lockTtlSeconds(), annotation.retryTimes(),
				annotation.retrySleepMillis(), (long) (annotation.baseTtlSeconds() * 0.2));
		return config;
	}

	/**
	 * 默认配置。
	 */
	public static SmartCacheConfig defaultConfig() {
		return new SmartCacheConfig();
	}

	/**
	 * 计算动态 TTL。
	 *
	 * @param cacheKey 缓存 key
	 * @return 最终使用的 TTL 秒数
	 */
	public long calculateDynamicTtl(String cacheKey) {
		if (ttlCalculator != null) {
			// 传递热点参数给 TtlCalculator
			return ttlCalculator.calculateTtl(cacheKey, hotKeyId, hotKeyType, baseTtlSeconds, hotTtlSeconds);
		}
		return baseTtlSeconds + jitterTtlSeconds;
	}

	/**
	 * 设置 TTL 计算器。
	 */
	public SmartCacheConfig withTtlCalculator(TtlCalculator ttlCalculator) {
		this.ttlCalculator = ttlCalculator;
		return this;
	}

	/**
	 * 设置热点参数。
	 */
	public SmartCacheConfig withHotKeyParams(Long hotKeyId, HotKeyType hotKeyType, long baseTtl, long hotTtl) {
		this.hotKeyId = hotKeyId;
		this.hotKeyType = hotKeyType;
		return this;
	}

}
