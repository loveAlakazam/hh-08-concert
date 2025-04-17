package io.hhplus.concert.domain.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentInfo.CreatePayment create(PaymentCommand.CreatePayment command) {
        Payment payment = paymentRepository.saveOrUpdate(Payment.of(command.reservation()));
        return PaymentInfo.CreatePayment.of(payment);
    }
}
