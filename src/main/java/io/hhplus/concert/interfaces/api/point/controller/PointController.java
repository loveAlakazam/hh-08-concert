package io.hhplus.concert.interfaces.api.point.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponseEntity;
import io.hhplus.concert.interfaces.api.point.dto.PointRequest;
import io.hhplus.concert.interfaces.api.point.dto.PointResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("points")
@RequiredArgsConstructor
public class PointController implements PointApiDocs {
	// 잔액 충전
	@PatchMapping()
	public ResponseEntity<ApiResponse<PointResponse>> chargePoint(@RequestHeader("token") String token, @RequestBody
		PointRequest request) {
		PointResponse response = PointResponse.builder().id(1L).point(50000L).build();
		return ApiResponseEntity.ok(response);
	}

	// 포인트 잔액 조회
	@GetMapping()
	public ResponseEntity<ApiResponse<PointResponse>> getPoint( @RequestHeader("token") String token){
		PointResponse response = PointResponse.builder().id(1L).point(50000L).build();
		return ApiResponseEntity.ok(response);
	}
}
