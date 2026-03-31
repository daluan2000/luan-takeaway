package com.luan.takeaway.common.core.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 智能缓存服务组件。
 *
 * <p>职责：
 * <ul>
 *   <li>提供统一的缓存读写接口</li>
 *   <li>【防穿透】空值缓存标记</li>
 *   <li>【防击穿】互斥锁机制</li>
 *   <li>【防雪崩】随机 TTL 抖动</li>
 *   <li>【动态 TTL】通过 TtlCalculator 接口支持业务扩展</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>通用模块不依赖任何业务特定类型</li>
 *   <li>TTL 计算策略通过 TtlCalculator 接口抽象</li>
 *   <li>业务模块注入具体的 TTL 计算实现</li>
 * </ul>
 *
 * <p>该服务是 @SmartCache 注解的共同底层实现。
 */
@Slf4j
@Service
public class SmartCacheService {

	/** 空值占位符。 */
	private static final String NULL_MARKER = "__NULL__";

	private final StringRedisTemplate stringRedisTemplate;

	private final ObjectMapper objectMapper;

	@Autowired
	public SmartCacheService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * 执行缓存查询（带完整保护）。
	 *
	 * @param dataKey 缓存 key
	 * @param lockKey 锁 key
	 * @param javaType 返回类型
	 * @param dbLoader 数据库加载函数
	 * @param config 缓存配置
	 * @param <T> 返回类型泛型
	 * @return 查询结果
	 */
	public <T> T execute(String dataKey, String lockKey, JavaType javaType, Supplier<T> dbLoader, SmartCacheConfig config) {
		if (config == null) {
			config = SmartCacheConfig.defaultConfig();
		}

		// 第 1 次读缓存
		String cached = stringRedisTemplate.opsForValue().get(dataKey);
		T parsed = parseFromCache(dataKey, cached, javaType);
		if (parsed != null || NULL_MARKER.equals(cached)) {
			return parsed;
		}

		// 尝试获取锁（防击穿）
		Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", config.getLockTtlSeconds(),
				TimeUnit.SECONDS);

		if (Boolean.TRUE.equals(locked)) {
			try {
				// 双检：拿到锁后再读一次缓存
				String secondRead = stringRedisTemplate.opsForValue().get(dataKey);
				T secondParsed = parseFromCache(dataKey, secondRead, javaType);
				if (secondParsed != null || NULL_MARKER.equals(secondRead)) {
					return secondParsed;
				}

				// 真正回源 DB
				T dbResult = dbLoader.get();

				// 计算动态 TTL 并写入缓存
				long dynamicTtl = config.calculateDynamicTtl(dataKey);
				writeCache(dataKey, dbResult, dynamicTtl, config.getNullTtlSeconds());

				return dbResult;
			}
			finally {
				// 释放锁
				stringRedisTemplate.delete(lockKey);
			}
		}

		// 没抢到锁，等待重试
		for (int i = 0; i < config.getRetryTimes(); i++) {
			try {
				Thread.sleep(config.getRetrySleepMillis());
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				break;
			}

			String retryRead = stringRedisTemplate.opsForValue().get(dataKey);
			T retryParsed = parseFromCache(dataKey, retryRead, javaType);
			if (retryParsed != null || NULL_MARKER.equals(retryRead)) {
				return retryParsed;
			}
		}

		// 兜底：直接执行
		T dbResult = dbLoader.get();
		long dynamicTtl = config.calculateDynamicTtl(dataKey);
		writeCache(dataKey, dbResult, dynamicTtl, config.getNullTtlSeconds());
		return dbResult;
	}

	/**
	 * 执行缓存查询（使用 Class 类型）。
	 */
	public <T> T execute(String dataKey, String lockKey, Class<T> valueType, Supplier<T> dbLoader, SmartCacheConfig config) {
		JavaType javaType = objectMapper.getTypeFactory().constructType(valueType);
		return execute(dataKey, lockKey, javaType, dbLoader, config);
	}

	private <T> T parseFromCache(String dataKey, String cached, JavaType javaType) {
		if (NULL_MARKER.equals(cached)) {
			return null;
		}
		if (!StringUtils.hasText(cached)) {
			return null;
		}
		if (javaType == null) {
			return null;
		}
		try {
			return objectMapper.readValue(cached, javaType);
		}
		catch (Exception ex) {
			stringRedisTemplate.delete(dataKey);
			log.warn("缓存反序列化失败，删除脏数据, key={}", dataKey, ex);
			return null;
		}
	}

	private void writeCache(String dataKey, Object dbValue, long baseTtlSeconds, long nullTtlSeconds) {
		if (dbValue == null) {
			stringRedisTemplate.opsForValue().set(dataKey, NULL_MARKER, nullTtlSeconds, TimeUnit.SECONDS);
			return;
		}
		try {
			stringRedisTemplate.opsForValue().set(dataKey, objectMapper.writeValueAsString(dbValue), baseTtlSeconds,
					TimeUnit.SECONDS);
		}
		catch (Exception ex) {
			log.warn("缓存序列化失败，跳过写缓存, key={}", dataKey, ex);
		}
	}

	/**
	 * 写入缓存（指定 TTL）。
	 */
	public void put(String dataKey, Object value, long ttlSeconds) {
		writeCache(dataKey, value, ttlSeconds, ttlSeconds);
	}

	/**
	 * 写入空值缓存（防穿透）。
	 */
	public void putNull(String dataKey, long nullTtlSeconds) {
		stringRedisTemplate.opsForValue().set(dataKey, NULL_MARKER, nullTtlSeconds, TimeUnit.SECONDS);
	}

	/**
	 * 删除缓存。
	 */
	public void evict(String dataKey) {
		stringRedisTemplate.delete(dataKey);
	}

	/**
	 * 读取缓存值（不执行回源）。
	 */
	public String get(String dataKey) {
		return stringRedisTemplate.opsForValue().get(dataKey);
	}

	/**
	 * 读取缓存值并反序列化。
	 */
	public <T> T get(String dataKey, Class<T> valueType) {
		String cached = get(dataKey);
		if (NULL_MARKER.equals(cached)) {
			return null;
		}
		if (!StringUtils.hasText(cached)) {
			return null;
		}
		try {
			return objectMapper.readValue(cached, valueType);
		}
		catch (Exception ex) {
			log.warn("缓存反序列化失败, key={}", dataKey, ex);
			return null;
		}
	}

}
