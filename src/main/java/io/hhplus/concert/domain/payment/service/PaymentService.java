package io.hhplus.concert.domain.payment.service;

import static io.hhplus.concert.domain.payment.exceptions.messages.PaymentExceptionMessage.*;

import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.domain.payment.entity.Payment;
import io.hhplus.concert.domain.payment.repository.PaymentRepository;
import io.hhplus.concert.domain.reservation.entity.Reservation;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    /**
     * 결제 엔티티 생성 및 데이터베이스에 저장
     *
     * @param reservation - 예약 엔티티
     * @param price - 예약좌석 가격
     * @return Payment
     */
    public Payment confirmedPayment(Reservation reservation, long price) {
        // 예약상태가 확정상태인지 확인
        Reservation.validateConfirmedStatus(reservation);

        // 예약상태를 저장
        Payment payment = new Payment(price);
        payment.setReservation(reservation);

        // 데이터베이스에 결제내역 저장후 반환
        return paymentRepository.saveOrUpdate(payment);
    }

    /**
     * 결제내역 상세정보 조회
     *
     * @param paymentId - 결제 PK
     * @return PaymentResponse
     */
    public PaymentResponse getPaymentDetailInfo(long paymentId) {
        PaymentResponse paymentDetailInfo = paymentRepository.getPaymentDetailInfo(paymentId);
        if(paymentDetailInfo == null) throw new NotFoundException(NOT_FOUND_PAYMENT);
        return paymentDetailInfo;
    }

}
