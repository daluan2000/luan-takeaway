package com.luan.takeaway.common.core.cache;

import com.luan.takeaway.common.core.cache.HotKeyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 热点 TTL 计算器实现。
 *
 * <p>职责：
 * <ul>
 *   <li>实现 TtlCalculator 接口，提供热点感知的动态 TTL 计算</li>
 *   <li>根据热点检测结果返回不同的缓存过期时间</li>
 *   <li>热点数据享受更长的 TTL，减少回源压力</li>
 * </ul>
 *
 * <p>热点检测策略：
 * <ul>
 *   <li>60秒内访问次数 >= 阈值（默认100次）则标记为热点</li>
 *   <li>热点数据 TTL = 30分钟，普通数据 TTL = 5分钟</li>
 * </ul>
 *
 * <p>该组件由 SmartCacheAspect 自动注入，无需手动配置。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotKeyTtlCalculator implements TtlCalculator {

	private final HotKeyManager hotKeyManager;

	private final StringRedisTemplate stringRedisTemplate;

	/**
	 * 计算缓存 TTL。
	 *
	 * <p>参数格式：
	 * <ul>
	 *   <li>[0] hotKeyId - 热点 ID（Long）</li>
	 *   <li>[1] hotKeyType - 热点类型（HotKeyType）</li>
	 *   <li>[2] baseTtlSeconds - 基础 TTL（Long）</li>
	 *   <li>[3] hotTtlSeconds - 热点 TTL（Long）</li>
	 * </ul>
	 *
	 * @param cacheKey 缓存 key（未使用）
	 * @param businessParams 业务参数
	 * @return TTL 秒数
	 */
	@Override
	public long calculateTtl(String cacheKey, Object... businessParams) {
		if (businessParams == null || businessParams.length < 4) {
			return HotKeyConstants.NORMAL_DATA_TTL_SECONDS;
		}

		Object hotKeyIdObj = businessParams[0];
		Object hotKeyTypeObj = businessParams[1];
		Object baseTtlObj = businessParams[2];
		Object hotTtlObj = businessParams[3];

		if (!(hotKeyIdObj instanceof Long hotKeyId)
				|| !(hotKeyTypeObj instanceof HotKeyType hotKeyType)
				|| hotKeyType == HotKeyType.NONE) {
			return baseTtlObj instanceof Long baseTtl ? baseTtl : HotKeyConstants.NORMAL_DATA_TTL_SECONDS;
		}

		// 记录热点访问
		boolean isHot = switch (hotKeyType) {
			case DISH -> {
				hotKeyManager.recordDishAccess(hotKeyId);
				yield hotKeyManager.isHotDish(hotKeyId);
			}
			case SHOP -> {
				hotKeyManager.recordShopAccess(hotKeyId);
				yield hotKeyManager.isHotShop(hotKeyId);
			}
			default -> false;
		};

		if (isHot) {
			log.debug("热点命中, hotKeyType={}, hotKeyId={}", hotKeyType, hotKeyId);
			return hotTtlObj instanceof Long hotTtl ? hotTtl : HotKeyConstants.HOT_DATA_TTL_SECONDS;
		}

		return baseTtlObj instanceof Long baseTtl ? baseTtl : HotKeyConstants.NORMAL_DATA_TTL_SECONDS;
	}

}
