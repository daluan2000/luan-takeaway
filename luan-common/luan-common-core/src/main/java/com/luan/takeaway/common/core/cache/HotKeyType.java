package com.luan.takeaway.common.core.cache;

/**
 * 热点 Key 类型枚举。
 *
 * <p>用于指定数据的热点类型，以便实现动态 TTL 自适应。
 *
 * <p>使用方式：
 * <ul>
 *   <li>DISH - 菜品数据，会使用 HotKeyManager 记录访问并判断热点</li>
 *   <li>SHOP - 商家数据，会使用 HotKeyManager 记录访问并判断热点</li>
 *   <li>NONE - 不进行热点检测，使用固定 TTL</li>
 * </ul>
 */
public enum HotKeyType {

	/**
	 * 菜品类型热点检测。
	 */
	DISH,

	/**
	 * 商家类型热点检测。
	 */
	SHOP,

	/**
	 * 不进行热点检测，使用固定 TTL。
	 */
	NONE

}
