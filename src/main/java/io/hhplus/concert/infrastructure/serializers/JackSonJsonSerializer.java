package io.hhplus.concert.infrastructure.serializers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.concert.domain.snapshot.JsonSerializer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JackSonJsonSerializer implements JsonSerializer {
	private final ObjectMapper objectMapper;

	@Override
	public <T> String toJson(T object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("직렬화 실패", e);
		}
	}

	@Override
	public <T> T fromJson(String json, Class<T> type) {
		try {
			return objectMapper.readValue(json, type);
		} catch(JsonProcessingException e) {
			throw new RuntimeException("역직렬화 실패", e);
		}
	}

	@Override
	public <T> List<T> fromJsonList(String json, Class<T> elementType) {
		try {
			JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
			return objectMapper.readValue(json, listType);
		} catch(JsonProcessingException e) {
			throw new RuntimeException("리스트 역직렬화 실패", e);
		}
	}
}
