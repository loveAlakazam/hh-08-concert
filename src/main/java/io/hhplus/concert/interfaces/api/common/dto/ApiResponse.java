package io.hhplus.concert.interfaces.api.common.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
	private final int status;
	private final String message;
	private final T data;

	private ApiResponse(int status, String message, T data) {
		this.status = status;
		this.message = message;
		this.data = data;
	}

	public static<T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(HttpStatus.OK.value(), "OK", data);
	}

	public static<T> ApiResponse<T> created(T data) {
		return new ApiResponse<>(HttpStatus.CREATED.value(), "CREATED", data);
	}

	public static<T> ApiResponse<T> of(int status, String message, T data) {
		return new ApiResponse<>(status, message, data);
	}
}
