package io.hhplus.concert.domain.token;

import java.util.List;
import java.util.UUID;


public interface WaitingQueue {
	/**
	 * 대기열큐 안에 대기상태토큰이 몇개 있는지 확인하기위해서 대기열큐의 길이를 의미한다
	 * @return int
	 */
	int size();
	/**
	 * 대기열큐에 대기상태토큰을 넣는다
	 * @param uuid
	 */
	void enqueue(UUID uuid);
	/**
	 * 대기열큐의 가장맨앞의 요소(대기상태토큰)를 조회만 하고 아직 빠져나가지 않는다.
	 * @return uuid
	 */
	UUID peek();
	/**
	 * 맨앞의 요소(대기상태토큰)가 대기열큐에서 빠져나간다.
	 * 빠져 나간 토큰은 활성화 상태로 변경된다.
	 *
	 * @return uuid
	 */
	UUID dequeue();
	/**
	 * 대기열큐 안에 있는 토큰중 uuid에 일치한 토큰을 큐에서 제거된다. <br>
	 * 대기열큐 안에 있는 토큰중 시간이 만료된 토큰을 대상으로 제거하는 목적으로 활용한다.
	 * @param uuid
	 */
	void remove(UUID uuid);
	/**
	 * 대기열큐에 uuid 에 해당되는 토큰이 들어있는지 확인한다
	 * @return boolean
	 */
	boolean contains(UUID uuid);
	/**
	 * 요청 대기토큰(requestUUID)이 대기열큐에서 몇번째 인덱스에 위치하고있는지 나타냄
	 * 시작점이 1인 인덱스기반으로 맨앞에 있는 요소는 1번째 로 리턴.<br>
	 * - 요청 대기토큰이 큐에 들어있을때 위치: 1 <= poisition <= queue.size() <br>
	 * - 요청 대기토큰이 큐에 들어있지않을때 위치: -1
	 * @param requestUUID
	 * @return int
	 */
	int getPosition(UUID requestUUID);
	/**
	 * 현재 대기열큐를 리스트로 변환한다.
	 * @return List
	 */
	List<UUID> toList();
	/**
	 * 대기열큐를 비운다
	 */
	void clear();
}
