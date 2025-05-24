package io.hhplus.concert.interfaces.events;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import io.hhplus.concert.domain.concert.ConcertRankingRepository;
import io.hhplus.concert.domain.concert.ConcertRepository;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.payment.PaymentSuccessEvent;
import io.hhplus.concert.domain.payment.SoldOutConcertDateFailEvent;
import io.hhplus.concert.domain.payment.SoldOutConcertDateFailEventPublisher;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.support.CacheCleaner;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.infrastructure.containers.TestcontainersConfiguration;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@RecordApplicationEvents
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE redis_ranking_snapshots",
	"TRUNCATE TABLE payments",
	"TRUNCATE TABLE reservations",
	"TRUNCATE TABLE concert_seats",
	"TRUNCATE TABLE concert_dates",
	"TRUNCATE TABLE concerts",
	"TRUNCATE TABLE user_point_histories",
	"TRUNCATE TABLE user_points",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaymentSuccessEventListenerIntegrationTest {
	@Autowired private PaymentSuccessEventListener paymentSuccessEventListener;

	@Autowired private ConcertService concertService;
	@Autowired private ReservationService reservationService;
	@Autowired private ConcertRankingRepository concertRankingRepository;
	@Autowired private SoldOutConcertDateFailEventPublisher soldOutConcertDateFailEventPublisher;

	@Autowired private ConcertRepository concertRepository;
	@Autowired private ConcertDateRepository concertDateRepository;
	@Autowired private ConcertSeatRepository concertSeatRepository;
	@Autowired private ReservationRepository reservationRepository;
	@Autowired private UserRepository userRepository;

	@Autowired private CacheCleaner cacheCleaner;
	@Autowired private CacheStore cacheStore;
	private static final Logger log = LoggerFactory.getLogger(PaymentSuccessEventListenerIntegrationTest.class);

	@BeforeEach
	void setUp() {
		cacheCleaner.cleanAll();
	}

	@Order(1)
	@Nested
	class PaymentSuccessEventListenerIntegration {
		@Test
		void 전좌석이_모두_예약확정이라서_매진이되면_레디스에_기록됨을_확인할_수있다(ApplicationEvents events) {
			// given
			Concert concert = concertRepository.saveOrUpdate(Concert.create("테스트콘서트", "테스트아티스트", LocalDate.now(), "장소", 1000));
			ConcertDate concertDate = concert.getDates().get(0);
			List<ConcertSeat> concertSeats = concertDate.getSeats();
			User user = userRepository.save(User.of("테스트유저"));

			for (ConcertSeat seat : concertSeats) {
				Reservation reservation = reservationRepository.saveOrUpdate(
					Reservation.of(user, concert, concertDate, seat)
				);
				reservation.temporaryReserve(); // 임시예약
				reservation.confirm(); // 예약확정 처리
				reservationRepository.saveOrUpdate(reservation);
			}

			// when: 결제 성공 이벤트 발행 (매진 트리거)
			paymentSuccessEventListener.handleSoldOutConcertDate(
				new PaymentSuccessEvent(1L, concert.getId(), concertDate.getId())
			);

			// then: Awaitility로 비동기 이벤트 처리 대기
			await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
				// 1. 콘서트일정 isAvailable == false
				ConcertDate updated = concertDateRepository.findConcertDateById(concertDate.getId());
				assertThat(updated.isAvailable()).isFalse();

				// 2. 레디스 일간랭킹에 기록됨
				String key = "daily_famous_concert_rank:" + LocalDate.now(ZoneId.of("Asia/Seoul"));
				Set<Object> members = concertRankingRepository.getDailyFamousConcertRanking();
				String expectMember = String.format("concert:%s:%s", concert.getId(), concertDate.getProgressDate());
				assertThat(members).contains(expectMember);

				// 3. 매진처리 실패이벤트가 발생하지 않음
				assertThat(events.stream(SoldOutConcertDateFailEvent.class).count()).isZero();
			});
		}
	}

}
