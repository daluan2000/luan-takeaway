package com.luan.takeaway.common.core.cache;

/**
 * 热点数据管理器接口。
 *
 * <p>职责：
 * <ul>
 *   <li>访问计数：每次数据访问时调用 recordAccess() 记录</li>
 *   <li>热点判断：调用 isHotXxx() 判断数据是否为热点</li>
 *   <li>动态 TTL：根据热点状态返回不同的缓存过期时间</li>
 * </ul>
 *
 * <p>热点识别策略：基于 Redis 时间窗口计数
 * <ul>
 *   <li>每次访问时 INCR 计数器 Key（TTL=60秒）</li>
 *   <li>60秒内访问次数 >= 阈值（默认100）则标记为热点</li>
 *   <li>热点数据享受更长 TTL（默认30分钟 vs 普通5分钟）</li>
 * </ul>
 */
public interface HotKeyManager {

	/**
	 * 记录菜品访问并判断是否升级为热点。
	 *
	 * @param dishId 菜品ID
	 * @return true 表示该菜品已被判定为热点
	 */
	boolean recordDishAccess(Long dishId);

	/**
	 * 判断菜品是否为热点数据。
	 *
	 * @param dishId 菜品ID
	 * @return true 表示是热点数据
	 */
	boolean isHotDish(Long dishId);

	/**
	 * 记录商家访问并判断是否升级为热点。
	 *
	 * @param shopId 商家ID
	 * @return true 表示该商家已被判定为热点
	 */
	boolean recordShopAccess(Long shopId);

	/**
	 * 判断商家是否为热点数据。
	 *
	 * @param shopId 商家ID
	 * @return true 表示是热点数据
	 */
	boolean isHotShop(Long shopId);

	/**
	 * 获取菜品缓存的动态 TTL。
	 *
	 * @param dishId 菜品ID
	 * @return 缓存过期时间（秒）
	 */
	long getDishTtlSeconds(Long dishId);

	/**
	 * 获取商家缓存的动态 TTL。
	 *
	 * @param shopId 商家ID
	 * @return 缓存过期时间（秒）
	 */
	long getShopTtlSeconds(Long shopId);

	/**
	 * 移除热点标记（手动降级）。
	 *
	 * @param dishId 菜品ID
	 */
	void removeDishHotFlag(Long dishId);

	/**
	 * 移除热点标记（手动降级）。
	 *
	 * @param shopId 商家ID
	 */
	void removeShopHotFlag(Long shopId);

}
