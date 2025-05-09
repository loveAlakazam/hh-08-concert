package io.hhplus.concert.infrastructure.distributedlock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedSpinLock {
	String key();
	int ttlSeconds() default 5; // 락점유시간: 기본 5초
	int waitSeconds() default 5; // 다른스레드가 대기하는 시간: 기본 5초
	int retryMillis() default 100; // 재시도를 하기위한 대기시간: 기본 100ms
}
