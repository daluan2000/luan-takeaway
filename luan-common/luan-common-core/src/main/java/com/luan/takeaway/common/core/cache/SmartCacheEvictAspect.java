package com.luan.takeaway.common.core.cache;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * SmartCacheEvict 注解切面处理类。
 *
 * <p>职责：
 * <ul>
 *   <li>拦截带有 @SmartCacheEvict 或 @SmartCacheEvicts 注解的方法</li>
 *   <li>在方法执行后清除对应的缓存</li>
 *   <li>支持按 key 清除和批量清除</li>
 *   <li>支持多个缓存清除</li>
 * </ul>
 */
@Slf4j
@Aspect
@Component
public class SmartCacheEvictAspect {

	private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();

	private final StringRedisTemplate stringRedisTemplate;

	public SmartCacheEvictAspect(StringRedisTemplate stringRedisTemplate) {
		this.stringRedisTemplate = stringRedisTemplate;
	}

	@Pointcut("@annotation(com.luan.takeaway.common.core.cache.SmartCacheEvict)")
	public void smartCacheEvictPointcut() {
	}

	@Pointcut("@annotation(com.luan.takeaway.common.core.cache.SmartCacheEvicts)")
	public void smartCacheEvictsPointcut() {
	}

	@Around("smartCacheEvictPointcut() || smartCacheEvictsPointcut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();

		// 获取所有 @SmartCacheEvict 注解
		SmartCacheEvict[] evicts = getEvictAnnotations(signature);

		try {
			return joinPoint.proceed();
		}
		finally {
			for (SmartCacheEvict evict : evicts) {
				if (evict.allEntries()) {
					evictAll(evict.name());
				}
				else {
					String key = resolveKey(evict.key(), signature, joinPoint.getArgs());
					evictKey(evict.name(), key);
				}
			}
		}
	}

	private SmartCacheEvict[] getEvictAnnotations(MethodSignature signature) {
		// 先获取容器注解
		SmartCacheEvicts evictsContainer = signature.getMethod().getAnnotation(SmartCacheEvicts.class);
		SmartCacheEvict singleEvict = signature.getMethod().getAnnotation(SmartCacheEvict.class);

		if (evictsContainer != null && evictsContainer.value().length > 0) {
			return evictsContainer.value();
		}
		if (singleEvict != null) {
			return new SmartCacheEvict[] { singleEvict };
		}
		return new SmartCacheEvict[0];
	}

	private void evictAll(String cacheName) {
		log.debug("清除所有缓存: {}", cacheName);
		stringRedisTemplate.delete(stringRedisTemplate.keys(cacheName + ":*"));
	}

	private void evictKey(String cacheName, String keyExpression) {
		String cacheKey = cacheName + ":" + keyExpression;
		log.debug("清除缓存: {}", cacheKey);
		stringRedisTemplate.delete(cacheKey);
	}

	private String resolveKey(String keyExpression, MethodSignature signature, Object[] args) {
		if (!StringUtils.hasText(keyExpression)) {
			return buildKeyFromArgs(args);
		}

		Expression expression = SPEL_PARSER.parseExpression(keyExpression);
		EvaluationContext context = new StandardEvaluationContext();
		String[] paramNames = signature.getParameterNames();
		if (paramNames != null) {
			for (int i = 0; i < paramNames.length; i++) {
				((StandardEvaluationContext) context).setVariable(paramNames[i], args[i]);
			}
		}
		for (int i = 0; i < args.length; i++) {
			((StandardEvaluationContext) context).setVariable("p" + i, args[i]);
		}

		Object result = expression.getValue(context);
		return result == null ? "" : result.toString();
	}

	private String buildKeyFromArgs(Object[] args) {
		if (args == null || args.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				sb.append(":");
			}
			sb.append(args[i] == null ? "_" : args[i].toString());
		}
		return sb.toString();
	}

}
