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

import io.hhplus.concert.domain.concert.entity.Seat;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponseEntity;
import io.hhplus.concert.interfaces.api.concert.dto.SeatResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("concerts")
@RequiredArgsConstructor
public class ConcertController {
	// 예약가능 날짜 목록조회
	@GetMapping("{id}/dates")
	public ResponseEntity<ApiResponse<List<String>>> getAvailableConcertDate(@PathVariable("id") long id, @RequestHeader("token") String token) {
		List<String> dates = List.of("2025-04-04", "2025-04-11");
		ApiResponse<List<String>> response = ApiResponse.ok(dates);
		return ResponseEntity.ok(response);
	}

	// 특정날짜에서 예약가능한 좌석정보조회
	@GetMapping("{id}/seats?date={date}")
	public ResponseEntity<ApiResponse<List<SeatResponse>>> getAvailableSeats(@PathVariable("id") long id, @RequestParam("date")LocalDate date,  @RequestHeader("token") String token) {
		List<SeatResponse> availableSeats = List.of(
			SeatResponse.of(51L, 1, true),
			SeatResponse.of(52L, 2, true),
			SeatResponse.of(53L, 3, true)
		);
		ApiResponse<List<SeatResponse>> response = ApiResponse.ok(availableSeats);
		return ResponseEntity.ok(response);
	}
}
