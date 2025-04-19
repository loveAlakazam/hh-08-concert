package io.hhplus.concert.interfaces.api.concert;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import io.hhplus.concert.interfaces.api.common.ApiResponse;
import io.hhplus.concert.interfaces.api.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Concert")
public interface ConcertApiDocs {
	/**
	 *	콘서트 목록 조회
	 *
	 * @param page -페이지
	 * @return
	 */
	ResponseEntity<ApiResponse<ConcertResponse.GetConcerts>>  getConcerts(
		@RequestParam(value = "page", required = false, defaultValue = "1") int page
	);

	/**
	 * 특정 콘서트 공연 일정 목록 조회
	 *
	 * @param id
	 * @param page
	 * @return
	 */
	@Operation(summary = "예약가능한 날짜 조회", description="특정 콘서트 중 예약가능한 콘서트날짜 목록을 반환")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "200",
		description = "OK",
		content= @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				name = "예약 가능한 날짜 목록 조회 성공 응답 예시",
				summary = "예약 가능한 날짜 목록 조회 성공 응답 데이터",
				value= """
					{
					  "status": 200,
					  "message": "OK",
					  "data": [
						"2025-04-04",
						"2025-04-05",
						"2025-04-11"
					  ]
					}
					"""
			)
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "400",
		description = "BAD_REQUEST",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class)
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "401",
		description = "UNAUTHORIZED",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class)
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "500",
		description = "INTERNAL_SERVER_ERROR",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ErrorResponse.class)
		)
	)
	ResponseEntity<ApiResponse<ConcertResponse.GetAvailableConcertDates>> getAvailableConcertDates(
		@PathVariable("id") long id,
		@RequestParam(value = "page", required = false, defaultValue = "1") int page
	);
	//
	@Operation(summary = "예약가능한 콘서트좌석 정보조회", description="특정 콘서트와 예약가능한 날짜에서 예약가능한 좌석목록을 반환 (최소 1개~최대 50개)")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "200",
		description = "OK",
		content= @Content(
			mediaType = "application/json",
			examples = @ExampleObject(
				name = "예약 가능한 콘서트 좌석 목록 조회 성공 응답 예시",
				summary = "예약 가능한 콘서트 좌석 목록 조회 성공 응답 데이터",
				value= """
				{
				  "status": 200,
				  "message": "OK",
				  "data": [
						{
							"id": 51,
							"number": 1,
							"isAvailable": true
						},{
							"id": 52,
							"number": 2,
							"isAvailable": true
						},{
							"id": 53,
							"number": 3,
							"isAvailable": false
						}
					]
				}
				"""
			)
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "401",
		description = "UNAUTHORIZED",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ErrorResponse.class)
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "400",
		description = "BAD_REQUEST",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ErrorResponse.class),
			examples= {
				@ExampleObject(
					name="ID_SHOULD_BE_POSITIVE_NUMBER",
					summary = "id는 0보다 큰 양수이다"
				),
			}
		)
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponse(
		responseCode = "500",
		description = "INTERNAL_SERVER_ERROR",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ErrorResponse.class),
			examples= {
				@ExampleObject(
					name = "INTERNAL_SERVER_ERROR",
					summary = "서버내부 에러"
				)
			}
		)
	)
	ResponseEntity<ApiResponse<ConcertResponse.GetAvailableSeats>> getAvailableSeats(
		@PathVariable("id") long id,
		@RequestParam("date-id") long dateId
	);

}
