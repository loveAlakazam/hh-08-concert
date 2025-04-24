package io.hhplus.concert.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import io.hhplus.concert.TestcontainersConfiguration;
import io.hhplus.concert.domain.concert.Concert;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE user_points",
	"TRUNCATE TABLE user_point_histories",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserPointConcurrencyIntegrationTest {
	@Autowired private UserService userService;
	@Autowired private UserRepository userRepository;
	@Autowired private UserPointRepository userPointRepository;
	@Autowired private UserPointHistoryRepository userPointHistoryRepository;

	User sampleUser;
	UserPoint sampleUserPoint;
	@BeforeEach
	void setUp() {
		// truncate -> serUp -> 테스트케이스 수행 순으로 진행
		// 유저 테스트 데이터 셋팅
		sampleUser = User.of("최은강");
		userRepository.save(sampleUser);

		sampleUserPoint = UserPoint.of(sampleUser); // 초기포인트 0 포인트
		userPointRepository.save(sampleUserPoint);
	}

	@Test
	@Order(1)
	void 포인트_충전과_사용은_동시에_진행되어도_정합성이_깨지지_않아야_한다() throws Exception {
		// given
		long userId = sampleUser.getId();

		// 먼저 10,000원 충전
		userService.chargePoint(UserPointCommand.ChargePoint.of(userId, 10_000L));

		// 두 작업이 동시에 실행되도록 조율하는 CyclicBarrier
		CyclicBarrier barrier = new CyclicBarrier(2);

		ExecutorService executor = Executors.newFixedThreadPool(2);
		List<Future<Void>> results = new ArrayList<>();

		// 충전 쓰레드
		results.add(executor.submit(() -> {
			barrier.await(); // 🔥 다른 스레드가 도달할 때까지 대기
			userService.chargePoint(UserPointCommand.ChargePoint.of(userId, 5_000L));
			return null;
		}));

		// 사용 쓰레드
		results.add(executor.submit(() -> {
			barrier.await(); // 🔥 두 스레드가 동시에 실행되도록 조율
			userService.usePoint(UserPointCommand.UsePoint.of(userId, 5_000L));
			return null;
		}));

		// when: 두 작업이 완료될 때까지 기다림
		for (Future<Void> result : results) {
			result.get(); // 예외 발생 시 여기서 잡힘
		}

		// then: 최종 포인트는 10,000 + 5,000 - 5,000 = 10,000
		UserInfo.GetCurrentPoint info = userService.getCurrentPoint(UserPointCommand.GetCurrentPoint.of(userId));
		assertEquals(10_000L, info.point());
	}

}
