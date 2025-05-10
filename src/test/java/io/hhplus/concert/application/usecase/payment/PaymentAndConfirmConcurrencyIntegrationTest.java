package io.hhplus.concert.application.usecase.payment;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;
import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import io.hhplus.concert.domain.concert.ConcertRepository;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.payment.PaymentRepository;
import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationInfo;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserInfo;
import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.domain.user.UserPointCommand;
import io.hhplus.concert.domain.user.UserPointRepository;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.domain.user.UserService;

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
public class PaymentAndConfirmConcurrencyIntegrationTest {
	@Autowired private PaymentUsecase paymentUsecase;
	@Autowired private PaymentService paymentService;
	@Autowired private ReservationService reservationService;
	@Autowired private UserService userService;

	@Autowired private PaymentRepository paymentRepository;
	@Autowired private ReservationRepository reservationRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private UserPointRepository userPointRepository;

	@Autowired private ConcertRepository concertRepository;
	@Autowired private ConcertDateRepository concertDateRepository;
	@Autowired private ConcertSeatRepository concertSeatRepository;
	private static final Logger log = LoggerFactory.getLogger(PaymentAndConfirmConcurrencyIntegrationTest.class);


	/**
	 * [동시성테스트] 동일한 회원이 여러개의 예약좌석 확정 및 결제를 동시에 진행한다.
	 * (일정A, 좌석1번, 15000원), (일정B, 좌석11번, 10000원) => 둘다 임시예약상태(임시5분동안 좌석은 점유됨)
	 * 여기서 공유자원은 무엇일까? => 회원의 포인트
	 * - 좌석은 여러가지이고 상태값외로는 데이터변경자체가 없는편이다. 각 좌석은 낙관적락을 사용하고있고
	 * - 포인트 충전/사용의 경우에는 비관적락(x-lock)을 사용함. 왜냐면 포인트는 의도적인 변경에 민감하며, 돈과 직결되므로 데이터의 정합성이 우선시함.
	 */
	User sampleUser;
	UserPoint sampleUserPoint;

	Concert sampleConcert1;
	ConcertDate sampleConcertDate1;
	ConcertSeat sampleConcertSeat1;

	Concert sampleConcert2;
	ConcertDate sampleConcertDate2;
	ConcertSeat sampleConcertSeat2;

	@BeforeEach
	void setUp() {
		// truncate -> setUp -> 테스트케이스 수행순으로 이뤄지고 있음.
		// 유저 & 유저포인트 테스트 데이터 셋팅
		sampleUser = User.of("최은강");
		userRepository.save(sampleUser);
		userPointRepository.save(UserPoint.of(sampleUser)); // 초기포인트 0 포인트

		// 콘서트 테스트 데이터 셋팅
		// 콘서트 1
		sampleConcert1 = concertRepository.saveOrUpdate(Concert.create(
			"테스트를 우선시하는 TDD 콘서트",
			"TDD",
			LocalDate.now(),
			"서울시 성동구 연무장길",
			10000
		));
		sampleConcertDate1 = sampleConcert1.getDates().get(0);
		sampleConcertSeat1 = sampleConcertDate1.getSeats().get(0);

		// 콘서트 2
		sampleConcert2 = concertRepository.saveOrUpdate(Concert.create(
			"통합테스트와 함께하는 TDD 콘서트",
			"통합테스트",
			LocalDate.now().plusWeeks(2),
			"서울시 성동구 왕십리 광장",
			5000
		));
		sampleConcertDate2 = sampleConcert2.getDates().get(0);
		sampleConcertSeat2 = sampleConcertDate2.getSeats().get(0);
	}
	@Test
	@Order(1)
	@Sql(statements = {
		"SET SESSION innodb_lock_wait_timeout=10"
	}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(statements = {
		"SET SESSION innodb_lock_wait_timeout=50"
	}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	void 회원이_동시에_여러개의_임시예약을_결제한다() throws ExecutionException, InterruptedException {
		// given
		long userId = sampleUser.getId();
		// 먼저 2만원 충전
		userService.chargePoint(UserPointCommand.ChargePoint.of(userId, 20_000L));

		// 2개의 예약 셋팅
		// 예약 1
		ReservationInfo.TemporaryReserve temporaryReserveInfo1 = reservationService.temporaryReserve(ReservationCommand.TemporaryReserve.of(
			sampleUser, sampleConcertSeat1
		));
		Reservation reservation1 = temporaryReserveInfo1.reservation();
		long reservationId1 = reservation1.getId();
		PaymentCriteria.PayAndConfirm paymentCriteria1 = PaymentCriteria.PayAndConfirm.of(userId, reservationId1);

		// 예약 2
		ReservationInfo.TemporaryReserve temporaryReserveInfo2 = reservationService.temporaryReserve(ReservationCommand.TemporaryReserve.of(
			sampleUser, sampleConcertSeat2
		));
		Reservation reservation2 = temporaryReserveInfo2.reservation();
		long reservationId2 = reservation2.getId();
		PaymentCriteria.PayAndConfirm paymentCriteria2 = PaymentCriteria.PayAndConfirm.of(userId, reservationId2);

		// when
		// 결제 동시성테스트
		CompletableFuture<PaymentResult.PayAndConfirm> future1 = CompletableFuture.supplyAsync(() -> paymentUsecase.payAndConfirm(paymentCriteria1));
		CompletableFuture<PaymentResult.PayAndConfirm> future2 = CompletableFuture.supplyAsync(() -> paymentUsecase.payAndConfirm(paymentCriteria2));
		CompletableFuture.allOf(future1, future2).join(); // 둘다 완료할 때까지 대기

		PaymentResult.PayAndConfirm result1 = future1.get();
		PaymentResult.PayAndConfirm result2 = future2.get();

		// then
		assertThat(result1.payment().getReservation()).isNotNull();
		assertThat(result2.payment().getReservation()).isNotNull();

		// 두개의 예약이모두 확정상태인지 확인
		assertThat(result1.payment().getReservation().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
		assertThat(result2.payment().getReservation().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

		// 포인트가 정상차감됐는지 확인: 20,000원 - (10,000원 + 5,000원) = 5,000원
		UserInfo.GetUserPoint userPointInfo = userService.getUserPoint(UserPointCommand.GetUserPoint.of(userId));
		assertThat(userPointInfo.userPoint().getPoint()).isEqualTo(5_000L);
	}
}
