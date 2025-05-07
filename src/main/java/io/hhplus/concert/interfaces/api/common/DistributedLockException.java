package io.hhplus.concert.interfaces.api.common;

public class DistributedLockException extends RuntimeException {
	private String message;
	public DistributedLockException(String message) {
		super(message);
	}

}
