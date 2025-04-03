package io.hhplus.concert.interfaces.api.payment.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.concert.interfaces.api.common.dto.ApiResponse;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentRequest;
import io.hhplus.concert.interfaces.api.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
public class PaymentController {
	// 임시 예약 좌석 결제
	@PostMapping()
	public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
		PaymentResponse response = PaymentResponse.builder()
			.reservationId(request.getReservationId())
			.userId(1L)
			.concertId(1L)
			.concertDate(LocalDate.of(2025,4,1))
			.seatNo(4)
			.price(15000)
			.confirmedAt(LocalDateTime.now()).build();
		return ResponseEntity.ok(ApiResponse.created(response));
	}
}
