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
	void 콘서트_목록조회를_성공한다() {
		// given
		List<Concert> list = new ArrayList<>();
		list.add(Concert.of("벚꽃바람이 부는 재즈패스티벌 콘서트", "재즈 아티스트"));
		list.add(Concert.of("남산의 밤하늘과 함께하는 피아노 공연", "피아노 아티스트"));
		when(concertRepository.findAll()).thenReturn(list);

		// when
		ConcertInfo.GetConcertList info = concertService.getConcertList();
		List<Concert> result = info.concerts();

		// then
		assertEquals(2, result.size());
		assertEquals("벚꽃바람이 부는 재즈패스티벌 콘서트", result.get(0).getName());
		assertEquals("재즈 아티스트", result.get(0).getArtistName());

		assertEquals("남산의 밤하늘과 함께하는 피아노 공연", result.get(1).getName());
		assertEquals("피아노 아티스트", result.get(1).getArtistName());
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
