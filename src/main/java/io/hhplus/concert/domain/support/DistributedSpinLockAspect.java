package io.hhplus.concert.domain.support;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
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
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DistributedSpinLockAspect {
	private final RedissonClient redissonClient;

	@Around("annotation(lock)")
	public Object around(ProceedingJoinPoint joinPoint, DistributedSpinLock lock) throws Throwable {
		String key = resolveKey(joinPoint, lock);
		RLock rLock = redissonClient.getLock(key);
		boolean acquired = false;
		long waitDeadLine = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(lock.waitSeconds());
		while(System.currentTimeMillis() <= waitDeadLine ) {
			acquired = rLock.tryLock(0, lock.ttlSeconds(), TimeUnit.SECONDS);
			if(acquired) break;
			Thread.sleep(lock.retryMillis());
		}

		if(!acquired) {
			throw new DistributedLockException("Redisson SpinLock 획득실패: "+ key);
		}

		try {
			return joinPoint.proceed();
		} finally {
			if(rLock.isHeldByCurrentThread()) {
				rLock.unlock();
			}
		}
	}

	private String resolveKey(ProceedingJoinPoint joinPoint, DistributedSpinLock lock) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		EvaluationContext context = new StandardEvaluationContext();
		String[] paramNames = signature.getParameterNames();
		Object[] args = joinPoint.getArgs();

		for(int i=0; i<args.length; i++) {
			context.setVariable(paramNames[i], args[i]);
		}
		ExpressionParser parser = new SpelExpressionParser();
		Expression expression = parser.parseExpression(lock.key());
		return lock.prefix() + ":" + expression.getValue(context, String.class);
	}
}
