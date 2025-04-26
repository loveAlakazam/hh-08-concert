package io.hhplus.concert.domain.reservation;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertSeatRepository;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.validators.DateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationMaintenanceServiceTest {
    @InjectMocks
    private ReservationMaintenanceService reservationMaintenanceService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ConcertSeatRepository concertSeatRepository;

    @BeforeEach
    void setUp() {
        reservationMaintenanceService = new ReservationMaintenanceService(
                reservationRepository,
                concertSeatRepository
        );
    }
    private static final Logger log = LoggerFactory.getLogger(ReservationMaintenanceServiceTest.class);

    @Test
    void 임시예약날짜가_만료되면_예약상태는_취소상태로_변경되며_좌석도_예약가능으로_변경된다() throws InterruptedException {
        // given
        User user = User.of("예약자1");
        Concert concert = Concert.create("테스트콘서트", "아티스트명", LocalDate.now(), "콘서트장소", 2000);
        ConcertDate concertDate = concert.getDates().get(0);
        ConcertSeat concertSeat = concertDate.getSeats().get(0);

        Reservation reservation = Reservation.of(user,concert, concertDate, concertSeat);
        reservation.temporaryReserve();// 임시에약상태로 변경
        log.info(":: 임시예약 만료가 될때까지 5분 대기");
        Thread.sleep(5 * 60 * 1000 + 1);

        List<Reservation> expiredReservations = List.of(reservation);

        // 임시예약유효일자가 만료된 예약은 1개
        when(reservationRepository.findExpiredTempReservations()).thenReturn(expiredReservations);

        // updateCanceledExpiredTempReservations 메소드가 호출될 때 실제로 reservation 의 상태를 변경하도록 설정
        doAnswer(invocation -> {
            reservation.cancel(); // 직접 reservation 객체의 상태를 변경
            return reservation;
        }).when(reservationRepository).updateCanceledExpiredTempReservations();

        //when
        reservationMaintenanceService.cancel();

        // then
        verify(reservationRepository, times(1)).updateCanceledExpiredTempReservations();

        // 예약상태 확인
        assertEquals(ReservationStatus.CANCELED, reservation.getStatus(), "취소상태이다");
        assertFalse(reservation.isTemporary(), "임시예약상태가 아니다");
        assertTrue(DateValidator.isPastDateTime(reservation.getTempReservationExpiredAt()), "임시예약만료일자가 이미 만료된상태다");
        assertNull( reservation.getReservedAt(), "예약확정일은 없는상태다");
        // 좌석상태 확인
        assertTrue(reservation.getConcertSeat().isAvailable(), "예약이 가능하다");
    }

    @Test
    void 임시예약이_취소되면_soft_delete_처리() {
        // when
        reservationMaintenanceService.deleteCanceledReservations();

        // then
        verify(reservationRepository, times(1)).deleteCanceledReservations();
    }
}
