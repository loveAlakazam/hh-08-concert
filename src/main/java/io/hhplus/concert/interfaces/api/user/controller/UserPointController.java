package io.hhplus.concert.interfaces.api.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.domain.user.service.UserService;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponseEntity;
import io.hhplus.concert.interfaces.api.user.dto.PointRequest;
import io.hhplus.concert.interfaces.api.user.dto.PointResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("points")
@RequiredArgsConstructor
public class UserPointController implements UserPointApiDocs {
	private final UserService userService;
	// 잔액 충전
	@PatchMapping()
	public ResponseEntity<ApiResponse<PointResponse>> chargePoint(@RequestHeader("token") String token, @RequestBody
		PointRequest request) {
		PointResponse response = PointResponse.of(1L, 50000L);
		return ApiResponseEntity.ok(response);
	}

	// 포인트 잔액 조회
	@GetMapping()
	public ResponseEntity<ApiResponse<PointResponse>> getPoint( @RequestHeader("token") String token){
		PointResponse response = PointResponse.of(1L, 50000L);
		return ApiResponseEntity.ok(response);
	}
}
