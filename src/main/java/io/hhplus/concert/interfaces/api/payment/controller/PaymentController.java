package io.hhplus.concert.interfaces.api.payment.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.domain.reservation.entity.ReservationStatus;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.common.dto.ApiResponseEntity;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentRequest;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentApiDocs {
	// 임시 예약 좌석 결제
	@PostMapping()
	public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
		PaymentResponse response = PaymentResponse.builder()
			.paymentId(1L)
			.reservationId(request.reservationId())
			.concertSeatId(1L)
			.status(ReservationStatus.CONFIRMED)
			.confirmedAt(LocalDateTime.now())
			.price(15000)
			.concertName("재즈캣의 테스트 콘서트 입니다")
			.artistName("재즈캣")
			.concertDate(LocalDate.of(2025,4,1))
			.concertLocation("한강 뚝섬 공원")
			.concertSeatNumber(5)
			.userId(1L)
			.build();
		return ApiResponseEntity.created(response);
	}
}
