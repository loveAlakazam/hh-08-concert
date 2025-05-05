package io.hhplus.concert.interfaces.api.concert;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.interfaces.api.common.ApiResponse;
import io.hhplus.concert.interfaces.api.common.ApiResponseEntity;
import io.hhplus.concert.interfaces.api.common.PaginationUtils;
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
		// 리스트를 반환
		ConcertInfo.GetConcertList info = concertService.getConcertList();
		// 페이징처리를 한다
		Page<ConcertInfo.GetConcertListDto> concertPages = PaginationUtils.toPage(info.concerts(), page);
		// 페이징처리결과를 응답데이터에 넣어서 응답
		return ApiResponseEntity.ok(ConcertResponse.GetConcerts.from(concertPages));
	}


	// 콘서트의 예약가능 날짜 목록조회
	@GetMapping("/{id}/dates/list")
	public ResponseEntity<ApiResponse<ConcertResponse.GetAvailableConcertDates>> getAvailableConcertDates(
		@PathVariable("id") long id,
		@RequestParam(value = "page", required = false, defaultValue = "1") int page
	) {
		// 리스트를 반환
		ConcertInfo.GetConcertDateList info = concertService.getConcertDateList(ConcertCommand.GetConcertDateList.of(id));
		// 페이징처리를 한다
		Page<ConcertInfo.GetConcertDateListDto> concertDatePage = PaginationUtils.toPage(info.concertDates(), page);
		// 페이징처리 결과를 응답데이터에 넣고 응답한다
		return ApiResponseEntity.ok(ConcertResponse.GetAvailableConcertDates.from(concertDatePage));
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
