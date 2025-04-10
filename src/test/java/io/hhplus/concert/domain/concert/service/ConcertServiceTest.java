package io.hhplus.concert.domain.concert.service;

import static io.hhplus.concert.domain.common.entity.BaseEntity.*;
import static io.hhplus.concert.domain.concert.exceptions.messages.ConcertExceptionMessage.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.concert.entity.Concert;
import io.hhplus.concert.domain.concert.entity.ConcertDate;
import io.hhplus.concert.domain.concert.entity.ConcertSeat;
import io.hhplus.concert.domain.concert.repository.ConcertDateRepository;
import io.hhplus.concert.domain.concert.repository.ConcertRepository;
import io.hhplus.concert.domain.concert.repository.ConcertSeatRepository;
import io.hhplus.concert.domain.user.entity.User;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertDateResponse;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertResponse;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertSeatDetailResponse;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertSeatResponse;

@ExtendWith(MockitoExtension.class)
public class ConcertServiceTest {
	@InjectMocks
	private ConcertService concertService;

	@Mock
	private ConcertRepository concertRepository;

	@Mock
	private ConcertDateRepository concertDateRepository;

	@Mock
	private ConcertSeatRepository concertSeatRepository;

	@BeforeEach
	void setUp() {
		concertService = new ConcertService(concertRepository, concertDateRepository, concertSeatRepository);
	}

	private static final Logger log = LoggerFactory.getLogger(ConcertServiceTest.class);

