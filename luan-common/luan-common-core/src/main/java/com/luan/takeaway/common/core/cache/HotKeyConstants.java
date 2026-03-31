package com.luan.takeaway.common.core.cache;

/**
 * 热点缓存常量定义。
 */
public interface HotKeyConstants {

	/**
	 * 热点计数器 Key 前缀。
	 * 格式：hot:counter:{type}:{id}
	 * 例如：hot:counter:dish:1001
	 */
	String HOT_COUNTER_PREFIX = "hot:counter:";

	/**
	 * 热点集合 Key 前缀。
	 * 格式：hot:set:{type}
	 * 例如：hot:set:dish
	 */
	String HOT_SET_PREFIX = "hot:set:";

	/**
	 * 热点计数器 TTL（秒）。
	 * 60秒内计数达到阈值则判定为热点。
	 */
	long HOT_COUNTER_TTL_SECONDS = 60;

	/**
	 * 热点判定阈值。
	 * 60秒内访问次数达到此值则判定为热点。
	 */
	int HOT_THRESHOLD = 100;

	/**
	 * 热点数据缓存 TTL（秒）。
	 * 热点数据享受更长的缓存时间。
	 */
	long HOT_DATA_TTL_SECONDS = 30 * 60;

	/**
	 * 普通数据缓存 TTL（秒）。
	 */
	long NORMAL_DATA_TTL_SECONDS = 5 * 60;

	/**
	 * 热点集合成员 TTL（秒）。
	 * 热点标记过期后需要重新计数判断。
	 */
	long HOT_MEMBER_TTL_SECONDS = 30 * 60;

	/**
	 * 热点类型：菜品
	 */
	String TYPE_DISH = "dish";

	/**
	 * 热点类型：商家
	 */
	String TYPE_SHOP = "shop";

}
