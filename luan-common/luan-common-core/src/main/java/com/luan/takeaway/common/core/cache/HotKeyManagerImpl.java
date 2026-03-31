package com.luan.takeaway.common.core.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 热点数据管理器实现。
 *
 * <p>基于 Redis 时间窗口计数实现热点识别：
 * <ul>
 *   <li>每次访问时对计数器 Key 执行 INCR 并设置 TTL</li>
 *   <li>计数器超过阈值时，将 ID 加入热点集合</li>
 *   <li>查询时直接检查热点集合判断是否为热点</li>
 * </ul>
 *
 * <p>Redis Key 设计：
 * <ul>
 *   <li>计数器：hot:counter:{type}:{id} （TTL=60秒）</li>
 *   <li>热点集合：hot:set:{type} （Set 结构）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotKeyManagerImpl implements HotKeyManager {

	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public boolean recordDishAccess(Long dishId) {
		if (dishId == null) {
			return false;
		}
		return recordAccess(dishId, HotKeyConstants.TYPE_DISH);
	}

	@Override
	public boolean isHotDish(Long dishId) {
		if (dishId == null) {
			return false;
		}
		return isHot(dishId, HotKeyConstants.TYPE_DISH);
	}

	@Override
	public boolean recordShopAccess(Long shopId) {
		if (shopId == null) {
			return false;
		}
		return recordAccess(shopId, HotKeyConstants.TYPE_SHOP);
	}

	@Override
	public boolean isHotShop(Long shopId) {
		if (shopId == null) {
			return false;
		}
		return isHot(shopId, HotKeyConstants.TYPE_SHOP);
	}

	@Override
	public long getDishTtlSeconds(Long dishId) {
		return isHotDish(dishId) ? HotKeyConstants.HOT_DATA_TTL_SECONDS : HotKeyConstants.NORMAL_DATA_TTL_SECONDS;
	}

	@Override
	public long getShopTtlSeconds(Long shopId) {
		return isHotShop(shopId) ? HotKeyConstants.HOT_DATA_TTL_SECONDS : HotKeyConstants.NORMAL_DATA_TTL_SECONDS;
	}

	@Override
	public void removeDishHotFlag(Long dishId) {
		if (dishId == null) {
			return;
		}
		removeHotFlag(dishId, HotKeyConstants.TYPE_DISH);
	}

	@Override
	public void removeShopHotFlag(Long shopId) {
		if (shopId == null) {
			return;
		}
		removeHotFlag(shopId, HotKeyConstants.TYPE_SHOP);
	}

	/**
	 * 记录访问并判断是否升级为热点。
	 *
	 * @param id 数据ID
	 * @param type 数据类型
	 * @return true 表示已升级为热点
	 */
	private boolean recordAccess(Long id, String type) {
		String counterKey = buildCounterKey(type, id);

		try {
			Long count = stringRedisTemplate.opsForValue().increment(counterKey);

			if (count != null && count == 1L) {
				stringRedisTemplate.expire(counterKey, HotKeyConstants.HOT_COUNTER_TTL_SECONDS, TimeUnit.SECONDS);
			}

			if (count != null && count >= HotKeyConstants.HOT_THRESHOLD) {
				return markAsHot(id, type);
			}

			return false;
		}
		catch (Exception e) {
			log.warn("记录热点访问失败, type={}, id={}", type, id, e);
			return false;
		}
	}

	/**
	 * 判断是否为热点。
	 *
	 * @param id 数据ID
	 * @param type 数据类型
	 * @return true 表示是热点
	 */
	private boolean isHot(Long id, String type) {
		String setKey = buildSetKey(type);

		try {
			Boolean isMember = stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(id));
			return Boolean.TRUE.equals(isMember);
		}
		catch (Exception e) {
			log.warn("查询热点状态失败, type={}, id={}", type, id, e);
			return false;
		}
	}

	/**
	 * 标记为热点。
	 *
	 * @param id 数据ID
	 * @param type 数据类型
	 * @return true 表示成功标记
	 */
	private boolean markAsHot(Long id, String type) {
		String setKey = buildSetKey(type);

		try {
			Long result = stringRedisTemplate.opsForSet().add(setKey, String.valueOf(id));
			if (result != null && result > 0) {
				stringRedisTemplate.expire(setKey, HotKeyConstants.HOT_MEMBER_TTL_SECONDS, TimeUnit.SECONDS);
				log.info("数据升级为热点, type={}, id={}", type, id);
				return true;
			}
			return false;
		}
		catch (Exception e) {
			log.warn("标记热点失败, type={}, id={}", type, id, e);
			return false;
		}
	}

	/**
	 * 移除热点标记。
	 *
	 * @param id 数据ID
	 * @param type 数据类型
	 */
	private void removeHotFlag(Long id, String type) {
		String setKey = buildSetKey(type);

		try {
			stringRedisTemplate.opsForSet().remove(setKey, String.valueOf(id));
			log.info("移除热点标记, type={}, id={}", type, id);
		}
		catch (Exception e) {
			log.warn("移除热点标记失败, type={}, id={}", type, id, e);
		}
	}

	/**
	 * 构建计数器 Key。
	 * 格式：hot:counter:{type}:{id}
	 */
	private String buildCounterKey(String type, Long id) {
		return HotKeyConstants.HOT_COUNTER_PREFIX + type + ":" + id;
	}

	/**
	 * 构建热点集合 Key。
	 * 格式：hot:set:{type}
	 */
	private String buildSetKey(String type) {
		return HotKeyConstants.HOT_SET_PREFIX + type;
	}

}