	@Test
	void 콘서트_목록조회를_성공한다() {
		// given
		List<Concert> list = new ArrayList<>();
		list.add(new Concert(1L, "벚꽃바람이 부는 재즈패스티벌 콘서트", "테스트 재즈아티스트"));
		list.add(new Concert(2L, "진달래와 함께하는 피아노 공연", "피아노 아티스트"));
		when(concertRepository.findAll()).thenReturn(list);

		// when
		List<ConcertResponse> concertResponse = concertService.getConcertList();

		// then
		assertEquals(2, concertResponse.size());
		assertEquals(1, concertResponse.get(0).id());
		assertEquals("벚꽃바람이 부는 재즈패스티벌 콘서트", concertResponse.get(0).name());
		assertEquals("테스트 재즈아티스트", concertResponse.get(0).artistName());

		assertEquals(2, concertResponse.get(1).id());
		assertEquals("진달래와 함께하는 피아노 공연", concertResponse.get(1).name());
		assertEquals("피아노 아티스트", concertResponse.get(1).artistName());
	}
	@Test
	void 콘서트목록_조회시_페이지가_1미만의_정수이면_InvalidValidationException_예외발생() {
		// given
		int page = 0;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> concertService.getConcertList(page)
		);
		assertEquals(INVALID_PAGE, ex.getMessage());
	}
	@Test
	void 전체데이터는_12개이고_콘서트목록의_1페이지_조회하게되면_10개의_데이터가_존재시_성공한다() {
		// given
		int page = 1;
		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		List<Concert> list = new ArrayList<>();
		for(int i = 1; i <= 12; i++) {
			list.add(
				new Concert(i, "언제나 즐거운 TDD 테스트콘서트"+i , "아티스트"+i)
			);
		}
		List<Concert> listOfFirstPage = list.subList(0, 10);
		Page<Concert> concertPage = new PageImpl<>(listOfFirstPage, pageable, list.size());
		when(concertRepository.findAll(pageable)).thenReturn(concertPage);

		// when
		List<ConcertResponse> result = concertService.getConcertList(page);

		// then
		assertEquals(10, result.size());
		// 1페이지 1번째 항목 결과
		assertEquals("언제나 즐거운 TDD 테스트콘서트"+1, result.get(0).name());
		assertEquals("아티스트"+1, result.get(0).artistName());
		// 1페이지 2번째 항목 결과
		assertEquals("언제나 즐거운 TDD 테스트콘서트"+2, result.get(1).name());
		assertEquals("아티스트"+2, result.get(1).artistName());
		// 1페이지 10번째 항목 결과
		assertEquals("언제나 즐거운 TDD 테스트콘서트"+10, result.get(9).name());
		assertEquals("아티스트"+10, result.get(9).artistName());
	}
	@Test
	void 전체데이터는_12개이고_콘서트목록의_2페이지_조회하게되면_2개의_데이터가_존재시_성공한다() {
		// given
		int page = 2;
		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		List<Concert> list = new ArrayList<>();
		for(int i = 1; i <= 12; i++) {
			list.add(
				new Concert(i, "언제나 즐거운 TDD 테스트콘서트"+i , "아티스트"+i)
			);
		}

		List<Concert> listOfSecondPage = list.subList(10, 12);
		Page<Concert> concertPage = new PageImpl<>(listOfSecondPage, pageable, list.size());
		when(concertRepository.findAll(pageable)).thenReturn(concertPage);

		// when
		List<ConcertResponse> result = concertService.getConcertList(page);

		// then
		assertEquals(2, result.size());
		// 2페이지 1번째 항목 결과
		assertEquals(11, result.get(0).id());
		assertEquals("언제나 즐거운 TDD 테스트콘서트"+11, result.get(0).name());
		assertEquals("아티스트"+11, result.get(0).artistName());
		// 2페이지 2번째 항목 결과
		assertEquals(12, result.get(1).id());
		assertEquals("언제나 즐거운 TDD 테스트콘서트"+12, result.get(1).name());
		assertEquals("아티스트"+12, result.get(1).artistName());
	}
	@Test
	void 콘서트_날짜목록_조회시_페이지가_1미만의_정수이면_InvalidValidationException_예외발생() {
		// given
		int page = 0;
		long concertId = 1L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> concertService.getConcertDateList(page, concertId)
		);
		assertEquals(INVALID_PAGE, ex.getMessage());
	}
	@Test
	void 전체데이터는_19개이고_콘서트_날짜목록_1페이지_조회하게되면_10개의_데이터가_존재시_성공한다() {
		// given
		long concertId = 1;
		int page = 1;
		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		List<ConcertDate> list = new ArrayList<>();
		LocalDate currentDate = LocalDate.now();

		for(int i = 1; i <= 19; i++) {
			LocalDate concertDate;
			if(i == 1) {
				concertDate= currentDate;
			} else {
				concertDate = currentDate.plusDays(i-1);
			}

			list.add(
				new ConcertDate(i, concertDate, true, "테스트 콘서트 장소"+i)
			);
		}

		List<ConcertDate> listOfFirstPage = list.subList(0, 10);
		Page<ConcertDate> concertDatePage = new PageImpl<>(listOfFirstPage, pageable, list.size());
		when(concertDateRepository.findAll(concertId, pageable)).thenReturn(concertDatePage);

		// when
		List<ConcertDateResponse> result = concertService.getConcertDateList(page, concertId);

		// then
		assertEquals(10, result.size());
		// 1페이지 1번째 데이터 검증
		assertEquals(1, result.get(0).id());
		assertEquals(currentDate, result.get(0).progressDate());
		assertEquals(true, result.get(0).isAvailable());
		assertEquals("테스트 콘서트 장소"+1, result.get(0).place());
		// 1페이지 2번째 데이터 검증
		assertEquals(2, result.get(1).id());
		assertEquals(currentDate.plusDays(1), result.get(1).progressDate());
		assertEquals(true, result.get(1).isAvailable());
		assertEquals("테스트 콘서트 장소"+2, result.get(1).place());
		// 1페이지 10번째 데이터 검증
		assertEquals(10, result.get(9).id());
		assertEquals(currentDate.plusDays(9), result.get(9).progressDate());
		assertEquals(true, result.get(9).isAvailable());
		assertEquals("테스트 콘서트 장소"+10, result.get(9).place());
	}
	@Test
	void 전체데이터는_19개이고_콘서트의_날짜목록_2페이지_조회하게되면_9개데이터가_존재시_성공한다() {
		// given
		long concertId = 1;
		int page = 1;
		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		List<ConcertDate> list = new ArrayList<>();
		LocalDate currentDate = LocalDate.now();

		for(int i = 1; i <= 19; i++) {
			LocalDate concertDate;
			if(i == 1) {
				concertDate= currentDate;
			} else {
				concertDate = currentDate.plusDays(i-1);
			}

			list.add(
				new ConcertDate(i, concertDate, true, "테스트 콘서트 장소"+i)
			);
		}

		List<ConcertDate> listOfSecondPage = list.subList(10, 19);
		Page<ConcertDate> concertDatePage = new PageImpl<>(listOfSecondPage, pageable, list.size());
		when(concertDateRepository.findAll(concertId, pageable)).thenReturn(concertDatePage);

		// when
		List<ConcertDateResponse> result = concertService.getConcertDateList(page, concertId);

		// then
		assertEquals(9, result.size());
		// 1페이지 1번째 데이터 검증
		assertEquals(11, result.get(0).id());
		assertEquals(currentDate.plusDays(10), result.get(0).progressDate());
		assertEquals(true, result.get(0).isAvailable());
		assertEquals("테스트 콘서트 장소"+11, result.get(0).place());
		// 1페이지 2번째 데이터 검증
		assertEquals(12, result.get(1).id());
		assertEquals(currentDate.plusDays(11), result.get(1).progressDate());
		assertEquals(true, result.get(1).isAvailable());
		assertEquals("테스트 콘서트 장소"+12, result.get(1).place());
		// 1페이지 9번째 데이터 검증
		assertEquals(19, result.get(8).id());
		assertEquals(currentDate.plusDays(18), result.get(8).progressDate());
		assertEquals(true, result.get(8).isAvailable());
		assertEquals("테스트 콘서트 장소"+19, result.get(8).place());
	}
	@Test
	void 콘서트좌석조회에_성공한다() {
		// given
		long concertId = 1L;
		long concertDateId = 2L;

		/*
		* | 좌석 ID | 좌석번호 범위  |    가격    |
		* | 51~60  | 1~ 10       |   1000    |
		* | 61~70  | 11~20       |   2000    |
		* | 71~80  | 21~30       |   3000    |
		* | 81~90  | 31~40       |   4000    |
		* | 91~100 | 41~50       |   5000    |
		* */
		List<ConcertSeat> list = new ArrayList<>();
		for(int i = 0 ; i < 50; i++) {
			long id = ( i + 50 ) + 1;
			int number = i + 1;
			long price = 1000 * ( i / 10 + 1);
			boolean isAvailable = true;
			if( number % 5 == 0) isAvailable = false; // 5의배수인 좌석번호는 예약불가능 상태

			list.add(new ConcertSeat(id, number, price, isAvailable));
		}
		when(concertSeatRepository.findConcertSeats(concertId, concertDateId)).thenReturn(list);

		// when
		List<ConcertSeatResponse> concertSeatResponse = concertService.getConcertSeatList(concertId, concertDateId);

		// then
		assertEquals(50, concertSeatResponse.size());
		// 1번 좌석
		assertEquals(51, concertSeatResponse.get(0).id());
		assertEquals(1, concertSeatResponse.get(0).number());
		assertEquals(1000, concertSeatResponse.get(0).price());
		assertEquals(true, concertSeatResponse.get(0).isAvailable());
		// 25번 좌석
		assertEquals(75, concertSeatResponse.get(24).id());
		assertEquals(25, concertSeatResponse.get(24).number());
		assertEquals(3000, concertSeatResponse.get(24).price());
		assertEquals(false, concertSeatResponse.get(24).isAvailable());
		// 50번 좌석
		assertEquals(100, concertSeatResponse.get(49).id());
		assertEquals(50, concertSeatResponse.get(49).number());
		assertEquals(5000, concertSeatResponse.get(49).price());
		assertEquals(false, concertSeatResponse.get(49).isAvailable());
	}
	@Test
	void 콘서트_좌석정보조회_요청시_콘서트좌석ID에_대응되는_콘서트좌석_정보가_존재하지않으면_NotFoundException_예외발생() {
		// given
		long concertSeatId = 1L;
		when(concertSeatRepository.getConcertSeatInfo(concertSeatId)).thenReturn(null);

		// when & then
		NotFoundException ex = assertThrows(
			NotFoundException.class,
			() -> concertService.getConcertSeatInfo(concertSeatId)
		);
		assertEquals(CONCERT_SEAT_NOT_FOUND, ex.getMessage());
	}
	@Test
	void 콘서트_좌석정보조회_요청시_콘서트좌석ID에_대응되는_콘서트좌석정보가_존재하면_정보조회를_성공한다() {
		// given
		long concertSeatId = 1L;
		ConcertSeatDetailResponse expectedSeatInfo = new ConcertSeatDetailResponse(
			1L,
			1,
			15000,
			false,
			1L,
			1L
		);
		when(concertSeatRepository.getConcertSeatInfo(concertSeatId)).thenReturn(expectedSeatInfo);

		// when & then
		ConcertSeatDetailResponse result = assertDoesNotThrow(() -> concertService.getConcertSeatInfo(concertSeatId));
		assertEquals(result.id(), expectedSeatInfo.id());
		assertEquals(result.number(), expectedSeatInfo.number());
		assertEquals(result.price(), expectedSeatInfo.price());
		assertEquals(result.isAvailable(), expectedSeatInfo.isAvailable());
		assertEquals(result.concertId(), expectedSeatInfo.concertId());
		assertEquals(result.concertDateId(), expectedSeatInfo.concertDateId());
	}
	@Test
	void 콘서트좌석ID에_일치하는_콘서트좌석정보가_없는경우_NotFoundException_예외발생() {
		// given
		long concertSeatId = 1L;
		when(concertSeatRepository.findConcertSeatById(concertSeatId)).thenReturn(null);

		// when & then
		NotFoundException ex = assertThrows(
			NotFoundException.class,
			() -> concertService.getConcertSeatEntityById(concertSeatId)
		);
		assertEquals(CONCERT_SEAT_NOT_FOUND, ex.getMessage());
	}
}
