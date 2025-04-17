package io.hhplus.concert.interfaces.queue;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.hhplus.concert.domain.token.WaitingQueue;
import io.hhplus.concert.infrastructure.queue.InMemoryWaitingQueueImpl;

public class WaitingQueueUnitTest {
	private WaitingQueue queue;
	@BeforeEach
	void setUp() {
		queue = new InMemoryWaitingQueueImpl();
	}

	@Test
	void 큐에_삽입이_성공된다() {
		// given
		UUID uuid = UUID.randomUUID();

		// when
		queue.enqueue(uuid);

		// then
		assertThat(queue.size()).isEqualTo(1);
		assertThat(queue.contains(uuid)).isTrue();
		assertThat(queue.getPosition(uuid)).isEqualTo(1);
	}
	@Test
	void peek은_맨첫번째_요소만을_조회할_수_있다() {
		// given
		UUID uuid1 = UUID.randomUUID();
		UUID uuid2 = UUID.randomUUID();
		queue.enqueue(uuid1);
		queue.enqueue(uuid2);

		// when
		UUID result = queue.peek();

		// then
		assertThat(result).isNotEqualTo(uuid2);
		assertThat(result).isEqualTo(uuid1);
		assertThat(queue.size()).isEqualTo(2);
	}
	@Test
	void dequeue는_맨첫번째_요소만을_큐에서_제거한다() {
		// given
		UUID uuid1 = UUID.randomUUID();
		UUID uuid2 = UUID.randomUUID();
		queue.enqueue(uuid1);
		queue.enqueue(uuid2);

		// when
		UUID result = queue.dequeue();

		// then
		assertThat(result).isEqualTo(uuid1);
		assertThat(result).isNotEqualTo(uuid2);
		assertThat(queue.size()).isEqualTo(1);
	}
}
