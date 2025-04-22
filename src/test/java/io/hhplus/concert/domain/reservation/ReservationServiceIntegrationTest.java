package io.hhplus.concert.domain.reservation;

import static io.hhplus.concert.domain.reservation.Reservation.*;
import static io.hhplus.concert.domain.reservation.ReservationStatus.*;
import static io.hhplus.concert.interfaces.api.common.validators.DateValidator.*;
import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static io.hhplus.concert.interfaces.api.reservation.ReservationErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = {
	"SET FOREIGN_KEY_CHECKS=0",
	"TRUNCATE TABLE reservations",
	"TRUNCATE TABLE concert_seats",
	"TRUNCATE TABLE concert_dates",
	"TRUNCATE TABLE concerts",
	"TRUNCATE TABLE users",
	"SET FOREIGN_KEY_CHECKS=1"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationServiceIntegrationTest {
	@Autowired private ReservationService reservationService;
	@Autowired private ReservationRepository reservationRepository;
	@Autowired private ConcertRepository concertRepository;
	@Autowired private ConcertDateRepository concertDateRepository;
	@Autowired private ConcertSeatRepository concertSeatRepository;
	@Autowired private UserRepository userRepository;

	private static final Logger log = LoggerFactory.getLogger(ReservationServiceIntegrationTest.class);

	Concert sampleConcert;
	ConcertDate sampleConcertDate;
	ConcertSeat sampleConcertSeat;
	User sampleUser;
	@BeforeEach
	void setUp() {
		// truncate -> setUp -> 테스트케이스 수행순으로 이뤄지고 있음.
		// 유저 테스트 데이터 셋팅
		sampleUser = User.of("최은강");
		userRepository.save(sampleUser);

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

	/**
	 * temporaryReserve : 임시예약
	 */
	@Order(1)
	@Test
	void 이미_점유된_좌석을_예약신청할_경우_BusinessException_예외발생() {
		// given
		// 해당 좌석은 이미 점유된 좌석이다.
		sampleConcertSeat.reserve();
		concertSeatRepository.saveOrUpdate(sampleConcertSeat);

		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> reservationService.temporaryReserve(ReservationCommand.TemporaryReserve.of(
				sampleUser,
				sampleConcertSeat
			))
		);
		assertEquals(ALREADY_RESERVED_SEAT.getMessage(), exception.getMessage());
	}
	@Order(2)
	@Test
	void 유저가_콘서트좌석_임시예약을_성공한다() {
		// when
		ReservationInfo.TemporaryReserve info = assertDoesNotThrow(
			()-> reservationService.temporaryReserve(
				ReservationCommand.TemporaryReserve.of(sampleUser, sampleConcertSeat)
			)
		);
		// then
		// 콘서트 객체가 null이 아닌지 확인
		Reservation reservation = info.reservation();
		assertNotNull(reservation);
		assertInstanceOf(Reservation.class, reservation);
		// 콘서트 좌석은 점유된 상태인지 확인
		assertFalse(sampleConcertSeat.isAvailable());
		// 예약 상태는 임시예약 상태인지
		assertEquals(PENDING_PAYMENT, reservation.getStatus());
		// 임시 예약만료 일자가 기재되어있는지 확인
		assertNotNull(reservation.getTempReservationExpiredAt());
		// 아직 임시상태이므로 예약확정일자는 null 인지 확인
		assertNull(reservation.getReservedAt());
		// 예약만료일자가 아직 유효한지 확인
		assertFalse(isPastDateTime(reservation.getTempReservationExpiredAt()));
		// 임시예약상태인지 최종 확인
		assertTrue(reservation.isTemporary());
	}
	@Order(3)
	@Test
	void 이미_좌석을_결제하여_예약확정상태인데_동일한좌석을_임시예약_할_수_없으므로_BusinessException_예외발생() {
		// given
		// 임시예약이 만료된 상태의 예약이다.
		log.info("임시예약 신청하여 좌석 5분간 임시점유 상태");
		ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(sampleUser, sampleConcertSeat)
		);
		Reservation reservation = info.reservation(); // 임시예약상태
		long reservationId = reservation.getId();
		// 임시 예약 상태 확인
		assertNotNull(reservation);
		assertTrue(reservation.isTemporary());
		// 좌석이 점유(isAvailable = false) 됐는지 확인
		assertFalse(reservation.getConcertSeat().isAvailable());
		// 이미 예약 확정 상태
		ReservationInfo.Confirm confirmedInfo = reservationService.confirm(ReservationCommand.Confirm.of(reservationId));
		reservation = confirmedInfo.reservation();

		// 이미 예약확정 처리했는데 동일한 좌석을 임시예약상태로 변경은 비즈니스 규칙에 위배되므로 예외발생
		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			()-> reservationService.temporaryReserve(
				ReservationCommand.TemporaryReserve.of(sampleUser, sampleConcertSeat)
			)
		);
		assertEquals(INVALID_ACCESS.getMessage(), exception.getMessage());
		assertEquals(INVALID_ACCESS.getHttpStatus(), exception.getHttpStatus());
	}

	/**
	 * get: 예약 조회
	 */
	@Order(4)
	@Test
	void 예약식별자가_0이하의_음수이면_InvalidValidationException_예외발생() {
		// given
		long notExistId = 0L;
		// when & then
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> reservationService.get(ReservationCommand.Get.of(notExistId))
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER.getMessage(), exception.getMessage());
	}
	@Order(5)
	@Test
	void 예약식별자에_대응되는_예약데이터가_데이터베이스에_존재하지않으면_BusinessException_예외발생() {
		// given
		long notExistId = 999L;
		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> reservationService.get(ReservationCommand.Get.of(notExistId))
		);
		assertEquals(NOT_FOUND_RESERVATION.getMessage(), exception.getMessage());
	}

	/**
	 * confirm: 예약확정
	 */
	@Order(6)
	@Test
	void 예약확정요청중_예약식별자에_대응되는_예약데이터가_존재하지_않으면_BusinessException_예외발생() {
		// given
		long notExistId = 999L;
		// when & then
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> reservationService.confirm(ReservationCommand.Confirm.of(notExistId))
		);
		assertEquals(NOT_FOUND_RESERVATION.getMessage(), exception.getMessage());
	}
	@Order(7)
	@Test
	void 예약확정_성공() {
		// given
		// 임시예약이 만료된 상태의 예약이다.
		log.info("임시예약 신청하여 좌석 5분간 임시점유 상태");
		ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(sampleUser, sampleConcertSeat)
		);
		Reservation reservation = info.reservation(); // 임시예약상태
		long reservationId = reservation.getId();
		// 임시 예약 상태 확인
		assertNotNull(reservation);
		assertTrue(reservation.isTemporary());
		// 좌석이 점유(isAvailable = false) 됐는지 확인
		assertFalse(reservation.getConcertSeat().isAvailable());

		// when & then
		ReservationInfo.Confirm confirmedInfo = assertDoesNotThrow(
			() -> reservationService.confirm(ReservationCommand.Confirm.of(reservationId))
		);
		assertNotNull(confirmedInfo.reservation());
		assertInstanceOf(Reservation.class, confirmedInfo.reservation());
		reservation = confirmedInfo.reservation();
		// 좌석이 점유(isAvailable = false) 됐는지 확인
		assertFalse(reservation.getConcertSeat().isAvailable());
		// 예약확정 상태인지 확인
		assertTrue(reservation.isConfirm());
		assertEquals(CONFIRMED, reservation.getStatus());
		assertNotNull(reservation.getReservedAt());
		assertNull(reservation.getTempReservationExpiredAt());
	}
	@Order(8)
	@Test
	void 예약확정요청중_예약상태가_확정된상태인데_중복으로_확정요청_상태라면_BusinessException_예외발생() throws InterruptedException {
		// given
		// 임시예약이 만료된 상태의 예약이다.
		log.info("임시예약 신청하여 좌석 5분간 임시점유 상태");
		ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(sampleUser, sampleConcertSeat)
		);
		Reservation reservation = info.reservation(); // 임시예약상태
		long reservationId = reservation.getId();
		// 임시 예약 상태 확인
		assertNotNull(reservation);
		assertTrue(reservation.isTemporary());

		log.info("5분 내로 결제처리가 완료됐다고 가정하면, 예약의 상태는 확정상태가 됨");
		ReservationInfo.Confirm confirmedInfo = reservationService.confirm(ReservationCommand.Confirm.of(reservationId));
		reservation = confirmedInfo.reservation(); // 예약확정
		// 예약확정 확인
		assertInstanceOf(Reservation.class, reservation);
		assertTrue(reservation.isConfirm());

		// when & then
		// 임시예약이 만료되어있는데 예약확정상태로 변경하려는 경우에 비즈니스규칙에 위배
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> reservationService.confirm(ReservationCommand.Confirm.of(reservationId))
		);
		assertEquals(INVALID_ACCESS.getMessage(), exception.getMessage());
	}
	@Order(9)
	@Test
	void 예약확정요청중_예약상태가_임시예약상태가_아닌_만료된_상태라면_BusinessException_예외발생() throws InterruptedException {
		// given
		// 임시예약이 만료된 상태의 예약이다.
		log.info("임시예약 신청하여 좌석 5분간 임시점유 상태");
		ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(sampleUser, sampleConcertSeat)
		);
		Reservation reservation = info.reservation();
		assertNotNull(reservation);
		assertTrue(reservation.isTemporary()); // 임시예약상태
		long reservationId = reservation.getId();
		log.info("5분 + 1ms 대기");
		Thread.sleep(TEMPORARY_RESERVATION_DURATION_MILLISECOND + 1);

		log.info("임시예약이 만료됨");
		assertTrue(isPastDateTime(reservation.getTempReservationExpiredAt()));
		// when & then
		// 임시예약이 만료되어있는데 예약확정상태로 변경하려는 경우에 비즈니스규칙에 위배
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> reservationService.confirm(ReservationCommand.Confirm.of(reservationId))
		);
		assertEquals(INVALID_ACCESS.getMessage(), exception.getMessage());
	}

	/**
	 * 예약 취소
	 */
	@Order(10)
	@Test
	void 예약상태가_임시예약상태가_아닌_만료된_상태라면_예약상태는_취소상태로_변경이_가능하다() throws InterruptedException {
		// given
		// 임시예약이 만료된 상태의 예약이다.
		log.info("임시예약 신청하여 좌석 5분간 임시점유 상태");
		ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(sampleUser, sampleConcertSeat)
		);
		Reservation reservation = info.reservation();
		assertNotNull(reservation);
		assertTrue(reservation.isTemporary()); // 임시예약상태
		long reservationId = reservation.getId();
		log.info("5분 + 1ms 대기");
		Thread.sleep(TEMPORARY_RESERVATION_DURATION_MILLISECOND + 1);

		log.info("임시예약이 만료됨");
		assertTrue(isPastDateTime(reservation.getTempReservationExpiredAt()));
		// when & then
		// 임시예약이 만료되어있는데 예약확정상태로 변경하려는 경우에 비즈니스규칙에 위배
		ReservationInfo.Cancel cancelInfo = assertDoesNotThrow(
			() -> reservationService.cancel(ReservationCommand.Cancel.of(reservationId))
		);
		assertNotNull(cancelInfo.reservation());
		assertInstanceOf(Reservation.class, cancelInfo.reservation());
		// 예약상태 확인
		reservation = cancelInfo.reservation();
		assertEquals(CANCELED, reservation.getStatus());
		assertTrue(isPastDateTime(reservation.getTempReservationExpiredAt()));
		assertNotNull(reservation.getTempReservationExpiredAt());
		assertNull(reservation.getReservedAt());
		// 예약만료로 점유된 좌석도 임시예약이 취소되어 예약가능한 상태로 변경
		assertTrue(reservation.getConcertSeat().isAvailable());
	}
	@Order(11)
	@Test
	void 예약상태가_확정된상태인데_취소상태로_변경하게되면_임시예약만료일자가_존재하지않으므로_InvalidValidationException_예외발생() {
		// given
		// 임시예약이 만료된 상태의 예약이다.
		log.info("임시예약 신청하여 좌석 5분간 임시점유 상태");
		ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(sampleUser, sampleConcertSeat)
		);
		Reservation reservation = info.reservation(); // 임시예약상태
		long reservationId = reservation.getId();
		// 임시 예약 상태 확인
		assertNotNull(reservation);
		assertTrue(reservation.isTemporary());

		log.info("5분 내로 결제처리가 완료됐다고 가정하면, 예약의 상태는 확정상태가 됨");
		ReservationInfo.Confirm confirmedInfo = reservationService.confirm(ReservationCommand.Confirm.of(reservationId));
		reservation = confirmedInfo.reservation(); // 예약확정
		// 예약확정 확인
		assertInstanceOf(Reservation.class, reservation);
		assertTrue(reservation.isConfirm());

		// when & then
		// 임시예약이 만료되어있는데 예약확정상태로 변경하려는 경우에 비즈니스규칙에 위배
		InvalidValidationException exception = assertThrows(
			InvalidValidationException.class,
			() -> reservationService.cancel(ReservationCommand.Cancel.of(reservationId))
		);
		assertEquals(SHOULD_NOT_EMPTY.getMessage(), exception.getMessage());
	}
	@Order(12)
	@Test
	void 임시예약이_아직_유효한상태에서_취소상태로_변경하게되면_만료일자_날짜가_아직_유효하므로_BusinessException_예외발생() {
		// given
		// 임시예약이 만료된 상태의 예약이다.
		log.info("임시예약 신청하여 좌석 5분간 임시점유 상태");
		ReservationInfo.TemporaryReserve info = reservationService.temporaryReserve(
			ReservationCommand.TemporaryReserve.of(sampleUser, sampleConcertSeat)
		);
		Reservation reservation = info.reservation(); // 임시예약상태
		long reservationId = reservation.getId();
		// 임시 예약 상태 확인
		assertNotNull(reservation);
		assertTrue(reservation.isTemporary());
		assertFalse(isPastDateTime(reservation.getTempReservationExpiredAt()));

		// when & then
		// 임시예약이 만료되어있는데 예약확정상태로 변경하려는 경우에 비즈니스규칙에 위배
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> reservationService.cancel(ReservationCommand.Cancel.of(reservationId))
		);
		assertEquals(INVALID_ACCESS.getMessage(), exception.getMessage());
	}

}
