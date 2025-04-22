package io.hhplus.concert.domain.payment;

import static io.hhplus.concert.interfaces.api.payment.PaymentErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.TestcontainersConfiguration;
import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import io.hhplus.concert.domain.concert.ConcertRepository;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserPointHistoryRepository;
import io.hhplus.concert.domain.user.UserPointRepository;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

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
public class PaymentServiceIntegrationTest {
	@Autowired private PaymentService paymentService;
	@Autowired private PaymentRepository paymentRepository;

	@Autowired private UserRepository userRepository;
	@Autowired private UserPointRepository userPointRepository;
	@Autowired private UserPointHistoryRepository userPointHistoryRepository;
	@Autowired private ConcertRepository concertRepository;
	@Autowired private ConcertDateRepository concertDateRepository;
	@Autowired private ConcertSeatRepository concertSeatRepository;
	@Autowired private ReservationRepository reservationRepository;

	private static final Logger log = LoggerFactory.getLogger(PaymentServiceIntegrationTest.class);

	Concert sampleConcert;
	ConcertDate sampleConcertDate;
	ConcertSeat sampleConcertSeat;
	User sampleUser;

	@BeforeEach
	void setUp() {
		// truncate -> setUp -> 테스트케이스 수행순으로 이뤄지고 있음.
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

	@Test
	void 예약데이터가_존재하지_않으면_InvalidValidationException_예외발생() {
		Reservation reservation = null;
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> paymentService.create(PaymentCommand.CreatePayment.of(reservation))
		);
		assertEquals(NOT_NULLABLE.getMessage(), exception.getMessage());
		assertEquals(NOT_NULLABLE.getHttpStatus(), exception.getHttpStatus());
	}
	@Test
	void 예약의_상태가_확정상태가_아니라면_BusinessException_예외발생() {
		// 임시예약상태
		Reservation reservation = Reservation.of(sampleUser,sampleConcert, sampleConcertDate, sampleConcertSeat);
		reservation.temporaryReserve();
		reservationRepository.saveOrUpdate(reservation);
		assertTrue(reservation.isTemporary()); // 임시예약

		// 임시예약상태에서 결제를 생성할 수 없다
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> paymentService.create(PaymentCommand.CreatePayment.of(reservation))
		);
		assertEquals(NOT_VALID_STATUS_FOR_PAYMENT.getMessage(), exception.getMessage());
		assertEquals(NOT_VALID_STATUS_FOR_PAYMENT.getHttpStatus(), exception.getHttpStatus());
	}
	@Test
	void 예약확정후_결제생성에_성공한다() {
		// 임시예약상태
		Reservation reservation = Reservation.of(sampleUser,sampleConcert, sampleConcertDate, sampleConcertSeat);
		reservation.temporaryReserve();
		assertTrue(reservation.isTemporary()); // 임시예약
		reservation.confirm();
		assertTrue(reservation.isConfirm());// 예약확정
		reservationRepository.saveOrUpdate(reservation);

		// 예약확정인데 이미 한번더 예약확정한 경우는
		PaymentInfo.CreatePayment info = assertDoesNotThrow(
			() -> paymentService.create(PaymentCommand.CreatePayment.of(reservation))
		);
		assertInstanceOf(Payment.class, info.payment());
	}

}
