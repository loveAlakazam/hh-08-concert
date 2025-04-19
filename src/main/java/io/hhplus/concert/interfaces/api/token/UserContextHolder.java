package io.hhplus.concert.interfaces.api.token;

public class UserContextHolder {
	private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();
	public static void set(UserContext userContext) {
		CONTEXT.set(userContext);
	}
	public static UserContext get() {
		return CONTEXT.get();
	}
	public static void clear() {
		CONTEXT.remove();
	}
}
