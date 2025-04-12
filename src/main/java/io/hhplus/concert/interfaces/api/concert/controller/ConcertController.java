package io.hhplus.concert.interfaces.api.concert.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponseEntity;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertSeatResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("concerts")
@RequiredArgsConstructor
public class ConcertController implements ConcertApiDocs {
	// 콘서트 목록 조회

	// 예약가능 날짜 목록조회
	@GetMapping("{id}/dates")
	public ResponseEntity<ApiResponse<List<String>>> getAvailableConcertDates(@PathVariable("id") long id, @RequestHeader("token") String token) {
		List<String> dates = List.of("2025-04-04", "2025-04-11");
		return ApiResponseEntity.ok(dates);
	}

	// 특정날짜에서 예약가능한 좌석정보조회
	@GetMapping("{id}/seats")
	public ResponseEntity<ApiResponse<List<ConcertSeatResponse>>> getAvailableSeats(@PathVariable("id") long id, @RequestParam("date")LocalDate date,  @RequestHeader("token") String token) {
		List<ConcertSeatResponse> availableSeats = List.of(
			ConcertSeatResponse.of(51L, 1, 10000, true),
			ConcertSeatResponse.of(52L, 2, 10000, true),
			ConcertSeatResponse.of(53L, 3, 10000, true)
		);
		return ApiResponseEntity.ok(availableSeats);
	}
}
