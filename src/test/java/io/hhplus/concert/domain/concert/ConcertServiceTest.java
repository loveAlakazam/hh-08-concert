package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.interfaces.api.common.validators.PaginationValidator.*;
import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
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

import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

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
	void 페이징처리를_제외하고_콘서트_목록조회를_성공한다() {
		// given
		List<Concert> list = new ArrayList<>();
		list.add(Concert.of("벚꽃바람이 부는 재즈패스티벌 콘서트", "재즈 아티스트"));
		list.add(Concert.of("남산의 밤하늘과 함께하는 피아노 공연", "피아노 아티스트"));
		when(concertRepository.findAll()).thenReturn(list);

		// when
		List<Concert> result = concertService.getConcertList();

		// then
		assertEquals(2, result.size());
		assertEquals("벚꽃바람이 부는 재즈패스티벌 콘서트", result.get(0).getName());
		assertEquals("재즈 아티스트", result.get(0).getArtistName());

		assertEquals("남산의 밤하늘과 함께하는 피아노 공연", result.get(1).getName());
		assertEquals("피아노 아티스트", result.get(1).getArtistName());
	}
	@Test
	void 콘서트목록_조회시_페이지가_1미만의_정수이면_InvalidValidationException_예외발생() {
		// given
		int page = 0;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> concertService.getConcertList(ConcertCommand.GetConcertList.of(page))
		);
		assertEquals(INVALID_PAGE.getMessage(), ex.getMessage());
		assertEquals(INVALID_PAGE.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 전체데이터는_12개이고_콘서트목록의_1페이지_조회하게되면_10개의_데이터가_존재시_성공한다() {
		// given
		int page = 1;

		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		List<Concert> list = new ArrayList<>();
		for(int i = 1; i <= 12; i++) {
			list.add(Concert.of("언제나 즐거운 TDD 테스트콘서트"+i , "아티스트"+i));
		}

		List<Concert> listOfFirstPage = list.subList(0, 10);
		Page<Concert> concertPage = new PageImpl<>(listOfFirstPage, pageable, list.size());
		when(concertRepository.findAll(pageable)).thenReturn(concertPage);

		// when
		ConcertInfo.GetConcertList result = concertService.getConcertList(ConcertCommand.GetConcertList.of(page));

		List<Concert> pageResult = result.concertPage().getContent();
		int totalPages = result.concertPage().getTotalPages();
		long totalElements = result.concertPage().getTotalElements();
		long numberOfElementsInPage = result.concertPage().getNumberOfElements();

		// then
		assertEquals(10, pageResult.size());
		assertEquals(2, totalPages);
		assertEquals(12, totalElements);
		assertEquals(10, numberOfElementsInPage);

		// 1페이지 1번째 항목 결과
		assertEquals("언제나 즐거운 TDD 테스트콘서트"+1, pageResult.get(0).getName());
		assertEquals("아티스트"+1, pageResult.get(0).getArtistName());
		// 1페이지 2번째 항목 결과
		assertEquals("언제나 즐거운 TDD 테스트콘서트"+2, pageResult.get(1).getName());
		assertEquals("아티스트"+2, pageResult.get(1).getArtistName());
		// 1페이지 10번째 항목 결과
		assertEquals("언제나 즐거운 TDD 테스트콘서트"+10, pageResult.get(9).getName());
		assertEquals("아티스트"+10, pageResult.get(9).getArtistName());
	}
	@Test
	void 전체데이터는_12개이고_콘서트목록의_2페이지_조회하게되면_2개의_데이터가_존재시_성공한다() {
		// given
		int page = 2;
		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		List<Concert> list = new ArrayList<>();
		for(int i = 1; i <= 12; i++) {
			list.add(Concert.of("언제나 즐거운 TDD 테스트콘서트"+i , "아티스트"+i));
		}

		List<Concert> listOfSecondPage = list.subList(10, 12);
		Page<Concert> concertPage = new PageImpl<>(listOfSecondPage, pageable, list.size());
		when(concertRepository.findAll(pageable)).thenReturn(concertPage);

		// when
		ConcertInfo.GetConcertList result = concertService.getConcertList(ConcertCommand.GetConcertList.of(page));

		List<Concert> pageResult = result.concertPage().getContent();
		int totalPages = result.concertPage().getTotalPages();
		long totalElements = result.concertPage().getTotalElements();
		long numberOfElementsInPage = result.concertPage().getNumberOfElements();

		// then
		assertEquals(2, pageResult.size());
		assertEquals(2, totalPages);
		assertEquals(12, totalElements);
		assertEquals(2, numberOfElementsInPage);

		// 2페이지 1번째 항목 결과
		assertEquals("언제나 즐거운 TDD 테스트콘서트"+11, pageResult.get(0).getName());
		assertEquals("아티스트"+11, pageResult.get(0).getArtistName());
		// 2페이지 2번째 항목 결과
		assertEquals("언제나 즐거운 TDD 테스트콘서트"+12, pageResult.get(1).getName());
		assertEquals("아티스트"+12, pageResult.get(1).getArtistName());
	}
	@Test
	void 콘서트_날짜목록_조회시_페이지가_1미만의_정수이면_InvalidValidationException_예외발생() {
		// given
		int page = 0;
		long concertId = 1L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> concertService.getConcertDateList(ConcertCommand.GetConcertDateList.of(page, concertId))
		);
		assertEquals(INVALID_PAGE.getMessage(), ex.getMessage());
		assertEquals(INVALID_PAGE.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 전체데이터는_19개이고_콘서트_날짜목록_1페이지_조회하게되면_10개의_데이터가_존재시_성공한다() {
		// given
		long concertId = 1;
		int page = 1;
		LocalDate now = LocalDate.now();

		Concert concert = Concert.of("테스트 콘서트", "테스트 아티스트");
		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		List<ConcertDate> list = new ArrayList<>();

		for(int i = 1; i <= 19; i++) {
			list.add(ConcertDate.of(concert, now.plusDays(i), true, "테스트 콘서트 장소"+i));
		}

		List<ConcertDate> listOfFirstPage = list.subList(0, 10);
		Page<ConcertDate> concertDatePage = new PageImpl<>(listOfFirstPage, pageable, list.size());
		when(concertDateRepository.findAll(concertId, pageable)).thenReturn(concertDatePage);

		// when
		ConcertInfo.GetConcertDateList result = concertService.getConcertDateList(ConcertCommand.GetConcertDateList.of(page, concertId));

		List<ConcertDate> pageResult = result.concertDatePage().getContent();
		int totalPages = result.concertDatePage().getTotalPages();
		long totalElements = result.concertDatePage().getTotalElements();
		long numberOfElementsInPage = result.concertDatePage().getNumberOfElements();

		// then
		assertEquals(10, pageResult.size());
		assertEquals(2, totalPages);
		assertEquals(19, totalElements);
		assertEquals(10, numberOfElementsInPage);

		// 1페이지 1번째 데이터 검증
		assertEquals(now.plusDays(1), pageResult.get(0).getProgressDate());
		assertEquals(true, pageResult.get(0).isAvailable());
		assertEquals("테스트 콘서트 장소"+1, pageResult.get(0).getPlace());
		// 1페이지 2번째 데이터 검증
		assertEquals(now.plusDays(2), pageResult.get(1).getProgressDate());
		assertEquals(true, pageResult.get(1).isAvailable());
		assertEquals("테스트 콘서트 장소"+2, pageResult.get(1).getPlace());
		// 1페이지 10번째 데이터 검증
		assertEquals(now.plusDays(10), pageResult.get(9).getProgressDate());
		assertEquals(true, pageResult.get(9).isAvailable());
		assertEquals("테스트 콘서트 장소"+10, pageResult.get(9).getPlace());
	}
	@Test
	void 전체데이터는_19개이고_콘서트의_날짜목록_2페이지_조회하게되면_9개데이터가_존재시_성공한다() {
		// given
		long concertId = 1;
		int page = 2;
		LocalDate now = LocalDate.now();

		Concert concert = Concert.of("테스트 콘서트", "테스트 아티스트");
		Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
		List<ConcertDate> list = new ArrayList<>();

		for(int i = 1; i <= 19; i++) {
			list.add(ConcertDate.of(concert, now.plusDays(i), true, "테스트 콘서트 장소"+i));
		}

		List<ConcertDate> listOfSecondPage = list.subList(10, 19);
		Page<ConcertDate> concertDatePage = new PageImpl<>(listOfSecondPage, pageable, list.size());
		when(concertDateRepository.findAll(concertId, pageable)).thenReturn(concertDatePage);

		// when
		ConcertInfo.GetConcertDateList result = concertService.getConcertDateList(ConcertCommand.GetConcertDateList.of(page, concertId));
		List<ConcertDate> pageResult = result.concertDatePage().getContent();
		int totalPages = result.concertDatePage().getTotalPages();
		long totalElements = result.concertDatePage().getTotalElements();
		long numberOfElementsInPage = result.concertDatePage().getNumberOfElements();

		// then
		assertEquals(9, pageResult.size());
		assertEquals(2, totalPages);
		assertEquals(19, totalElements);
		assertEquals(9, numberOfElementsInPage);

		// 1페이지 1번째 데이터 검증
		assertEquals(now.plusDays(11), pageResult.get(0).getProgressDate());
		assertEquals(true, pageResult.get(0).isAvailable());
		assertEquals("테스트 콘서트 장소"+11, pageResult.get(0).getPlace());
		// 1페이지 2번째 데이터 검증
		assertEquals(now.plusDays(12), pageResult.get(1).getProgressDate());
		assertEquals(true, pageResult.get(1).isAvailable());
		assertEquals("테스트 콘서트 장소"+12, pageResult.get(1).getPlace());
		// 1페이지 10번째 데이터 검증
		assertEquals(now.plusDays(19), pageResult.get(8).getProgressDate());
		assertEquals(true, pageResult.get(8).isAvailable());
		assertEquals("테스트 콘서트 장소"+19, pageResult.get(8).getPlace());
	}
	@Test
	void 콘서트아이디1_콘서트날짜아이디2인_콘서트좌석_조회에_성공한다() {
		// given
		long concertId = 1L;
		long concertDateId = 2L;

		Concert concert = Concert.of("테스트 콘서트", "테스트 아티스트");
		ConcertDate concertDate = ConcertDate.of( concert, LocalDate.now(), true, "테스트 장소");

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
			int number = i + 1;
			long price = 1000 * ( i / 10 + 1);
			boolean isAvailable = true;
			if( number % 5 == 0) isAvailable = false; // 5의배수인 좌석번호는 예약불가능 상태

			list.add(ConcertSeat.of(concert, concertDate, number, price, isAvailable));
		}
		when(concertSeatRepository.findConcertSeats(concertId, concertDateId)).thenReturn(list);

		// when
		ConcertInfo.GetConcertSeatList result = concertService.getConcertSeatList(
			ConcertCommand.GetConcertSeatList.of(concertId, concertDateId)
		);
		List<ConcertSeat> concertSeatList = result.concertSeatList();

		// then
		assertEquals(50, concertSeatList.size());
		// 1번 좌석
		assertEquals(1, concertSeatList.get(0).getNumber());
		assertEquals(1000, concertSeatList.get(0).getPrice());
		assertEquals(true, concertSeatList.get(0).isAvailable());
		// 25번 좌석
		assertEquals(25, concertSeatList.get(24).getNumber());
		assertEquals(3000, concertSeatList.get(24).getPrice());
		assertEquals(false, concertSeatList.get(24).isAvailable());
		// 50번 좌석
		assertEquals(50, concertSeatList.get(49).getNumber());
		assertEquals(5000, concertSeatList.get(49).getPrice());
		assertEquals(false, concertSeatList.get(49).isAvailable());
	}
	@Test
	void 콘서트좌석_정보조회요청시_콘서트좌석ID에_대응되는_콘서트좌석_정보가_존재하지않으면_BusinessException_예외발생() {
		// given
		long concertSeatId = 1L;
		when(concertSeatRepository.getConcertSeatInfo(concertSeatId)).thenReturn(null);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> concertService.getConcertSeat(
				ConcertCommand.GetConcertSeat.of(concertSeatId)
			)
		);
		assertEquals(CONCERT_SEAT_NOT_FOUND.getMessage(), ex.getMessage());
		assertEquals(CONCERT_SEAT_NOT_FOUND.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 콘서트좌석_정보조회요청시_ID에_일치하는_콘서트좌석이_이미_예약이된경우_BusinessException_예외발생() {
		// given
		long concertSeatId = 1L;
		Concert concert = Concert.of("테스트 콘서트", "테스트 아티스트");
		ConcertDate concertDate = ConcertDate.of( concert, LocalDate.now(), true, "테스트 장소");
		ConcertSeat alreadyReservedSeat = ConcertSeat.of(concert, concertDate, 1, 1500, false);
		when(concertSeatRepository.getConcertSeatInfo(concertSeatId)).thenReturn(alreadyReservedSeat);

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> concertService.getConcertSeat(
				ConcertCommand.GetConcertSeat.of(concertSeatId)
			)
		);
		assertEquals(ALREADY_RESERVED_SEAT.getMessage(), ex.getMessage());
		assertEquals(ALREADY_RESERVED_SEAT.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 콘서트_좌석정보조회_요청시_콘서트좌석ID에_대응되는_콘서트좌석정보가_존재하면_정보조회를_성공한다() {
		// given
		long concertSeatId = 1L;
		Concert concert = Concert.of("테스트 콘서트", "테스트 아티스트");
		ConcertDate concertDate = ConcertDate.of( concert, LocalDate.now(), true, "테스트 장소");
		ConcertSeat concertSeat = ConcertSeat.of(concert, concertDate, 1, 1500, true);
		when(concertSeatRepository.getConcertSeatInfo(concertSeatId)).thenReturn(concertSeat);

		// when & then
		ConcertInfo.GetConcertSeat result = assertDoesNotThrow(
			() -> concertService.getConcertSeat(ConcertCommand.GetConcertSeat.of(concertSeatId))
		);
		assertEquals(1, result.concertSeat().getNumber());
		assertEquals(1500, result.concertSeat().getPrice());
		assertEquals(true, result.concertSeat().isAvailable());
		assertEquals("테스트 콘서트", result.concertSeat().getConcert().getName());
		assertEquals("테스트 장소", result.concertSeat().getConcertDate().getPlace());
		assertEquals(LocalDate.now(), result.concertSeat().getConcertDate().getProgressDate());
	}
	@Test
	void 좌석가격이_최소금액보다_적은금액으로_공연일정을_생성할경우_InvalidValidationException_예외발생() {
		// given
		long invalidPrice = 100L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> concertService.create(
				ConcertCommand.CreateConcert.of(
					"TDD 테스트 콘서트",
					"테스트 아티스트",
					LocalDate.now(),
					"테스트 장소",
					invalidPrice
				)
			)
		);
		assertEquals(PRICE_SHOULD_BE_MORE_THAN_MINIMUM_PRICE.getMessage(), ex.getMessage());
		assertEquals(PRICE_SHOULD_BE_MORE_THAN_MINIMUM_PRICE.getHttpStatus(), ex.getHttpStatus());
	}
	@Test
	void 좌석가격이_최대금액보다_큰금액으로_공연일정을_생성할경우_InvalidValidationException_예외발생() {
		// given
		long invalidPrice = 10_000_000L;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> concertService.create(
				ConcertCommand.CreateConcert.of(
					"TDD 테스트 콘서트",
					"테스트 아티스트",
					LocalDate.now(),
					"테스트 장소",
					invalidPrice
				)
			)
		);
		assertEquals(PRICE_SHOULD_BE_LESS_THAN_MAXIMUM_PRICE.getMessage(), ex.getMessage());
		assertEquals(PRICE_SHOULD_BE_LESS_THAN_MAXIMUM_PRICE.getHttpStatus(), ex.getHttpStatus());
	}

}
