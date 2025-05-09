package io.hhplus.concert.application.usecase.reservation;

import static io.hhplus.concert.domain.reservation.ReservationStatus.*;
import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.UserErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.TestcontainersConfiguration;
import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import io.hhplus.concert.domain.concert.ConcertRepository;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.reservation.ReservationService;

import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPointHistoryRepository;
import io.hhplus.concert.domain.user.UserPointRepository;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE reservations",
	"TRUNCATE TABLE concert_seats",
	"TRUNCATE TABLE concert_dates",
	"TRUNCATE TABLE concerts",
	"TRUNCATE TABLE user_points",
	"TRUNCATE TABLE user_point_histories",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationUsecaseIntegrationTest {
	@Autowired private ReservationUsecase reservationUsecase;
	@Autowired private UserService userService;
	@Autowired private ConcertService concertService;
	@Autowired private ReservationService reservationService;

	@Autowired private UserRepository userRepository;
	@Autowired private UserPointRepository userPointRepository;
	@Autowired private UserPointHistoryRepository userPointHistoryRepository;

	@Autowired private ConcertRepository concertRepository;
	@Autowired private ConcertDateRepository concertDateRepository;
	@Autowired private ConcertSeatRepository concertSeatRepository;
	@Autowired private ReservationRepository reservationRepository;


	Concert sampleConcert;
	ConcertDate sampleConcertDate;
	ConcertSeat sampleConcertSeat;
	User sampleUser;
	@BeforeEach
	void setUp() {
		// truncate -> setUp -> 테스트케이스 수행 순으로 진행
		// 유저 테스트 데이터 셋팅
		sampleUser = User.of("최은강");
		userRepository.save(sampleUser);

		// 콘서트 테스트 데이터 셋팅
		sampleConcert = Concert.create(
			"테스트를 우선시하는 TDD 콘서트",
			"TDD",
			LocalDate.now(),
			"서울시 성동구 연무장길",
			10000
		);
		concertRepository.saveOrUpdate(sampleConcert);

		sampleConcertDate = sampleConcert.getDates().get(0);
		sampleConcertSeat = sampleConcertDate.getSeats().get(0);
	}

	/**
	 * reserveConcertSeat - 좌석예약 유즈케이스
	 */
	@Test
	void userId가_0이하의_음수이면_InvalidValidationException_예외발생() {
		long userId = -1L; // 음수
		long concertSeatId = 1L;
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> reservationUsecase.reserveConcertSeat(
				ReservationCriteria.ReserveConcertSeat.of(userId, concertSeatId)
			)
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), exception.getMessage());
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getHttpStatus(), exception.getHttpStatus());
	}
	@Test
	void concertSeatId가_0이하의_음수이면_InvalidValidationException_예외발생() {
		long userId = 1L;
		long concertSeatId = 0L; // 양수가 아닌 0
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> reservationUsecase.reserveConcertSeat(
				ReservationCriteria.ReserveConcertSeat.of(userId, concertSeatId)
			)
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), exception.getMessage());
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getHttpStatus(), exception.getHttpStatus());
	}
	@Test
	void 요청유저가_존재하지_않으면_BusinessException_예외발생() {
		long userId = 999L; // 존재하지않음
		long concertSeatId = sampleConcertSeat.getId();;
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> reservationUsecase.reserveConcertSeat(
				ReservationCriteria.ReserveConcertSeat.of(userId, concertSeatId)
			)
		);
		assertEquals(NOT_EXIST_USER.getMessage(), exception.getMessage());
		assertEquals(NOT_EXIST_USER.getHttpStatus(), exception.getHttpStatus());
	}
	@Test
	void 콘서트좌석이_존재하지_않으면_BusinessException_예외발생() {
		long userId = sampleUser.getId();
		long concertSeatId = 999L; // 존재하지 않음
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> reservationUsecase.reserveConcertSeat(
				ReservationCriteria.ReserveConcertSeat.of(userId, concertSeatId)
			)
		);
		assertEquals(CONCERT_SEAT_NOT_FOUND.getMessage(), exception.getMessage());
		assertEquals(CONCERT_SEAT_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());
	}
	@Test
	void 이미_예약된_좌석을_예약요청하면_BusinessException_예외발생() {
		long userId = sampleUser.getId();
		long concertSeatId = sampleConcertSeat.getId();
		// 해당좌석을 이미 예약한 경우
		ReservationResult.ReserveConcertSeat result = ReservationResult.ReserveConcertSeat.from(
			reservationService.temporaryReserve(
				ReservationCommand.TemporaryReserve.of(sampleUser, sampleConcertSeat)
			)
		);
		assertEquals(PENDING_PAYMENT, result.reservation().getStatus());
		assertTrue(result.reservation().isTemporary());
		assertFalse(result.reservation().getConcertSeat().isAvailable());

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> reservationUsecase.reserveConcertSeat(
				ReservationCriteria.ReserveConcertSeat.of(userId, concertSeatId)
			)
		);
		assertEquals(ALREADY_RESERVED_SEAT.getMessage(), exception.getMessage());
		assertEquals(ALREADY_RESERVED_SEAT.getHttpStatus(), exception.getHttpStatus());
	}
	@Test
	void 좌석예약에_성공하며_5분동안_해당좌석이_임시로_점유된다() {
		long userId = sampleUser.getId();
		long concertSeatId = sampleConcertSeat.getId();

		ReservationResult.ReserveConcertSeat result = assertDoesNotThrow(
			() -> reservationUsecase.reserveConcertSeat(
				ReservationCriteria.ReserveConcertSeat.of(userId, concertSeatId)
			)
		);
		// 임시예약상태
		assertEquals(PENDING_PAYMENT, result.reservation().getStatus());
		assertTrue(result.reservation().isTemporary());
		// 좌석의 예약상태: 이미 점유되어 예약불가능 상태
		assertFalse(result.reservation().getConcertSeat().isAvailable());
	}
}
