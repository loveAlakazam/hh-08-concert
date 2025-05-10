package io.hhplus.concert.infrastructure.distributedlock;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import io.hhplus.concert.interfaces.api.common.DistributedLockException;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
public class DistributedSpinLockAspect {
	private final SpinLockManager lockManager;

	@Around("@annotation(lock)")
	public Object around(ProceedingJoinPoint joinPoint, DistributedSpinLock lock) throws Throwable {
		String key = generateKey(joinPoint, lock);
		String value = UUID.randomUUID().toString();

		Duration ttl = Duration.ofSeconds(lock.ttlSeconds());
		Duration wait = Duration.ofSeconds(lock.waitSeconds());
		Duration retry = Duration.ofMillis(lock.retryMillis());

		boolean acquired = lockManager.tryLock(key, value, ttl, wait, retry);
		if(!acquired) {
			throw new DistributedLockException("SpinLock 획득 실패: " + key);
		}

		try {
			return joinPoint.proceed();
		} finally {
			lockManager.unlock(key, value);
		}
	}

	private String generateKey(ProceedingJoinPoint joinPoint, DistributedSpinLock lock) {
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		EvaluationContext context = new StandardEvaluationContext();
		String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
		Object[] args = joinPoint.getArgs();

		for(int i=0; i<args.length; i++)
			context.setVariable(paramNames[i], args[i]);

		Expression expression = new SpelExpressionParser().parseExpression(lock.key());
		return "lock:" + expression.getValue(context, String.class);
	}
}
