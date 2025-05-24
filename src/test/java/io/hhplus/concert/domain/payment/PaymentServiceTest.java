package io.hhplus.concert.domain.payment;

import static io.hhplus.concert.interfaces.api.payment.PaymentErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.BusinessException;


@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
	@InjectMocks
	private PaymentService paymentService;
	@Mock
	private PaymentRepository paymentRepository;

	@BeforeEach
	void setUp() {
		paymentService = new PaymentService(paymentRepository);
	}


	@Test
	void 예약정보가_예약확정_상태가_아니라면_결제를_생성하지않고_BusinessException_예외발생() {
		// given
		User user = User.of("최은강");
		Concert concert = Concert.create(
			"TDD와 유닛테스트와 함께하는 재즈패스티벌",
			"테스트아티스트",
			LocalDate.now(),
			"서울시 성동구 무수막길",
			2000
		);
		ConcertDate concertDate  = concert.getDates().get(0);
		ConcertSeat concertSeat  = concertDate.getSeats().get(0);
		// 이 예약은 확정상태가 아닌 임시예약 상태임
		Reservation reservation = Reservation.of(user,concert,concertDate,concertSeat);
		reservation.temporaryReserve(); // 임시예약상태

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> paymentService.create(PaymentCommand.CreatePayment.of(reservation))
		);
		assertEquals(NOT_VALID_STATUS_FOR_PAYMENT.getMessage(), ex.getMessage());
		verify(paymentRepository, never()).saveOrUpdate(any());
	}
	@Test
	void 예약확정처리되어_결제내역생성에_성공한다() {
		// given
		User user = User.of("최은강");
		Concert concert = Concert.create(
			"TDD와 유닛테스트와 함께하는 재즈패스티벌",
			"테스트아티스트",
			LocalDate.now(),
			"서울시 성동구 무수막길",
			2000
		);
		ConcertDate concertDate  = concert.getDates().get(0);
		ConcertSeat concertSeat  = concertDate.getSeats().get(0);
		// 이 예약은 확정상태가 아닌 임시예약 상태임
		Reservation reservation = Reservation.of(user,concert,concertDate,concertSeat);
		reservation.temporaryReserve(); // 임시예약상태
		reservation.confirm(); // 예약확정

		// when & then
		PaymentInfo.CreatePayment info = assertDoesNotThrow(
			() -> paymentService.create(PaymentCommand.CreatePayment.of(reservation))
		);
		verify(paymentRepository, times(1)).saveOrUpdate(any());
	}

}
