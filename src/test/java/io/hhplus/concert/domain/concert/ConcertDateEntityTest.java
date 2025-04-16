package io.hhplus.concert.domain.concert;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;


public class ConcertDateEntityTest {
	@Test
	void 예약가능한좌석_50개중_10개_매진한경우_해당콘서트의_예약가능한_좌석개수는_40개이다() {
		// given
		LocalDate now = LocalDate.now();
		LocalDate progressDate = now.plusWeeks(1);
		long price = 1500L;

		// 기존에 1개 일정 추가
		Concert concert = Concert.create(
			"TDD 테스트 콘서트", "테스트 아티스트", progressDate, "테스트 장소", price
		);
		ConcertDate concertDate = concert.getDates().get(0);
		List<ConcertSeat> concertSeats = concertDate.concertSeats();
		assertEquals(1, concert.getDates().size());
		assertEquals(50, concertSeats.size());

		for(int i= 0; i <10; i++ ) {
			// 일부 좌석 10개만 예약되어있음
			concertSeats.get( 10+i ).reserve();
		}

		// when
		int result = concertDate.countAvailableSeats();

		// then
		assertEquals(40, result);
		assertEquals(true, concertDate.isAvailable()); // 좌석 예약 가능
	}
	@Test
	void 예약가능한좌석없이_전좌석_매진이면_해당_공연일정의_예약가능여부는_비활성화되어있다() {
		// given
		LocalDate now = LocalDate.now();
		LocalDate progressDate = now.plusWeeks(1);
		long price = 1500L;

		// 기존에 1개 일정 추가
		Concert concert = Concert.create(
			"TDD 테스트 콘서트", "테스트 아티스트", now, "테스트 장소", price
		);
		ConcertDate concertDate = concert.getDates().get(0);
		List<ConcertSeat> concertSeats = concertDate.concertSeats();
		assertEquals(1, concert.getDates().size());
		assertEquals(50, concertSeats.size());

		for(int i= 0; i <50; i++ ) {
			// 전좌석 매진
			concertSeats.get( i ).reserve();
		}

		// when
		concertDate.soldOut();

		// then
		assertEquals(0, concertDate.countAvailableSeats());
		assertEquals(false, concertDate.isAvailable()); // 좌석 매진으로 예약불가
	}
}
