package io.hhplus.concert.infrastructure.queue;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import io.hhplus.concert.domain.token.WaitingQueue;
import lombok.RequiredArgsConstructor;

/**
 * 인메모리 기반 대기열큐
 */
@Component
@RequiredArgsConstructor
public class InMemoryWaitingQueueImpl implements WaitingQueue {
	private final Queue<UUID> queue = new ConcurrentLinkedQueue<>();

	public int size() {
		return queue.size();
	}

	public void enqueue(UUID uuid) {
		queue.offer(uuid);
	}

	public UUID peek() {
		return queue.peek();
	}

	public UUID dequeue() {
		return queue.poll();
	}

	public void remove(UUID uuid) {
		queue.remove(uuid);
	}

	public boolean contains(UUID uuid) {
		return queue.contains(uuid);
	}

	public int getPosition(UUID requestUUID) {
		int index = 0;
		for(UUID uuid: queue) {
			if(requestUUID.equals(uuid)) return index + 1;
			index++;
		}
		return -1; // 대기열에 없음
	}

	public List<UUID> toList() {
		return List.copyOf(queue);
	}

}
