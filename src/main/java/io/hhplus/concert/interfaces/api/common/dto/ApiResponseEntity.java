package io.hhplus.concert.interfaces.api.common.dto;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseEntity {
	public static <T>ResponseEntity<ApiResponse<T>> ok(T data) {
		return ResponseEntity.ok(ApiResponse.success(data));
	}
	public static<T>ResponseEntity<ApiResponse<T>> created(T data) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.created(data));
	}
	public static<T> ResponseEntity<ApiResponse<T>> of(HttpStatus status, String message, T data) {
		return ResponseEntity
			.status(status)
			.body(ApiResponse.of(status.value(), message, data));
	}
}
