package io.hhplus.concert.application.usecase.concert;

import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertCommand;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertRedisRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.ReservationCommand;
import io.hhplus.concert.domain.reservation.ReservationService;

@ExtendWith(MockitoExtension.class)
public class SoldOutConcertDateTest {
	@InjectMocks
	private ConcertUsecase concertUsecase;
	@Mock
	private ConcertRedisRepository concertRedisRepository;
	@Mock
	private ConcertService concertService;
	@Mock
	private ReservationService reservationService;

	@BeforeEach
	void setUp() {
		concertUsecase = new ConcertUsecase(
			concertService,
			reservationService,
			concertRedisRepository
		);
	}
	private static final Logger log = LoggerFactory.getLogger(SoldOutConcertDateTest.class);

	@Test
	void 매진이벤트가_발생했을때_일간랭킹의_sortedSet에_데이터를_추가한다(){
		// given
		Long concertId = 1L;
		Long concertDateId = 1L;
		Concert concert = Concert.create("테스트콘서트", "테스트 아티스트", LocalDate.now(), "테스트 장소명", 2000);
		ConcertDate expected = concert.getDates().get(0);
		expected.soldOut();

		when(concertService.countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId))).thenReturn(50L);
		when(reservationService.countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId))).thenReturn(50L);
		when(concertService.soldOut(concertDateId)).thenReturn(expected);

		// when
		concertUsecase.soldOutConcertDate(ConcertCriteria.SoldOutConcertDate.of(concertId, concertDateId));

		// then
		verify(concertService, times(1)).countTotalSeats(ConcertCommand.CountTotalSeats.of(concertId, concertDateId));
		verify(reservationService, times(1)).countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId));
		verify(concertRedisRepository, times(1)).recordDailyFamousConcertRanking(any(), any());
	}

}
