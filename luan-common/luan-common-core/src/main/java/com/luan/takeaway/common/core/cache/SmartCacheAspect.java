package com.luan.takeaway.common.core.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luan.takeaway.common.core.cache.HotKeyType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * SmartCache 注解切面处理类。
 *
 * <p>职责：
 * <ul>
 *   <li>拦截带有 @SmartCache 注解的方法</li>
 *   <li>解析缓存 key 和注解参数</li>
 *   <li>委托 SmartCacheService 执行缓存读写逻辑</li>
 *   <li>实现防穿透、防击穿、防雪崩</li>
 *   <li>通过 TtlCalculator 接口支持热点自适应的动态 TTL</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>切面只负责拦截和组装参数</li>
 *   <li>TTL 计算策略由 TtlCalculator 实现</li>
 *   <li>热点检测由 HotKeyManager 实现</li>
 * </ul>
 */
@Slf4j
@Aspect
@Component
public class SmartCacheAspect {

	private static final String LOCK_SUFFIX = ":lock";

	private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();

	private final SmartCacheService smartCacheService;

	private final ObjectMapper objectMapper;

	@Autowired
	public SmartCacheAspect(SmartCacheService smartCacheService, ObjectMapper objectMapper) {
		this.smartCacheService = smartCacheService;
		this.objectMapper = objectMapper;
	}

	@Pointcut("@annotation(com.luan.takeaway.common.core.cache.SmartCache)")
	public void smartCachePointcut() {
	}

	@Around("smartCachePointcut()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		SmartCache smartCache = signature.getMethod().getAnnotation(SmartCache.class);

		String cacheName = smartCache.name();
		String keyExpression = resolveKeyExpression(smartCache.key(), signature, joinPoint.getArgs());
		String cacheKey = buildCacheKey(cacheName, keyExpression);
		String lockKey = cacheKey + LOCK_SUFFIX;

		// 构建 SmartCacheConfig
		SmartCacheConfig config = SmartCacheConfig.fromAnnotation(smartCache);

		// 设置热点 TTL 计算器参数
		HotKeyType hotKeyType = smartCache.hotKeyType();
		if (hotKeyType != null && hotKeyType != HotKeyType.NONE) {
			String hotKeyIdExpression = smartCache.hotKeyIdExpression();
			Object hotKeyId = resolveKeyExpression(hotKeyIdExpression, signature, joinPoint.getArgs());
			// 将热点参数传递给 TtlCalculator
			// 格式：[0]=hotKeyId, [1]=hotKeyType, [2]=baseTtl, [3]=hotTtl
			config.withHotKeyParams(
				Long.parseLong(hotKeyId.toString()),
				hotKeyType,
				smartCache.baseTtlSeconds(),
				smartCache.hotTtlSeconds()
			);
		}

		// 构建返回类型
		JavaType javaType = objectMapper.getTypeFactory().constructType(signature.getMethod().getGenericReturnType());

		// 执行缓存查询
		return smartCacheService.execute(cacheKey, lockKey, javaType, () -> {
			try {
				return joinPoint.proceed();
			}
			catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}, config);
	}

	/**
	 * 解析 key 表达式。
	 */
	private String resolveKeyExpression(String keyExpression, MethodSignature signature, Object[] args) {
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

	/**
	 * 从所有参数构建 key。
	 */
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

	/**
	 * 构建完整的缓存 key。
	 */
	private String buildCacheKey(String cacheName, String keyExpression) {
		return cacheName + ":" + keyExpression;
	}

}
