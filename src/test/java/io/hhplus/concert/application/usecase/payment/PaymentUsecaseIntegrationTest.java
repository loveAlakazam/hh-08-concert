package io.hhplus.concert.application.usecase.payment;

import static io.hhplus.concert.domain.reservation.ReservationStatus.*;
import static io.hhplus.concert.interfaces.api.payment.PaymentErrorCode.*;
import static io.hhplus.concert.interfaces.api.reservation.ReservationErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.UserErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import io.hhplus.concert.domain.payment.Payment;
import io.hhplus.concert.domain.payment.PaymentRepository;
import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.payment.PaymentServiceIntegrationTest;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationInfo;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPoint;
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
	"TRUNCATE TABLE payments",
	"TRUNCATE TABLE reservations",
	"TRUNCATE TABLE concert_seats",
	"TRUNCATE TABLE concert_dates",
	"TRUNCATE TABLE concerts",
	"TRUNCATE TABLE user_point_histories",
	"TRUNCATE TABLE user_points",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaymentUsecaseIntegrationTest {
	@Autowired private PaymentUsecase paymentUsecase;
	@Autowired private PaymentService paymentService;

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
	@Autowired private PaymentRepository paymentRepository;

	private static final Logger log = LoggerFactory.getLogger(PaymentUsecaseIntegrationTest.class);

	User sampleUser;
	UserPoint sampleUserPoint;
	Concert sampleConcert;
	ConcertDate sampleConcertDate;
	ConcertSeat sampleConcertSeat;
	Reservation sampleTemporaryReservation;

	@BeforeEach
	void setUp() {
		// truncate -> setUp -> 테스트케이스 수행순으로 이뤄지고 있음.
		// 유저 테스트 데이터 셋팅
		sampleUser = User.of("최은강");
		userRepository.save(sampleUser);
		// 유저 포인트 테스트 데이터 셋팅
		sampleUserPoint = UserPoint.of(sampleUser);
		userPointRepository.save(sampleUserPoint);

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

		// 샘플 임시예약상태 예약 데이터 셋팅
		ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(
			sampleUser, sampleConcertSeat
			)
		);
		sampleTemporaryReservation = reservationRepository.saveOrUpdate(info.reservation());
	}

	@Order(1)
	@Test
	void 유저_식별자가_0이하의_음수면_InvalidValidationException_예외발생() {
		// given
		long userId = 0L;
		long reservationId = sampleTemporaryReservation.getId();

		// when & then
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId, reservationId))
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), exception.getMessage());

	}
	@Order(2)
	@Test
	void 예약_식별자가가_0이하의_음수면_InvalidValidationException_예외발생() {
		// given
		long userId = sampleUser.getId();
		long reservationId = -1L;

		// when & then
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId, reservationId))
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), exception.getMessage());

	}
	@Order(3)
	@Test
	void 유저가_존재하지_않으면_BusinessException_예외발생() {
		// given
		long userId = 999L; // 존재하지 않음
		long reservationId = 1L;
		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId, reservationId))
		);
		assertEquals(NOT_EXIST_USER.getMessage(), exception.getMessage());
	}
	@Order(4)
	@Test
	void 예약좌석이_존재하지_않으면_BusinessException_예외발생() {
		// given
		long userId = sampleUser.getId();
		long reservationId = 999L; // 존재하지 않음
		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId, reservationId))
		);
		assertEquals(NOT_FOUND_RESERVATION.getMessage(), exception.getMessage());
	}
	@Order(5)
	@Test
	void 예약상태가_임시예약상태가_아니면_예약확정처리할수_없으므로_BusinessException_예외발생() {
		// given
		long userId = sampleUser.getId();
		long reservationId = sampleTemporaryReservation.getId();
		ReservationInfo.Confirm reservationInfo = reservationService.confirm(ReservationCommand.Confirm.of(reservationId));
		assertTrue(reservationInfo.reservation().isConfirm()); // 예약확정상태인지 검증

		// when & then
		// 임시예약상태가 아니라면 결제처리 및 예약확정으로 상태변경이 불가능하다
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId, reservationId))
		);
		assertEquals(NOT_VALID_STATUS_FOR_PAYMENT.getMessage(), exception.getMessage());
	}
	@Order(6)
	@Test
	void 결제처리중_결제금액보다_잔액이_부족한경우_BusinessException_예외발생() {
		// given
		long userId = sampleUser.getId();
		long reservationId = sampleTemporaryReservation.getId();
		// 임시예약상태
		assertTrue(sampleTemporaryReservation.isTemporary());
		// 유저포인트 1000원 충전 (좌석티켓가격보다 낮은 보유포인트)
		sampleUserPoint.charge(1000);
		userPointRepository.save(sampleUserPoint);

		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId, reservationId))
		);
		assertEquals(LACK_OF_YOUR_POINT.getMessage(), exception.getMessage());
	}
	@Order(7)
	@Test
	void 결제처리를_성공하여_결제내역생성_및_예약확정으로_상태가_변경된다() {
		// given
		long userId = sampleUser.getId();
		long reservationId = sampleTemporaryReservation.getId();
		// 임시예약상태
		assertTrue(sampleTemporaryReservation.isTemporary());
		// 유저포인트 15000원 충전 (좌석티켓가격보다 많은 포인트 보유)
		sampleUserPoint.charge(15000);
		userPointRepository.save(sampleUserPoint);

		// when & then
		PaymentResult.PayAndConfirm result = assertDoesNotThrow(
			() -> paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId, reservationId))
		);
		assertNotNull(result.payment());
		Payment payment = result.payment();
		assertInstanceOf(Payment.class, payment);
		assertNotNull(payment.getReservation());
		// 예약정보 상태는 '확정상태'인지
		Reservation reservation = payment.getReservation();
		assertTrue(reservation.isConfirm());
		assertEquals(CONFIRMED, reservation.getStatus());
		// 좌석은 점유한 상태로 그대로 있는지
		assertNotNull(reservation.getConcertSeat());
		assertFalse(reservation.getConcertSeat().isAvailable());
	}
}
