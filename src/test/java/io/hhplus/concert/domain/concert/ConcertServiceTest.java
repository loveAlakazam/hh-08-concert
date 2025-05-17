package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
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
import org.springframework.core.annotation.Order;
import org.springframework.test.util.ReflectionTestUtils;


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

	@Order(1)
	@Test
	void 콘서트목록_데이터_조회를_성공한다() {
		// given
		List<Concert> dbConcerts = new ArrayList<>();
		dbConcerts.add(Concert.of("벚꽃바람이 부는 재즈패스티벌 콘서트", "재즈 아티스트"));
		dbConcerts.add(Concert.of("남산의 밤하늘과 함께하는 피아노 공연", "피아노 아티스트"));
		ConcertInfo.GetConcertList expected = ConcertInfo.GetConcertList.from(dbConcerts);

		when(concertRepository.findAll()).thenReturn(expected);

		// when
		ConcertInfo.GetConcertList result = concertService.getConcertList();

		// then
		assertThat(result).isEqualTo(expected);
		verify(concertRepository, times(1)).findAll();
	}
	@Order(2)
	@Test
	void 캐시스토어에_콘서트아이디1_콘서트날짜아이디2인_콘서트좌석목록_조회를_성공한다() {
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
		List<ConcertSeat> dbConcertSeats = new ArrayList<>();
		for(int i = 0 ; i < 50; i++) {
			int number = i + 1;
			long price = 1000 * ( i / 10 + 1);
			boolean isAvailable = number % 5 != 0; // 5의배수인 좌석번호는 예약불가능 상태

			dbConcertSeats.add(ConcertSeat.of(concert, concertDate, number, price, isAvailable));
		}

		ConcertInfo.GetConcertSeatList expected = ConcertInfo.GetConcertSeatList.from(dbConcertSeats);
		when(concertSeatRepository.findConcertSeats(concertId, concertDateId)).thenReturn(expected);

		// when
		ConcertInfo.GetConcertSeatList result = concertService.getConcertSeatList(
			ConcertCommand.GetConcertSeatList.of(concertId, concertDateId)
		);
		List<ConcertInfo.ConcertSeatListDto> concertSeatList = result.concertSeatList();

		// then
		verify(concertSeatRepository, times(1)).findConcertSeats(concertId, concertDateId);
		assertEquals(50, concertSeatList.size());
		// 1번 좌석
		assertEquals(1, concertSeatList.get(0).number());
		assertEquals(1000, concertSeatList.get(0).price());
		assertTrue(concertSeatList.get(0).isAvailable());
		// 25번 좌석
		assertEquals(25, concertSeatList.get(24).number());
		assertEquals(3000, concertSeatList.get(24).price());
		assertFalse(concertSeatList.get(24).isAvailable());
		// 50번 좌석
		assertEquals(50, concertSeatList.get(49).number());
		assertEquals(5000, concertSeatList.get(49).price());
		assertFalse(concertSeatList.get(49).isAvailable());
	}
	@Order(3)
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
	@Order(4)
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
	@Order(5)
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
		assertTrue(result.concertSeat().isAvailable());
		assertEquals("테스트 콘서트", result.concertSeat().getConcert().getName());
		assertEquals("테스트 장소", result.concertSeat().getConcertDate().getPlace());
		assertEquals(LocalDate.now(), result.concertSeat().getConcertDate().getProgressDate());
	}
	@Order(6)
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
	@Order(7)
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
	// Order(8) 일정목록조회
	@Order(9)
	@Test
	void 전체좌석_개수는_50개이다() {
		// given
		long concertId = 1L;
		long concertDateId = 1L;
		Concert concert = Concert.create("테스트 콘서트", "테스트 아티스트", LocalDate.now(), "테스트장소", 3000);
		ConcertDate concertDate = concert.getDates().get(0);
		List<ConcertSeat> concertSeats = concertDate.getSeats();

		ReflectionTestUtils.setField(concert, "id", concertId);
		ReflectionTestUtils.setField(concertDate, "id", concertDateId);

		when(concertDateRepository.findConcertDateById(concertDateId)).thenReturn(concertDate);
		when(concertSeatRepository.findConcertSeats(concertId, concertDateId)).thenReturn(ConcertInfo.GetConcertSeatList.from(concertSeats));

		// when
		long totalSeatsCount = concertService.countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId));

		// then
		assertEquals(concertSeats.size() , totalSeatsCount, "콘서트 좌석의 개수는 50석이다");
		verify(concertDateRepository, times(1)).findConcertDateById(concertDateId);
		verify(concertSeatRepository, times(1)).findConcertSeats(concertId, concertDateId);
	}
	@Order(10)
	@Test
	void 콘서트일정의_매진상태로_변경이_성공된다() {
		// given
		long concertId = 1L;
		long concertDateId = 1L;
		Concert concert = Concert.create("테스트 콘서트", "테스트 아티스트", LocalDate.now(), "테스트장소", 3000);
		ConcertDate concertDate = concert.getDates().get(0);

		ReflectionTestUtils.setField(concert, "id", concertId);
		ReflectionTestUtils.setField(concertDate, "id", concertDateId);

		when(concertDateRepository.findConcertDateById(concertDateId)).thenReturn(concertDate);
		when(concertDateRepository.save(concertDate)).thenReturn(concertDate);

		// when
		ConcertDate result = concertService.soldOut(concertDateId);

		// then
		assertFalse(result.isAvailable(), "해당 콘서트 일정에 예약할 수 없다");
		verify(concertDateRepository, times(1)).findConcertDateById(concertDateId);
		verify(concertDateRepository, times(1)).save(concertDate);
	}
}
