package io.hhplus.concert.interfaces.events;

import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import io.hhplus.concert.domain.concert.ConcertRankingRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.payment.PaymentSuccessEvent;
import io.hhplus.concert.domain.payment.SoldOutConcertDateFailEventPublisher;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.interfaces.api.common.BusinessException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaymentSuccessEventListenerTest {
	@InjectMocks
	private PaymentSuccessEventListener paymentSuccessEventListener;
	@Mock
	private ConcertService concertService;
	@Mock
	private ReservationService reservationService;
	@Mock
	private ConcertRankingRepository concertRankingRepository;
	@Mock
	private SoldOutConcertDateFailEventPublisher soldOutConcertDateFailEventPublisher;
	@Mock
	private ConcertDateRepository concertDateRepository;


	private static final Logger log = LoggerFactory.getLogger(PaymentSuccessEventListenerTest.class);
	@BeforeEach
	void setUp() {
		paymentSuccessEventListener = new PaymentSuccessEventListener(
			concertService,
			reservationService,
			concertRankingRepository,
			soldOutConcertDateFailEventPublisher
		);
	}

	@Order(1)
	@Nested
	class PaymentSuccessEventOccur {
		@Test
		void 예약좌석_결제_성공후_모든좌석이_예약확정_상태라면_매진처리_및_랭킹기록이_정상동작된다() {
			// given
			long reservationId = 1L;
			long concertId = 1L;
			long concertDateId = 1L;
			long totalSeats = 50L;
			long confirmedSeats = 50L;

			// 이벤트 객체 생성
			PaymentSuccessEvent event = new PaymentSuccessEvent(reservationId, concertId, concertDateId);

			LocalDate progressDate = LocalDate.of(2025,5,21);
			ConcertDate soldOutConcertDate = mock(ConcertDate.class);
			when(soldOutConcertDate.getProgressDate()).thenReturn(progressDate);

			when(concertService.countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId))).thenReturn(totalSeats);
			when(reservationService.countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId))).thenReturn(confirmedSeats);
			when(concertService.soldOut(concertDateId)).thenReturn(soldOutConcertDate);

			// when
			paymentSuccessEventListener.handleSoldOutConcertDate(event);

			// then
			verify(concertService, times(1)).countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId));
			verify(reservationService, times(1)).countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId));
			verify(concertService, times(1)).soldOut(concertDateId);
			verify(concertRankingRepository, times(1)).recordDailyFamousConcertRanking(String.valueOf(concertDateId), progressDate.toString());
		}
		@Test
		void 결제성공후_모든좌석이_예약확정되지_않을경우에는_매진처리_및_랭킹기록을_호출하지않는다(){
			// given
			long reservationId = 1L;
			long concertId = 1L;
			long concertDateId = 1L;
			long totalSeats = 50L;
			long confirmedSeats = 30L; // 일부좌석만 예약확정

			// 이벤트 객체 생성
			PaymentSuccessEvent event = new PaymentSuccessEvent(reservationId, concertId, concertDateId);

			when(concertService.countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId))).thenReturn(totalSeats);
			when(reservationService.countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId))).thenReturn(confirmedSeats);

			// when
			paymentSuccessEventListener.handleSoldOutConcertDate(event);

			// then
			verify(concertService, times(1)).countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId));
			verify(reservationService, times(1)).countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId));

			verify(concertService, never()).soldOut(concertDateId);
			verify(concertRankingRepository, never()).recordDailyFamousConcertRanking(anyString(), anyString());
		}
		@Test
		void 좌석결제후_전체좌석수_조회에서_BusinessException_발생시_에러리포팅한다(){
			// given
			long reservationId = 1L;
			long concertId = 1L;
			long concertDateId = 1L;

			PaymentSuccessEvent event = new PaymentSuccessEvent(reservationId, concertId, concertDateId);
			BusinessException ex = new BusinessException(CONCERT_DATE_NOT_FOUND);
			when(concertService.countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId))).thenThrow(ex);

			// when
			paymentSuccessEventListener.handleSoldOutConcertDate(event);

			// then
			verify(concertService, times(1)).countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId));

			verify(reservationService, never()).countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId));
			verify(concertService, never()).soldOut(concertDateId);
			verify(concertRankingRepository, never()).recordDailyFamousConcertRanking(anyString(), anyString());

			// 실패이벤트 호출여부
			verify(soldOutConcertDateFailEventPublisher, times(1)).publishEvent(concertId, concertDateId, ex.getMessage(), ex);
		}
		@Test
		void 좌석결제후_매진처리에서_BusinessException_발생시_에러리포팅을한다(){
			// given
			long reservationId = 1L;
			long concertId = 1L;
			long concertDateId = 1L;
			long totalSeats = 50L;
			long confirmedSeats = 50L;

			PaymentSuccessEvent event = new PaymentSuccessEvent(reservationId, concertId, concertDateId);
			when(concertService.countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId))).thenReturn(totalSeats);
			when(reservationService.countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId))).thenReturn(confirmedSeats);

			BusinessException ex = new BusinessException(CONCERT_DATE_NOT_FOUND);
			when(concertService.soldOut(concertDateId)).thenThrow(ex);

			// when
			paymentSuccessEventListener.handleSoldOutConcertDate(event);

			// then
			verify(concertService, times(1)).countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId));
			verify(reservationService, times(1)).countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId));
			verify(concertService, times(1)).soldOut(concertDateId);

			verify(concertRankingRepository, never()).recordDailyFamousConcertRanking(anyString(), anyString());

			// 실패이벤트 호출여부
			verify(soldOutConcertDateFailEventPublisher, times(1)).publishEvent(concertId, concertDateId, ex.getMessage(), ex);
		}
	}
}
