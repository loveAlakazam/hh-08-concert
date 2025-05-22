package io.hhplus.concert.application.usecase.payment;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import io.hhplus.concert.domain.concert.ConcertRepository;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.payment.PaymentRepository;
import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.payment.PaymentSuccessEvent;
import io.hhplus.concert.domain.payment.PaymentSuccessEventPublisher;
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
import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;
import io.hhplus.concert.interfaces.events.PaymentSuccessEventListener;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@RecordApplicationEvents
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

	@MockitoSpyBean
	private PaymentSuccessEventPublisher paymentSuccessEventPublisher;
	@MockitoSpyBean
	private PaymentSuccessEventListener paymentSuccessEventListener;

	private static final Logger log = LoggerFactory.getLogger(PaymentAndConfirmConcurrencyIntegrationTest.class);

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
	void 회원이_동시에_여러개의_임시예약을_결제한다(ApplicationEvents events) throws ExecutionException, InterruptedException {
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

		// 이벤트 발행 검증: 두개의 결제트랜잭션이 성공후에 각각 결제성공이벤트가 발행됐는지 확인
		verify(paymentSuccessEventPublisher, times(1)).publishEvent(reservationId1, sampleConcert1.getId(), sampleConcertDate1.getId());
		verify(paymentSuccessEventPublisher, times(1)).publishEvent(reservationId2, sampleConcert2.getId(), sampleConcertDate2.getId());

		// 이벤트 수신후 이벤트 핸들러 handleSoldOutConcertDate 가 수행했는지 확인
		verify(paymentSuccessEventListener, times(2)).handleSoldOutConcertDate(any(PaymentSuccessEvent.class));
	}
}
