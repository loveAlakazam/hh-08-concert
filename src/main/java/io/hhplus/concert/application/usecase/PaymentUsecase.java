package io.hhplus.concert.application.usecase;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.common.exceptions.RequestTimeOutException;
import io.hhplus.concert.domain.common.exceptions.UnProcessableContentException;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.payment.Payment;
import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentUsecase {
	private final UserService userService;
	private final ReservationService reservationService;
	private final PaymentService paymentService;

	/**
	 * 임시예약 상태(5분간 좌석예약) 에서 결제 요청 유즈케이스<br><br>
	 *
	 * 결제완료시 예약좌석은 예약확정 상태로 변경된다.
	 *
	 * @param userId - 유저 PK
	 * @param reservationId - 예약 PK
	 * @return PaymentResponse
	 * @throws InvalidValidationException
	 * @throws RequestTimeOutException
	 * @throws NotFoundException
	 * @throws UnProcessableContentException
	 */
	public PaymentResponse applyPayment(long userId, long reservationId) {
		// 임시예약 상태인지 확인
		Reservation reservation = reservationService.checkTemporaryReservedStatus(reservationId);
		ConcertSeat concertSeat = reservation.getConcertSeat();
		long price = concertSeat.getPrice();

		// 포인트 결제
		userService.usePoint(userId, price);

		// 예약 상태를 확정상태로 변경
		reservation.updateConfirmedStatus();

		// 결제내역 생성후 정보 반환
		Payment payment = paymentService.confirmedPayment(reservation, price);
		return paymentService.getPaymentDetailInfo(payment.getId());
	}
}
