package com.luan.takeaway.takeaway.common.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 通用缓存保护组件。
 *
 * 这个类专门干一件事：把缓存常见三件麻烦事统一兜住，业务方只管传 key 和回源函数。
 * - 【防穿透】DB 没有的数据也会短暂缓存（空值标记），避免反复打 DB。
 * - 【防击穿】热点 key 过期时加互斥锁，只让一个线程回源。
 * - 【防雪崩】正常缓存 TTL 加随机抖动，不让大量 key 同时过期。
 */
@Slf4j
@Service
@AllArgsConstructor
public class RedisSafeCacheService {

    /** 空值占位符，表示“这个 key 回源查过了，DB 里确实没有”。 */
    private static final String NULL_MARKER = "__NULL__";

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    public <T> T queryWithProtect(String dataKey, String lockKey, Class<T> valueType, Supplier<T> dbLoader,
            CachePolicy policy) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(valueType);
        return queryWithProtect(dataKey, lockKey, javaType, dbLoader, policy);
    }

    public <T> T queryWithProtect(String dataKey, String lockKey, JavaType javaType, Supplier<T> dbLoader,
            CachePolicy policy) {
        // 第 1 次读缓存：
        // - 命中正常值：直接返回。
        // - 命中空值标记：直接返回 null（【防穿透】生效）。
        String cached = stringRedisTemplate.opsForValue().get(dataKey);
        T parsed = parseFromCache(dataKey, cached, javaType);
        if (parsed != null || NULL_MARKER.equals(cached)) {
            return parsed;
        }

        // 缓存没命中，开始抢锁：
        // 这里就是【防击穿】的核心，热点 key 只允许一个线程回源。
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", policy.getLockTtlSeconds(),
                TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(locked)) {
            try {
                // 拿到锁后再读一次（双检），避免别的线程已经把缓存填好了。
                String secondRead = stringRedisTemplate.opsForValue().get(dataKey);
                T secondParsed = parseFromCache(dataKey, secondRead, javaType);
                if (secondParsed != null || NULL_MARKER.equals(secondRead)) {
                    return secondParsed;
                }

                // 真正回源 DB。
                T dbValue = dbLoader.get();
                writeCache(dataKey, dbValue, policy);
                return dbValue;
            }
            finally {
                // 兜底释放锁。就算这里异常，锁也有 TTL，不会一直卡死。
                stringRedisTemplate.delete(lockKey);
            }
        }

        // 没抢到锁就短暂等一会再读缓存，给“持锁线程”一点时间回填。
        // 这段仍属于【防击穿】的一部分，避免大家一起怼 DB。
        for (int i = 0; i < policy.getRetryTimes(); i++) {
            try {
                Thread.sleep(policy.getRetrySleepMillis());
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

        // 极端情况下重试后还没拿到结果，做一次兜底回源，保证可用性优先。
        T dbValue = dbLoader.get();
        writeCache(dataKey, dbValue, policy);
        return dbValue;
    }

    private <T> T parseFromCache(String dataKey, String cached, JavaType javaType) {
        // 命中空值标记，直接返回 null（【防穿透】）。
        if (NULL_MARKER.equals(cached)) {
            return null;
        }
        if (!StringUtils.hasText(cached)) {
            return null;
        }

        try {
            return objectMapper.readValue(cached, javaType);
        }
        catch (Exception ex) {
            // 缓存脏了就删掉，让下一次请求自动回源重建。
            stringRedisTemplate.delete(dataKey);
            log.warn("缓存反序列化失败，删除脏数据, key={}", dataKey, ex);
            return null;
        }
    }

    private void writeCache(String dataKey, Object dbValue, CachePolicy policy) {
        if (dbValue == null) {
            // DB 不存在的数据写空值缓存（【防穿透】）。
            stringRedisTemplate.opsForValue().set(dataKey, NULL_MARKER, policy.getNullTtlSeconds(), TimeUnit.SECONDS);
            return;
        }

        long ttl = policy.getBaseTtlSeconds();
        if (policy.getRandomTtlSeconds() > 0) {
            // TTL 加随机抖动（【防雪崩】），把过期时间错开。
            ttl += ThreadLocalRandom.current().nextLong(policy.getRandomTtlSeconds() + 1);
        }

        try {
            stringRedisTemplate.opsForValue().set(dataKey, objectMapper.writeValueAsString(dbValue), ttl,
                    TimeUnit.SECONDS);
        }
        catch (Exception ex) {
            log.warn("缓存序列化失败，跳过写缓存, key={}", dataKey, ex);
        }
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class CachePolicy {

        /** 正常缓存基础 TTL（秒）。 */
        private final long baseTtlSeconds;

        /** 正常缓存随机抖动 TTL（秒）。 */
        private final long randomTtlSeconds;

        /** 空值缓存 TTL（秒），用于【防穿透】。 */
        private final long nullTtlSeconds;

        /** 锁 TTL（秒），用于【防击穿】。 */
        private final long lockTtlSeconds;

        /** 未拿到锁时重试次数。 */
        private final int retryTimes;

        /** 每次重试间隔（毫秒）。 */
        private final long retrySleepMillis;
    }
}
