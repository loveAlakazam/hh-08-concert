package io.hhplus.concert.domain.support;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import io.hhplus.concert.interfaces.api.common.DistributedLockException;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
public class DistributedSimpleLockAspect {
	private final RedissonClient redissonClient;
	private static final Logger log = LoggerFactory.getLogger(DistributedSimpleLockAspect.class);

	@Around("@annotation(distributedLock)")
	public Object around(ProceedingJoinPoint joinPoint, DistributedSimpleLock distributedLock) throws Throwable{
		String key = resolveKey(joinPoint, distributedLock);
		RLock lock = redissonClient.getLock(key);
		boolean isLocked = false;
		try {
			// 락 시도 (waitTime 0초, leaseTime = ttl)
			isLocked =  lock.tryLock(0, distributedLock.ttlSeconds(), TimeUnit.SECONDS);
			if(!isLocked) {
				log.warn("분산락(심플락) 획득 실패 - key: {}", key);
				throw new DistributedLockException("Redisson Lock failed for key: " + key);
			}

			return joinPoint.proceed();
		} finally {
			if(isLocked && lock.isHeldByCurrentThread()) lock.unlock();
		}
	}

	private String resolveKey(ProceedingJoinPoint joinPoint, DistributedSimpleLock lock) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		EvaluationContext context = new StandardEvaluationContext();
		String[] paramNames = signature.getParameterNames();
		Object[] args = joinPoint.getArgs();

		for (int i = 0; i < args.length; i++) {
			context.setVariable(paramNames[i], args[i]);
		}

		ExpressionParser parser = new SpelExpressionParser();
		Expression expression = parser.parseExpression(lock.key());
		return lock.prefix() + ":" + expression.getValue(context, String.class);
	}
}
