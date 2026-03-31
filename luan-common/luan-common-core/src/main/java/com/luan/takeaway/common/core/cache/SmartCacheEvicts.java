package com.luan.takeaway.common.core.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 智能缓存清除注解容器（用于可重复注解）。
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SmartCacheEvicts {

	SmartCacheEvict[] value();

}
