package io.hhplus.concert.infrastructure.distributedlock;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
@Order(0) // 트랜잭션보다 먼저 실행되도록 가장 앞 순서로 한다.
@RequiredArgsConstructor
public class DistributedLockAspect {
	private final RedisTemplate<String, String> redisTemplate;

	@Around("@annotation(distributedLock)")
	public Object around(ProceedingJoinPoint joinPoint, DistributedSimpleLock distributedLock) throws Throwable {
		String key = generateDistributedLockKey(joinPoint, distributedLock);
		String lockValue = UUID.randomUUID().toString();
		Duration timeout = Duration.ofSeconds(distributedLock.ttlSeconds());

		// 락 획득 시도(SETNX + EXPIRE)
		Boolean success = redisTemplate.opsForValue().setIfAbsent(key, lockValue, timeout);
		if(Boolean.FALSE.equals(success)) {
			throw new DistributedLockException("Lock failed for key: "+ key);
		}

		try {
			return joinPoint.proceed(); // 실제서비스 메서드 수행
		} finally {
			unlock(key, lockValue);
		}
	}

	private void unlock(String key, String value) {
		String rawLuaScript = """
		if redis.call("get", KEYS[1]) == ARGV[1] then
			return redis.call("del", KEYS[1])
		else
			return 0
		end
		""";
		redisTemplate.execute(
			new DefaultRedisScript<>(rawLuaScript,  Long.class),
			Collections.singletonList(key),
			value
		);
	}

	private String generateDistributedLockKey(ProceedingJoinPoint joinPoint, DistributedSimpleLock distributedLock) {
		Object[] args = joinPoint.getArgs();
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

		ExpressionParser parser = new SpelExpressionParser();
		EvaluationContext context = new StandardEvaluationContext();

		String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
		for(int i=0; i<args.length; i++) {
			context.setVariable(paramNames[i], args[i]);
		}
		Expression expression = parser.parseExpression(distributedLock.key());
		String evaluatedKey = expression.getValue(context, String.class);
		return "lock:"+evaluatedKey;
	}
}
