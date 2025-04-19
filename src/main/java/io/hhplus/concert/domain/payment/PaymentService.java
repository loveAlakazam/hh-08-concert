package io.hhplus.concert.domain.payment;

import static io.hhplus.concert.interfaces.api.payment.PaymentErrorCode.*;

import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentInfo.CreatePayment create(PaymentCommand.CreatePayment command) {
        // 예약의 상태가 이미 결제처리까지 완료해서 확정(CONFIRMED)상태인지 확인
        Reservation reservation = command.reservation();
        if(reservation.getStatus() != ReservationStatus.CONFIRMED)
            throw new BusinessException(NOT_VALID_STATUS_FOR_PAYMENT);

        Payment payment = paymentRepository.saveOrUpdate(Payment.of(command.reservation()));
        return PaymentInfo.CreatePayment.of(payment);
    }
}
