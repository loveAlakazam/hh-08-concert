package io.hhplus.concert.domain.reservation;

import io.hhplus.concert.domain.concert.Concert;
import io.hhplus.concert.domain.concert.ConcertDate;
import io.hhplus.concert.domain.concert.ConcertInfo;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.hhplus.concert.domain.concert.ConcertService.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class ReservationMaintenanceServiceTest {
    @InjectMocks
    private ReservationMaintenanceService reservationMaintenanceService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ConcertSeatRepository concertSeatRepository;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOps;
    @Mock
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        reservationMaintenanceService = new ReservationMaintenanceService(
            reservationRepository,
            concertSeatRepository,
            redisTemplate,
            objectMapper
        );
    }
    private static final Logger log = LoggerFactory.getLogger(ReservationMaintenanceServiceTest.class);

    @Test
    void 임시예약날짜가_만료되면_예약상태는_취소상태로_변경되며_좌석도_예약가능으로_변경된다() throws InterruptedException {
        // given
        long userId = 1L;
        long concertId = 1L;
        long concertDateId = 1L;
        long concertSeatId = 1L;

        User user = User.of("예약자1");
        Concert concert = Concert.create("테스트콘서트", "아티스트명", LocalDate.now(), "콘서트장소", 2000);
        ConcertDate concertDate = concert.getDates().get(0);
        ConcertSeat concertSeat = concertDate.getSeats().get(0);

        Reservation reservation = Reservation.of(user,concert, concertDate, concertSeat);
        reservation.temporaryReserve(); // 임시에약상태로 변경
        reservation.expireTemporaryReserve(LocalDateTime.now().minusSeconds(1)); // 임시예약상태를 만료
        List<Reservation> expiredReservations = List.of(reservation);

        ReflectionTestUtils.setField(user, "id", userId); // user 아이디 수동설정
        ReflectionTestUtils.setField(concert, "id", concertId); // concert 아이디 수동설정
        ReflectionTestUtils.setField(concertDate, "id", concertDateId); // concertDate 아이디 수동설정
        ReflectionTestUtils.setField(concertSeat, "id", concertSeatId); // concertSeat 아이디 수동설정

        when(reservationRepository.findExpiredTempReservations()).thenReturn(expiredReservations); // 임시예약유효일자가 만료된 예약은 1개
        doAnswer(invocation -> {
            reservation.cancel();
            return reservation;
        }).when(reservationRepository).updateCanceledExpiredTempReservations(); // 예약상태를 '취소' 로 변경

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(concertSeatRepository.findConcertSeats(concertId, concertDateId)).thenReturn(
          ConcertInfo.GetConcertSeatList.from(concertDate.getSeats())
        );

        //when
        reservationMaintenanceService.cancel();

        // then
        verify(reservationRepository, times(1)).updateCanceledExpiredTempReservations();

        assertEquals(ReservationStatus.CANCELED, reservation.getStatus(), "예약상태는 취소상태이다");
        assertFalse(reservation.isTemporary(), "취소상태로 변경됐으므로 임시예약상태가 아니다");
        assertTrue(DateValidator.isPastDateTime(reservation.getTempReservationExpiredAt()), "만료일자는 현재보다 이전이어야한다.");
        assertNull( reservation.getReservedAt(), "예약확정일은 없어야한다");

        assertTrue(reservation.getConcertSeat().isAvailable(), "좌석은 다시 예약이 가능하다");

        String cacheKey = CONCERT_SEAT_LIST_CACHE_KEY + "-" + "concert_id:" + concertId +"-" + "concert_date_id:" + concertDateId;
        verify(valueOps).set(eq(cacheKey), any(ConcertInfo.GetConcertSeatList.class), any());
    }

    @Test
    void 임시예약이_취소되면_soft_delete_처리() {
        // when
        reservationMaintenanceService.deleteCanceledReservations();

        // then
        verify(reservationRepository, times(1)).deleteCanceledReservations();
    }
}
