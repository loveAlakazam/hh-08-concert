package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.concert.Concert.*;
import static io.hhplus.concert.domain.concert.ConcertService.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.domain.payment.PaymentRepository;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.support.CacheCleaner;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.domain.user.UserPointHistoryRepository;
import io.hhplus.concert.domain.user.UserPointRepository;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.infrastructure.containers.RedisTestContainerConfiguration;
import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import jakarta.persistence.EntityManager;

@ActiveProfiles("test")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = {
	TestcontainersConfiguration.class,
	RedisTestContainerConfiguration.class
})
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE concert_seats",
	"TRUNCATE TABLE concert_dates",
	"TRUNCATE TABLE concerts",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ConcertServiceIntegrationTest {
	@Autowired private EntityManager em;
	@Autowired private ConcertService concertService;
	@Autowired private ConcertRepository concertRepository;
	@Autowired private ConcertDateRepository concertDateRepository;
	@Autowired private ConcertSeatRepository concertSeatRepository;


	private static final Logger log = LoggerFactory.getLogger(ConcertServiceIntegrationTest.class);

	Concert sampleConcert;
	ConcertDate sampleConcertDate;
	ConcertSeat sampleConcertSeat;
	@BeforeEach
	void setUp() {
		// truncate -> setUp -> 테스트케이스 수행 순으로 이뤄지고 있음.
		sampleConcert = concertRepository.saveOrUpdate(Concert.create(
			"TDD와 함께하는 테스트 콘서트",
			"아티스트",
			LocalDate.now(),
			"공연 장소",
			10000
		));
		sampleConcertDate = sampleConcert.getDates().get(0);
		sampleConcertSeat = sampleConcertDate.getSeats().get(0);
	}

	@Test
	@Order(1)
	void 콘서트_목록조회에_성공한다(){
		// when
		ConcertInfo.GetConcertList concertInfo = assertDoesNotThrow(
			() -> concertService.getConcertList()
		);
		List<ConcertInfo.GetConcertListDto> concerts = concertInfo.concerts();
		assertEquals(1, concertInfo.size());

		// then
		ConcertInfo.GetConcertListDto concert = concerts.get(0);
		assertEquals(sampleConcert.getName(), concert.name());
		assertEquals(sampleConcert.getArtistName(), concert.artistName());
		assertEquals(1, concert.id());
	}
	@Order(2)
	@Test
	void 콘서트_일정목록_조회를_성공한다() {
		// given
		long concertId = sampleConcert.getId();

		// when
		// 콘서트아이디에 해당하는 공연의 공연일정목록 조회
		ConcertInfo.GetConcertDateList info = assertDoesNotThrow(
			()-> concertService.getConcertDateList(ConcertCommand.GetConcertDateList.of(concertId))
		);

		// then
		assertEquals(1, info.size());

		List<ConcertInfo.GetConcertDateListDto> concertDates = info.concertDates();
		ConcertInfo.GetConcertDateListDto concertDate = concertDates.get(0);
		assertEquals(sampleConcertDate.getId(), concertDate.id());
		assertEquals(sampleConcertDate.getPlace(), concertDate.place());
		assertEquals(sampleConcertDate.getProgressDate(), concertDate.progressDate());

		// 예약가능한 좌석개수 확인
		assertEquals(50, concertDate.concertSeats().size());
	}
	@Order(3)
	@Test
	void 콘서트_일정목록조회_요청시_concertId가_0이하의_음수이면_InvalidValidationException_예외발생() {
		// given
		long concertId = -1L;

		// when
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> concertService.getConcertDateList(ConcertCommand.GetConcertDateList.of(concertId))
		);

		// then
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), ex.getMessage());
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getHttpStatus(), ex.getHttpStatus());
	}
	@Order(4)
	@Test
	void 콘서트_공연일정의_좌석목록조회를_성공한다() {
		// given
		long concertId = sampleConcert.getId();
		long concertDateId = sampleConcertDate.getId();

		// when
		ConcertInfo.GetConcertSeatList info = assertDoesNotThrow(
			() -> concertService.getConcertSeatList(ConcertCommand.GetConcertSeatList.of(concertId, concertDateId))
		);

		// then
		List<ConcertInfo.ConcertSeatListDto> concertSeats = info.concertSeatList();
		assertEquals(50, concertSeats.size()); // 공연좌석 개수는 최대 50개이다.
	}
	@Order(5)
	@Test
	void 좌석목록조회_요청시_concertId가_0이하의_음수이면_InvalidValidationException_예외발생() {
		// given
		long concertId = 0L;
		long concertDateId = sampleConcertDate.getId();

		// when
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> concertService.getConcertSeatList(ConcertCommand.GetConcertSeatList.of(concertId, concertDateId))
		);

		// then
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), ex.getMessage());
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getHttpStatus(), ex.getHttpStatus());
	}
	@Order(6)
	@Test
	void 좌석목록조회_요청시_concertDateId가_0이하의_음수이면_InvalidValidationException_예외발생() {
		// given
		long concertId = sampleConcert.getId();
		long concertDateId = -1L;

		// when
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> concertService.getConcertSeatList(ConcertCommand.GetConcertSeatList.of(concertId, concertDateId))
		);

		// then
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), ex.getMessage());
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getHttpStatus(), ex.getHttpStatus());
	}
	@Order(7)
	@Test
	void 콘서트좌석정보_조회하면_좌석에대한_정보를_조회할_수있다() {
		// given
		long concertSeatId = sampleConcertSeat.getId();
		log.info("concertSeatId: "+ concertSeatId);

		// when
		ConcertInfo.GetConcertSeat info = assertDoesNotThrow(
			() -> concertService.getConcertSeat(ConcertCommand.GetConcertSeat.of(concertSeatId))
		);
		ConcertSeat concertSeat = info.concertSeat();
		// then
		assertTrue(concertSeat.isAvailable()); // 예약가능상태
		assertEquals(1L, concertSeat.getId());
		assertEquals(sampleConcertSeat.getNumber(), concertSeat.getNumber());
		assertEquals(sampleConcertSeat.getPrice(), concertSeat.getPrice());
		assertTrue(concertSeat.isAvailable());
	}
	@Test
	@Order(8)
	void 콘서트좌석_목록조회에_성공한다(){
		// given
		long concertId = sampleConcert.getId();
		long concertDateId = sampleConcertDate.getId();

		ConcertInfo.GetConcertSeatList expected = concertSeatRepository.findConcertSeats(concertId, concertDateId);

		// when
		ConcertInfo.GetConcertSeatList result = concertService.getConcertSeatList(
			ConcertCommand.GetConcertSeatList.of(concertId, concertDateId)
		);

		// then
		assertThat(result).isEqualTo(expected); // 응답확인
	}
	@Test
	@Order(9)
	void 콘서트_전체_좌석개수를_얻는데_성공한다() {
		// given
		long concertId = sampleConcert.getId();
		long concertDateId = sampleConcertDate.getId();

		// when
		long result = concertService.countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId));

		// then
		assertEquals(sampleConcertDate.getSeats().size(), result);
	}
	@Test
	@Order(10)
	void 콘서트일정_상태를_매진상태로_변경할_수있다(){
		// given
		long concertDateId = sampleConcertDate.getId();

		// when
		ConcertDate result = concertService.soldOut(concertDateId);
		// then
		assertFalse(result.isAvailable());
	}


}
