package io.hhplus.concert.interfaces.api.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.application.usecase.payment.PaymentCriteria;
import io.hhplus.concert.application.usecase.payment.PaymentResult;
import io.hhplus.concert.application.usecase.payment.PaymentUsecase;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.interfaces.api.common.ApiResponse;
import io.hhplus.concert.interfaces.api.common.ApiResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentApiDocs {
	private final PaymentUsecase paymentUsecase;
	@PostMapping()
	public ResponseEntity<ApiResponse<PaymentResponse.Execute>> execute(
		@RequestHeader("token") @Valid String token,
		@RequestBody PaymentRequest.Execute request
	) {
		PaymentResult.PayAndConfirm result = paymentUsecase.payAndConfirm(PaymentCriteria.PayAndConfirm.of(request.userId(), request.reservationId()));
		return ApiResponseEntity.created(PaymentResponse.Execute.from(result));
	}
}
