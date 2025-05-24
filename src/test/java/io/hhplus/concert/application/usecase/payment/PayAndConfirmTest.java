package io.hhplus.concert.application.usecase.payment;

import static io.hhplus.concert.interfaces.api.payment.PaymentErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.payment.Payment;
import io.hhplus.concert.domain.payment.PaymentCommand;
import io.hhplus.concert.domain.payment.PaymentInfo;
import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.payment.PaymentSuccessEventPublisher;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationInfo;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserInfo;
import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.domain.user.UserPointCommand;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.interfaces.api.common.BusinessException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PayAndConfirmTest {
	@InjectMocks
	private PaymentUsecase paymentUsecase;
	@Mock
	private UserService userService;
	@Mock
	private ReservationService reservationService;
	@Mock
	private PaymentService paymentService;
	@Mock
	private PaymentSuccessEventPublisher paymentSuccessEventPublisher;


	@BeforeEach
	void setUp() {
		paymentUsecase = new PaymentUsecase(userService, reservationService, paymentService, paymentSuccessEventPublisher);
	}
	private static final Logger log = LoggerFactory.getLogger(PayAndConfirmTest.class);

	@Order(1)
	@Nested
	class PayAndConfirm {
		@Test
		void 이미_예약확정된_상태에서_결제처리를_요청할경우_BusinessException_예외를_발생시킨다() {
			// given
			log.info("유저와 유저포인트 50000원 보유 목데이터 생성");
			long userId = 1L;
			User user = User.of("테스트유저");
			UserPoint userPoint = UserPoint.of(user);
			userPoint.charge(50000); // 5만원 충전
			UserPointCommand.GetUserPoint getUserPointCommand = UserPointCommand.GetUserPoint.of(userId);
			when(userService.getUserPoint(getUserPointCommand)).thenReturn(UserInfo.GetUserPoint.of(userPoint));

			log.info("콘서트 목데이터, 임시예약상태의 예약데이터 목데이터 생성");
			Concert concert = Concert.create("테스트 콘서트입니다", "테스트 아티스트", LocalDate.now(), "테스트 장소", 10000L);
			ConcertDate concertDate = concert.getDates().get(0);
			ConcertSeat concertSeat = concertDate.getSeats().get(0); // 예약좌석

			long reservationId = 1L;
			Reservation reservation = Reservation.of(user,concert,concertDate, concertSeat);
			reservation.temporaryReserve(); // 임시예약 상태
			reservation.confirm(); // 5분내로 결제를 완료했다고 가정하여 예약확정 상태
			assertTrue(reservation.isConfirm()); // 예약확정상태
			assertFalse(concertSeat.isAvailable()); // 예약할수없는 상태

			ReservationCommand.Get getReservationCommand = ReservationCommand.Get.of(reservationId);
			when(reservationService.get(getReservationCommand)).thenReturn(ReservationInfo.Get.from(reservation));

			// when
			log.info("when: 임시예약 상태가 아니므로 결제할 수 없음 - 이미 예약된 상태이므로 결제를 할 수 없음");
			BusinessException ex = assertThrows(
				BusinessException.class,
				() -> paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId, reservationId))
			);

			// then
			assertEquals(NOT_VALID_STATUS_FOR_PAYMENT.getMessage(), ex.getMessage());
			assertEquals(NOT_VALID_STATUS_FOR_PAYMENT.getHttpStatus(), ex.getHttpStatus());

			// 결제처리가 진행되지 않았으므로 유저포인트는 계산이전 그대로 보유하고 있는지 확인
			assertEquals(50000L, userPoint.getPoint());
			// 호출확인
			verify(userService,times(1)).getUserPoint(getUserPointCommand);
			verify(reservationService,times(1)).get(getReservationCommand);
			verify(paymentService,never()).create(any());
			// 이벤트 발행 확인
			verify(paymentSuccessEventPublisher, never()).publishEvent(anyLong(), anyLong(), anyLong()); // 이벤트발행 확인
		}
		@Test
		void 임시예약이_만료되어_취소된_상태에서_결제처리를_요청할경우_BusinessException_예외를_발생시킨다() {
			// given
			long userId = 1L;
			long concertId = 1L;
			long concertDateId = 1L;
			long concertSeatId = 1L;
			long reservationId = 1L;

			log.info("유저와 유저포인트 50000원 보유 목데이터 생성");
			User user = User.of("테스트유저");
			UserPoint userPoint = UserPoint.of(user);
			userPoint.charge(50000); // 5만원 충전
			ReflectionTestUtils.setField(user, "id", userId);
			UserPointCommand.GetUserPoint getUserPointCommand = UserPointCommand.GetUserPoint.of(userId);
			when(userService.getUserPoint(getUserPointCommand)).thenReturn(UserInfo.GetUserPoint.of(userPoint));

			log.info("콘서트 목데이터, 임시예약상태의 예약데이터 목데이터 생성");
			Concert concert = Concert.create("테스트 콘서트입니다", "테스트 아티스트", LocalDate.now(), "테스트 장소", 10000L);
			ConcertDate concertDate = concert.getDates().get(0);
			ConcertSeat concertSeat = concertDate.getSeats().get(0); // 예약좌석

			ReflectionTestUtils.setField(concert, "id", concertId);
			ReflectionTestUtils.setField(concertDate, "id", concertDateId);
			ReflectionTestUtils.setField(concertSeat, "id", concertSeatId);

			Reservation reservation = Reservation.of(user,concert,concertDate, concertSeat);
			// 임시예약상태
			reservation.temporaryReserve();
			// 임시예약상태를 만료시킨다.
			reservation.expireTemporaryReserve(LocalDateTime.now().minusSeconds(1));

			reservation.cancel(); // 취소처리
			ReflectionTestUtils.setField(reservation, "id", reservationId);
			ReservationCommand.Get getReservationCommand = ReservationCommand.Get.of(reservationId);
			when(reservationService.get(getReservationCommand)).thenReturn(ReservationInfo.Get.from(reservation));

			// when
			log.info("when: 임시예약상태가 아니므로 결제를 할 수 없음 - 만료일자가 지남");
			BusinessException ex = assertThrows(
				BusinessException.class,
				() -> paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId, reservationId))
			);

			// then
			assertEquals(NOT_VALID_STATUS_FOR_PAYMENT.getMessage(), ex.getMessage());
			assertEquals(NOT_VALID_STATUS_FOR_PAYMENT.getHttpStatus(), ex.getHttpStatus());

			// 결제처리가 진행되지 않았으므로 유저포인트는 계산이전 그대로 보유하고 있는지 확인
			assertEquals(50000L, userPoint.getPoint());
			// 결제처리가 진행되지 않았으므로 예약확정상태가 아니다
			assertFalse(reservation.isConfirm());
			// 좌석상태 확인 - 중간에 취소되었으므로 해당좌석
			assertTrue(reservation.getConcertSeat().isAvailable());
			// 호출확인
			verify(userService,times(1)).getUserPoint(getUserPointCommand);
			verify(reservationService,times(1)).get(getReservationCommand);
			verify(paymentService,never()).create(any());
			// 이벤트 발행 확인
			verify(paymentSuccessEventPublisher, never()).publishEvent(anyLong(), anyLong(), anyLong()); // 이벤트발행 확인
		}
		@Test
		void 임시예약상태에서_5분내로_결제를완료하면_결제처리_및_예약확정에_성공한다() {
			// given
			long userId = 1L;
			long concertId = 1L;
			long concertDateId = 1L;
			long concertSeatId = 1L;
			long reservationId = 1L;
			long paymentId = 1L;

			log.info("유저와 유저포인트 50000원 보유 목데이터 생성");
			User user = User.of("테스트유저");
			UserPoint userPoint = UserPoint.of(user);
			userPoint.charge(50000); // 5만원 충전
			ReflectionTestUtils.setField(user, "id", userId);

			UserPointCommand.GetUserPoint getUserPointCommand = UserPointCommand.GetUserPoint.of(userId);
			when(userService.getUserPoint(getUserPointCommand)).thenReturn(UserInfo.GetUserPoint.of(userPoint));

			log.info("콘서트 목데이터, 임시예약상태의 예약데이터 목데이터 생성");
			Concert concert = Concert.create("테스트 콘서트입니다", "테스트 아티스트", LocalDate.now(), "테스트 장소", 10000L);
			ConcertDate concertDate = concert.getDates().get(0);
			ConcertSeat concertSeat = concertDate.getSeats().get(0); // 예약좌석
			ReflectionTestUtils.setField(concert, "id", concertId);
			ReflectionTestUtils.setField(concertDate, "id", concertDateId);
			ReflectionTestUtils.setField(concertSeat, "id", concertSeatId);

			Reservation reservation = Reservation.of(user,concert,concertDate, concertSeat);
			reservation.temporaryReserve(); // 임시예약상태
			ReflectionTestUtils.setField(reservation, "id", reservationId);
			ReservationCommand.Get getReservationCommand = ReservationCommand.Get.of(reservationId);
			when(reservationService.get(getReservationCommand)).thenReturn(ReservationInfo.Get.from(reservation));

			log.info("결제 내역 데이터 생성");
			userPoint.use(concertSeat.getPrice());
			UserPointCommand.UsePoint usePointCommand = UserPointCommand.UsePoint.of(userId, concertSeat.getPrice());
			when(userService.usePoint(usePointCommand)).thenReturn(UserInfo.UsePoint.of(userPoint.getPoint()));

			Payment payment = Payment.of(reservation);
			ReflectionTestUtils.setField(payment, "id", paymentId);
			PaymentCommand.CreatePayment createPaymentCommand = PaymentCommand.CreatePayment.of(reservation);
			when(paymentService.create(createPaymentCommand)).thenReturn(PaymentInfo.CreatePayment.of(payment));

			// when
			log.info("when: 결제 및 예약확정 처리 유즈케이스 실행");
			PaymentResult.PayAndConfirm result = assertDoesNotThrow(() -> paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(userId, reservationId)));

			// then
			// 유저포인트 계산 확인
			assertEquals(40000L, userPoint.getPoint());
			// 예약상태 확인 - 예약확정 상태인지
			assertTrue(result.payment().getReservation().isConfirm());
			assertEquals(ReservationStatus.CONFIRMED, result.payment().getReservation().getStatus());
			assertNotNull(result.payment().getReservation().getReservedAt());
			assertNull(result.payment().getReservation().getTempReservationExpiredAt());
			// 좌석상태 확인
			assertFalse(result.payment().getReservation().getConcertSeat().isAvailable());
			// 호출확인
			verify(userService,times(1)).getUserPoint(getUserPointCommand);
			verify(reservationService,times(1)).get(getReservationCommand);
			verify(paymentService,times(1)).create(createPaymentCommand);
			// 이벤트 발행 확인
			verify(paymentSuccessEventPublisher, times(1)).publishEvent(reservationId, concertId, concertDateId); // 이벤트발행 확인
		}
	}

}
