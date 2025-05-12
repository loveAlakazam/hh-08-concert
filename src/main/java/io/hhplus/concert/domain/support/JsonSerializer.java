package io.hhplus.concert.domain.support;

import java.util.List;

public interface JsonSerializer {
	<T> String toJson(T object);
	<T> T fromJson(String json, Class<T> type);
	<T> List<T> fromJsonList(String json, Class<T> elementType);
}
