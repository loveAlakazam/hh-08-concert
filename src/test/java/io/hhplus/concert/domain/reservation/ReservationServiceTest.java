package io.hhplus.concert.domain.reservation;

import static io.hhplus.concert.domain.concert.Concert.*;
import static io.hhplus.concert.domain.concert.ConcertService.*;
import static io.hhplus.concert.domain.reservation.ReservationStatus.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertInfo;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.reservation.ReservationRepository;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.domain.user.UserRepository;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.validators.DateValidator;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private ConcertSeatRepository concertSeatRepository;
	@Mock
	private CacheStore cacheStore;


	@BeforeEach
	void setUp() {
		reservationService = new ReservationService(reservationRepository,concertSeatRepository, cacheStore);
	}

	private static final Logger log = LoggerFactory.getLogger(ReservationServiceTest.class);


	@Nested
	@DisplayName("temporaryReserve")
	class TemporaryReserve {
		@Test
		void 해당좌석에_대한_예약이력이_없는경우_임시예약을_신청한다() {
			// given
			long userId = 1L;
			long concertId = 1L;
			long concertDateId = 1L;
			long concertSeatId = 1L;

			User user = User.of("테스트");
			Concert concert = Concert.create("테스트 콘서트", "테스트 아티스트", LocalDate.now(), "테스트 장소", 15000);
			ConcertDate concertDate = concert.getDates().get(0);
			ConcertSeat concertSeat = concertDate.getSeats().get(0);

			// id 설정
			ReflectionTestUtils.setField(user, "id", userId); // user 아이디 수동설정
			ReflectionTestUtils.setField(concert, "id", concertId); // concert 아이디 수동설정
			ReflectionTestUtils.setField(concertDate, "id", concertDateId); // concertDate 아이디 수동설정
			ReflectionTestUtils.setField(concertSeat, "id", concertSeatId); // concertSeat 아이디 수동설정


			log.info("예약내역이 없음");
			when(reservationRepository.findByConcertSeatIdAndUserId(anyLong(), anyLong()))
				.thenReturn(null);

			// when
			log.info("when: 임시예약 요청");
			ReservationCommand.TemporaryReserve command = ReservationCommand.TemporaryReserve.of(user, concertSeat);
			ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(command);

			// then
			assertTrue(info.reservation().isTemporary()); // 임시예약상태인지 확인
			assertEquals(ReservationStatus.PENDING_PAYMENT, info.reservation().getStatus());
			assertFalse(concertSeat.isAvailable()); // 좌석은 이미 예약된 상태

			String cacheKey = CONCERT_SEAT_LIST_CACHE_KEY + "-" + "concert_id:" + concertId +"-" + "concert_date_id:" + concertDateId;
			verify(cacheStore).evict(eq(cacheKey)); // 캐시삭제 여부 검증
		}
		@Test
		void 예약이_취소상태일때_예약하려는좌석이_예약가능한상태라면_다시_예약할수있다()  {
			// given
			long userId = 1L;
			long concertId = 1L;
			long concertDateId = 1L;
			long concertSeatId = 1L;

			User user = User.of("테스트");
			Concert concert = Concert.create("테스트 콘서트", "테스트 아티스트", LocalDate.now(), "테스트 장소", 15000);
			ConcertDate concertDate = concert.getDates().get(0);
			ConcertSeat concertSeat = concertDate.getSeats().get(0);

			// id 설정
			ReflectionTestUtils.setField(user, "id", userId); // user 아이디 수동설정
			ReflectionTestUtils.setField(concert, "id", concertId); // concert 아이디 수동설정
			ReflectionTestUtils.setField(concertDate, "id", concertDateId); // concertDate 아이디 수동설정
			ReflectionTestUtils.setField(concertSeat, "id", concertSeatId); // concertSeat 아이디 수동설정

			// 예약 이력 설정: 취소상태
			Reservation reservation = Reservation.of(user, concert, concertDate, concertSeat);
			reservation.temporaryReserve();
			reservation.expireTemporaryReserve(LocalDateTime.now().minusSeconds(1));
			reservation.cancel();

			when(reservationRepository.findByConcertSeatIdAndUserId(userId, concertSeatId)).thenReturn(reservation);

			// when
			ReservationCommand.TemporaryReserve command = ReservationCommand.TemporaryReserve.of(user, concertSeat);
			ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(command);

			// then
			assertTrue(info.reservation().isTemporary()); // 임시예약상태인지 확인
			assertEquals(ReservationStatus.PENDING_PAYMENT, info.reservation().getStatus());
			assertFalse(concertSeat.isAvailable()); // 좌석은 이미 예약된 상태

			String cacheKey = CONCERT_SEAT_LIST_CACHE_KEY + "-" + "concert_id:" + concertId +"-" + "concert_date_id:" + concertDateId;
			verify(cacheStore).evict(eq(cacheKey)); // 캐시삭제 여부 검증
		}

	}


	@Test
	void 임시예약이_만료되면_예약취소상태로_변경할_수_있다() throws InterruptedException {
		// given
		long reservationId = 1L;
		User user = User.of("테스트");
		Concert concert = Concert.create("테스트 콘서트", "테스트 아티스트", LocalDate.now(), "테스트 장소", 15000);
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);
		assertTrue(concertSeat.isAvailable()); // 해당좌석은 예약가능

		log.info("해당 좌석 임시예약 상태로 변경");
		Reservation reservation = Reservation.of(user, concert, concertDate, concertSeat);
		assertDoesNotThrow(()-> reservation.temporaryReserve());
		assertTrue(reservation.isTemporary());

		log.info("임시예약 유효기간 만료");
		reservation.expireTemporaryReserve(LocalDateTime.now().minusSeconds(1));

		when( reservationRepository.findById( reservationId )).thenReturn(reservation);
		// when
		log.info("when: 임시예약이 유효일자가 만료되어 취소상태로 변경을 요청한다");
		ReservationInfo.Cancel info = reservationService.cancel(ReservationCommand.Cancel.of( reservationId ));
		// then
		assertEquals(CANCELED, info.reservation().getStatus());
		assertTrue(DateValidator.isPastDateTime(info.reservation().getTempReservationExpiredAt()));
	}
	@Test
	void 임시예약이_만료된_상태에서_결제를_신청하면_BusinessException이_발생한다() throws InterruptedException {
		// given
		User user = User.of("테스트");
		Concert concert = Concert.create("테스트 콘서트", "테스트 아티스트", LocalDate.now(), "테스트 장소", 15000);
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);
		assertTrue(concertSeat.isAvailable()); // 해당좌석은 예약가능

		log.info("해당 좌석 임시예약 상태로 변경");
		long reservationId = 1L;
		Reservation reservation = Reservation.of(user, concert, concertDate, concertSeat);
		assertDoesNotThrow(()-> reservation.temporaryReserve());
		assertTrue(reservation.isTemporary());

		log.info("임시예약 유효기간이 만료됨");
		reservation.expireTemporaryReserve(LocalDateTime.now().minusSeconds(1));

		log.info("임시예약이 유효일자가 만료되어 취소상태로 변경");
		assertDoesNotThrow(() -> reservation.cancel()); // 임시예약상태로 변경
		assertFalse(reservation.isTemporary()); // 임시예약상태가 아님

		when(reservationRepository.findById(reservationId)).thenReturn(reservation);

		// when & then
		log.info("when: 임시예약이 유효일자가 만료된 상태에서 예약확정 상태로 변경을 요청한다");
		BusinessException ex = assertThrows(
			BusinessException.class,
			() -> reservationService.confirm(ReservationCommand.Confirm.of(reservationId))
		);
		assertEquals(INVALID_ACCESS.getHttpStatus(), ex.getHttpStatus());
		assertEquals(INVALID_ACCESS.getMessage(), ex.getMessage());
		assertTrue(concertSeat.isAvailable()); // 해당좌석은 이미 취소되었으므로 예약이 가능하다
	}
	@Test
	void 임시예약이_유효한상태에서_예약확정을_요청하면_상태변경에_성공된다() {
		// given
		User user = User.of("테스트");
		Concert concert = Concert.create("테스트 콘서트", "테스트 아티스트", LocalDate.now(), "테스트 장소", 15000);
		ConcertDate concertDate = concert.getDates().get(0);
		ConcertSeat concertSeat = concertDate.getSeats().get(0);
		assertTrue(concertSeat.isAvailable()); // 해당좌석은 예약가능

		log.info("해당 좌석 임시예약 상태로 변경");
		long reservationId = 1L;
		Reservation reservation = Reservation.of(user, concert, concertDate, concertSeat);
		assertDoesNotThrow(()-> reservation.temporaryReserve());
		assertTrue(reservation.isTemporary()); // 임시예약상태

		when(reservationRepository.findById(reservationId)).thenReturn(reservation);

		// when
		log.info("when: 임시예약이 유효일자가 만료된 상태에서 예약확정 상태로 변경을 요청한다");
		ReservationInfo.Confirm info = assertDoesNotThrow(
			() -> reservationService.confirm(ReservationCommand.Confirm.of(reservationId))
		);

		// then
		assertTrue(info.reservation().isConfirm()); // 예약확정상태인지 확인
		assertEquals(ReservationStatus.CONFIRMED, info.reservation().getStatus());
		assertNotNull(info.reservation().getReservedAt());
		assertNull(info.reservation().getTempReservationExpiredAt());
		assertFalse(concertSeat.isAvailable()); // 좌석은 예약불가능 상태인지 확인
	}
	@Test
	void 상태가_CONFIRMED_인_예약들의_개수를_구한다() {
		// given
		long concertId = 1L;
		long concertDateId = 1L;

		Concert concert = Concert.create("테스트 콘서트", "테스트 아티스트", LocalDate.now(), "테스트 장소명", 2000);
		ConcertDate concertDate = concert.getDates().get(0);
		List<ConcertSeat> concertSeats = concertDate.getSeats();

		when(reservationRepository.countConfirmedReservations(concertId, concertDateId)).thenReturn(50L);

		// when
		long result = reservationService.countConfirmedSeats(ReservationCommand.CountConfirmedSeats.of(concertId, concertDateId));

		// then
		assertEquals(concertSeats.size(), result);
		verify(reservationRepository, times(1)).countConfirmedReservations(concertId, concertDateId);
	}
}
