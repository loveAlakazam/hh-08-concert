package io.hhplus.concert.application.usecase.payment;

import static io.hhplus.concert.interfaces.api.payment.PaymentErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertRankingRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.payment.PaymentCommand;
import io.hhplus.concert.domain.payment.PaymentInfo;
import io.hhplus.concert.domain.payment.PaymentService;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationInfo;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.user.UserInfo;
import io.hhplus.concert.domain.user.UserPoint;
import io.hhplus.concert.domain.user.UserPointCommand;
import io.hhplus.concert.domain.user.UserService;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentUsecase {
	private final UserService userService;
	private final ReservationService reservationService;
	private final PaymentService paymentService;
	private final ConcertService concertService;
	private final ConcertRankingRepository concertRankingRepository;

	/**
	 * 임시예약 상태(5분간 좌석예약) 에서 결제 요청 유즈케이스<br><br>
	 *
	 * 결제완료시 예약좌석은 예약확정 상태로 변경된다.
	 *
	 * @param criteria
	 * @return PaymentResponse
	 * @throws InvalidValidationException
	 */
	@Transactional
	public PaymentResult.PayAndConfirm payAndConfirm(PaymentCriteria.PayAndConfirm criteria) {
		// 유저포인트 조회
		UserInfo.GetUserPoint userPointInfo = userService.getUserPoint(UserPointCommand.GetUserPoint.of(criteria.userId()));
		UserPoint userPoint = userPointInfo.userPoint();

		// 예약데이터 조회
		ReservationInfo.Get reservationInfo = reservationService.get(ReservationCommand.Get.of(criteria.reservationId()));
		Reservation reservation = reservationInfo.reservation();
		long concertSeatPrice = reservation.getConcertSeat().getPrice();
		long concertId = reservation.getConcert().getId();
		long concertDateId = reservation.getConcertDate().getId();

		// 임시예약상태일 경우에 결제 가능
		if(reservation.isTemporary()) {
			// 포인트 사용
			userService.usePoint(UserPointCommand.UsePoint.of(criteria.userId(), concertSeatPrice));

			// 예약 확정 변경
			reservation.confirm();

			// 결제처리 및 결제정보 반환
			PaymentInfo.CreatePayment paymentInfo = paymentService.create(PaymentCommand.CreatePayment.of(reservation));

			// 매진 확인 및 매진처리
			// 전체 좌석의 개수를 구한다
			long totalSeats = concertService.countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId));
			// 확정상태의 예약 개수를 구한다
			long confirmedSeatsCount = reservationService.countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId));

			// 전좌석이 모두 예약확정 상태라면
			if( totalSeats == confirmedSeatsCount) {
				// 전좌석 예약확정이므로 해당콘서트일정은 매진상태이므로 예약불가능한 상태로 변경한다.
				ConcertDate soldOutConcertDate = concertService.soldOut(concertDateId);
				// 매진됐다면, 매진시점에 일간 인기콘서트 에 넣는다.
				concertRankingRepository.recordDailyFamousConcertRanking(String.valueOf(concertId), soldOutConcertDate.getProgressDate().toString());
			}
			return PaymentResult.PayAndConfirm.of(paymentInfo);
		}
		throw new BusinessException(NOT_VALID_STATUS_FOR_PAYMENT);
	}
}
