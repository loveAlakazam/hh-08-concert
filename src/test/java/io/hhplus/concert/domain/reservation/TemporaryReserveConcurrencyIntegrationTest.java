package io.hhplus.concert.domain.reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.TestcontainersConfiguration;
import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertDateRepository;
import io.hhplus.concert.domain.concert.ConcertRepository;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserRepository;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE reservations",
	"TRUNCATE TABLE concert_seats",
	"TRUNCATE TABLE concert_dates",
	"TRUNCATE TABLE concerts",
	"TRUNCATE TABLE user_point_histories",
	"TRUNCATE TABLE user_points",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class TemporaryReserveConcurrencyIntegrationTest {
	@Autowired
	private ReservationService reservationService;
	@Autowired private ReservationRepository reservationRepository;
	@Autowired private ConcertRepository concertRepository;
	@Autowired private ConcertDateRepository concertDateRepository;
	@Autowired private ConcertSeatRepository concertSeatRepository;
	@Autowired private UserRepository userRepository;
	private static final Logger log = LoggerFactory.getLogger(TemporaryReserveConcurrencyIntegrationTest.class);

	/**
	 * [동시성 테스트]
	 * 서로다른 두명의 사용자가 동시에 동일한 좌석을 예약하려고 할때 실패한다.
	 *
 	 */
	Concert sampleConcert;
	ConcertDate sampleConcertDate;
	ConcertSeat sampleConcertSeat;
	@BeforeEach
	void setUp() {
		// truncate -> setUp -> 테스트케이스 수행순으로 이뤄지고 있음.
		// 콘서트 테스트 데이터 셋팅
		sampleConcert = Concert.create(
			"테스트를 우선시하는 TDD 콘서트",
			"TDD",
			LocalDate.now(),
			"서울시 성동구 연무장길",
			10000
		);
		concertRepository.saveOrUpdate(sampleConcert);
		sampleConcertDate = sampleConcert.getDates().get(0);
		sampleConcertSeat = sampleConcertDate.getSeats().get(0);
	}
	@Test
	void 서로다른_두명의_사용자가_동일한_좌석을_예약하려고할때_한명만_예약이_가능하다() throws InterruptedException {
		// given
		User user1 = userRepository.save(User.of("최은강"));
		User user2 = userRepository.save(User.of("최금강"));;

		ReservationCommand.TemporaryReserve command1 = ReservationCommand.TemporaryReserve.of(user1, sampleConcertSeat);
		ReservationCommand.TemporaryReserve command2 = ReservationCommand.TemporaryReserve.of(user2, sampleConcertSeat);

		// when
		int threadCount = 2;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		List<Future<ReservationInfo.TemporaryReserve>> results = new ArrayList<>();
		// user1 이 예약신청
		results.add(executorService.submit(()-> {
			latch.countDown();
			latch.await();
			return reservationService.temporaryReserve(command1);
		}));
		// user2 가 예약신청
		results.add(executorService.submit(()-> {
			latch.countDown();
			latch.await();
			return reservationService.temporaryReserve(command2);
		}));
		executorService.shutdown();
		executorService.awaitTermination(5, TimeUnit.SECONDS);

		// then
		long successfulReservations = results.stream().filter(
			temporaryReserveFuture -> {
				try {
					return temporaryReserveFuture.get() != null;
				} catch (Exception e) {
					return false;
				}
			}).count();
		assertEquals(1, successfulReservations, "오직 한명의 유저만 좌석 예약에 성공한다");
	}
	@Test
	void 서로다른_사용자_10명이_동일한_좌석을_예약하려고할때_한명만_예약이_가능하다() throws InterruptedException {
		// given
		List<User> users = new ArrayList<>();
		List<ReservationCommand.TemporaryReserve> commands = new ArrayList<>();
		for(int i = 1; i <= 10 ; i++) {
			User user = User.of("최은강 "+i);
			users.add(userRepository.save(user));
			commands.add(ReservationCommand.TemporaryReserve.of(user, sampleConcertSeat));
		}
		// when
		int threadCount = users.size();
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		List<Future<ReservationInfo.TemporaryReserve>> results = new ArrayList<>();
		commands.forEach(command -> {
			Future<ReservationInfo.TemporaryReserve> commandResult = executorService.submit(() -> {
				latch.countDown();
				latch.await();
				return reservationService.temporaryReserve(command);
			});
			results.add(commandResult);
		});
		executorService.shutdown();


		// then
		long successfulReservations = results.stream().filter(
			temporaryReserveFuture -> {
				try {
					return temporaryReserveFuture.get() != null;
				} catch (Exception e) {
					return false;
				}
			}).count();

		// 로그작성
		for(int i = 0; i < results.size(); i++) {
			User user = users.get(i);
			Future<ReservationInfo.TemporaryReserve> future = results.get(i);
			try {
				ReservationInfo.TemporaryReserve info = future.get();
				if (info != null) {
					log.info("✅ 성공: {}(userId: {}) 님이 좌석을 예약하였습니다.", user.getName(), user.getId());
				} else {
					log.info("❌ 실패: {}(userId: {}) 님은 좌석예약에 실패하였습니다.", user.getName(), user.getId());
				}

			} catch(Exception e) {
				log.warn("❌ 실패: {}(userId: {}) 님은 처리중 예외발생으로 예약에 실패했습니다. - 발생예외: {} - 메시지: {}",
					user.getName(),
					user.getId(),
					e.getCause().getClass().getSimpleName(),
					e.getMessage()
				);
			}
		}
		assertEquals(1, successfulReservations, "오직 한명의 유저만 좌석 예약에 성공한다");
	}
}
