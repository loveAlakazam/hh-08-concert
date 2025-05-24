package io.hhplus.concert.application.usecase.reservation;

import static io.hhplus.concert.domain.concert.Concert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertSeat;
import io.hhplus.concert.domain.concert.ConcertService;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.domain.reservation.ReservationService;
import io.hhplus.concert.domain.reservation.ReservationStatus;
import io.hhplus.concert.domain.support.CacheStore;
import io.hhplus.concert.domain.user.User;
import io.hhplus.concert.interfaces.api.common.validators.DateValidator;

@ExtendWith(MockitoExtension.class)
public class ReservationMaintenanceUsecaseTest {
    @InjectMocks
    private ReservationMaintenanceUsecase reservationMaintenanceUsecase;
    @Mock
    private ReservationService reservationService;
    @Mock
    private ConcertService concertService;
    @Mock
    private CacheStore cacheStore;


    @BeforeEach
    void setUp() {
        reservationMaintenanceUsecase = new ReservationMaintenanceUsecase(
            reservationService,
            concertService,
            cacheStore
        );
    }
    private static final Logger log = LoggerFactory.getLogger(ReservationMaintenanceUsecaseTest.class);

    @Test
    void 임시예약날짜가_만료되면_예약상태는_취소상태로_변경되며_좌석도_예약가능으로_변경된다() {
        // given
        long userId = 1L;
        long concertId = 1L;
        long concertDateId = 1L;
        long concertSeatId = 1L;

        User user = User.of("예약자1");
        Concert concert = Concert.create("테스트콘서트", "아티스트명", LocalDate.now(), "콘서트장소", 2000);
        ConcertDate concertDate = concert.getDates().get(0);
        ConcertSeat concertSeat = concertDate.getSeats().get(0);

        // id 설정
        ReflectionTestUtils.setField(user, "id", userId); // user 아이디 수동설정
        ReflectionTestUtils.setField(concert, "id", concertId); // concert 아이디 수동설정
        ReflectionTestUtils.setField(concertDate, "id", concertDateId); // concertDate 아이디 수동설정
        ReflectionTestUtils.setField(concertSeat, "id", concertSeatId); // concertSeat 아이디 수동설정

        // 예약만료
        Reservation reservation = Reservation.of(user,concert, concertDate, concertSeat);
        reservation.temporaryReserve(); // 임시에약상태로 변경
        reservation.expireTemporaryReserve(LocalDateTime.now().minusSeconds(1)); // 임시예약상태를 만료
        List<Reservation> expiredReservations = List.of(reservation);

        when(reservationService.expiredReservations()).thenReturn(expiredReservations); // 임시예약유효일자가 만료된 예약은 1개
        doAnswer(invocation -> {
            reservation.cancel();
            return reservation;
        }).when(reservationService).cancelExpiredTempReservations(); // 예약상태를 '취소'로 변경

        //when
        reservationMaintenanceUsecase.cancel();

        // then
        verify(reservationService, times(1)).cancelExpiredTempReservations();
        Assertions.assertEquals(ReservationStatus.CANCELED, reservation.getStatus(), "예약상태는 취소상태이다");
        assertFalse(reservation.isTemporary(), "취소상태로 변경됐으므로 임시예약상태가 아니다");
        assertTrue(DateValidator.isPastDateTime(reservation.getTempReservationExpiredAt()), "만료일자는 현재보다 이전이어야한다.");
        assertNull( reservation.getReservedAt(), "예약확정일은 없어야한다");
        assertTrue(reservation.getConcertSeat().isAvailable(), "좌석은 다시 예약이 가능하다");

        String cacheKey = CONCERT_SEAT_LIST_CACHE_KEY + "-" + "concert_id:" + concertId +"-" + "concert_date_id:" + concertDateId;
        verify(cacheStore).evict(eq(cacheKey)); // 캐시삭제 검증

    }

    @Test
    void 임시예약이_취소되면_soft_delete_처리() {
        // when
        reservationMaintenanceUsecase.deleteCanceledReservations();

        // then
        verify(reservationService, times(1)).deleteCanceledReservations();
    }
}
