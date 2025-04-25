package io.hhplus.concert.interfaces.api.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.domain.user.UserCommand;
import io.hhplus.concert.domain.user.UserInfo;
import io.hhplus.concert.domain.user.UserPointCommand;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.interfaces.api.common.ApiResponse;
import io.hhplus.concert.interfaces.api.common.ApiResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController implements UserPointApiDocs {
	private final UserService userService;
	// 유저생성
	@PostMapping("/account")
	public ResponseEntity<ApiResponse<UserResponse.CreateNewUser>> createNewUser(
		@RequestBody UserRequest.CreateNewUser request
	) {
		UserInfo.CreateNewUser info = userService.createUser(UserCommand.CreateNewUser.from(request.name()));
		return ApiResponseEntity.created(UserResponse.CreateNewUser.from(info));
	}
	// 잔액 충전
	@PatchMapping("/points")
	public ResponseEntity<ApiResponse<PointResponse.ChargePoint>> chargePoint(
		@RequestHeader("token") @Valid String token,
		@RequestBody PointRequest.ChargePoint request
	) {
		UserInfo.ChargePoint result = userService.chargePoint(UserPointCommand.ChargePoint.from(request));
		PointResponse.ChargePoint response = PointResponse.ChargePoint.from(result);
		return ApiResponseEntity.ok(response);
	}

	// 포인트 잔액 조회
	@GetMapping("/points")
	public ResponseEntity<ApiResponse<PointResponse.GetCurrentPoint>> getPoint(
		@RequestHeader("token") @Valid String token
	) {
		// TODO: ACTIVE token의 유효성검사
		// 토큰을 통해서 userId 를 추출.
		long userId = 1L;
		UserInfo.GetCurrentPoint result = userService.getCurrentPoint(UserPointCommand.GetCurrentPoint.of(userId));
		PointResponse.GetCurrentPoint response = PointResponse.GetCurrentPoint.from(result);
		return ApiResponseEntity.ok(response);
	}
}
