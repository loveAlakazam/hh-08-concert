package io.hhplus.concert.domain.payment.service;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.concert.domain.payment.PaymentRepository;
import io.hhplus.concert.domain.payment.PaymentService;

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

	/**
	@Test
	void 예약정보가_예약확정_상태가_아니라면_결제를_생성하지않고_InvalidValidationException_예외발생() {
		// given
		LocalDateTime now = LocalDateTime.now();
		Reservation reservation = new Reservation(ReservationStatus.PENDING_PAYMENT, null, now.plusMinutes(5));

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> paymentService.confirmedPayment(reservation, 15000)
		);
		assertEquals(INVALID_RESERVATION_STATUS, ex.getMessage());
		verify(paymentRepository, never()).saveOrUpdate(any());
	}
	@Test
	void 결제정보가_존재하지_않으면_NotFoundeException_예외발생() {
		// given
		long id = 1L;

		// when & then
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> paymentService.getPaymentDetailInfo(id)
		);
		assertEquals(NOT_FOUND_PAYMENT, ex.getMessage());
	}
	@Test
	void 결제정보가_존재하면_응답을_반환하여_성공한다() {
		// given
		long id = 1L;
		PaymentResponse expected = new PaymentResponse(
			id,
			1L,
			1L,
			ReservationStatus.CONFIRMED,
			LocalDateTime.now(),
			15000,
			"항해99 TDD 재즈패스티벌를 개최합니다.",
			"테스트1",
			LocalDate.now(),
			"뚝섬 공원",
			1,
			1L
		);
		when(paymentRepository.getPaymentDetailInfo(id)).thenReturn(expected);

		// when
		PaymentResponse result = paymentService.getPaymentDetailInfo(id);
		assertEquals(result.paymentId(), expected.paymentId());
		assertEquals(result.status(), expected.status());
		assertEquals(result.concertSeatId(), expected.concertSeatId());
	}
	**/
}
