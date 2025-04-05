package io.hhplus.concert.interfaces.api.concert.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("concerts")
@RequiredArgsConstructor
public class ConcertController {
	// 예약가능 날짜 목록조회
	@GetMapping("{concert_id}/dates")
	public void getAvailableConcertDate() {}

	// 특정날짜에서 예약가능한 좌석정보조회
	@GetMapping("{concert_id}/seats?date={date}")
	public void getAvailableSeats() {}
}
