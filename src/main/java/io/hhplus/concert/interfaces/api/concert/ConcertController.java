package io.hhplus.concert.interfaces.api.concert;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.interfaces.api.common.ApiResponse;
import io.hhplus.concert.interfaces.api.common.ApiResponseEntity;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("concerts")
@RequiredArgsConstructor
public class ConcertController implements ConcertApiDocs {
	private final ConcertService concertService;
	// 콘서트 목록 조회
	@GetMapping("/")
	public ResponseEntity<ApiResponse<ConcertResponse.GetConcerts>> getConcerts(
		@RequestParam(value = "page", required = false, defaultValue = "1") int page
	) {
		ConcertInfo.GetConcertList result = concertService.getConcertList(ConcertCommand.GetConcertList.of(page));
		return ApiResponseEntity.ok(ConcertResponse.GetConcerts.from(result));
	}


	// 콘서트의 예약가능 날짜 목록조회
	@GetMapping("/{id}/dates/list")
	public ResponseEntity<ApiResponse<ConcertResponse.GetAvailableConcertDates>> getAvailableConcertDates(
		@PathVariable("id") long id,
		@RequestParam(value = "page", required = false, defaultValue = "1") int page
	) {
		ConcertInfo.GetConcertDateList result = concertService.getConcertDateList(ConcertCommand.GetConcertDateList.of(page, id));
		return ApiResponseEntity.ok(ConcertResponse.GetAvailableConcertDates.from(result));
	}

	// 특정날짜에서 예약가능한 좌석정보조회
	@GetMapping("/{id}/dates/{date-id}/seats")
	public ResponseEntity<ApiResponse<ConcertResponse.GetAvailableSeats>> getAvailableSeats(
		@PathVariable("id") long id,
		@RequestParam("date-id") long dateId
	) {
		ConcertInfo.GetConcertSeatList result = concertService.getConcertSeatList(ConcertCommand.GetConcertSeatList.of(id, dateId));
		return ApiResponseEntity.ok(ConcertResponse.GetAvailableSeats.from(result));
	}
}
