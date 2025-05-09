package io.hhplus.concert.infrastructure.distributedlocks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedSpinLock {
	String key();
	int ttlSeconds() default 5;
	int waitSeconds() default 5;
	int retryMillis() default 100;
	String prefix() default "lock";
}
