package io.hhplus.concert.domain.concert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConcertMaintenanceServiceTest {
    @InjectMocks
    private ConcertMaintenanceService concertMaintenanceService;
    @Mock
    private ConcertDateRepository concertDateRepository;
    @Mock
    private ConcertSeatRepository concertSeatRepository;

    @BeforeEach
    void setUp() {
        concertMaintenanceService = new ConcertMaintenanceService(concertDateRepository, concertSeatRepository);
    }

    private static final Logger log = LoggerFactory.getLogger(ConcertMaintenanceServiceTest.class);

    @Test
    void 콘서트일정중_현재기준으로_지난날짜가_있다면_soft_delete를_수행한다() {
        // given
        List<Long> finishedConcertDateIds = List.of(101L, 102L);
        when(concertDateRepository.findFinishedConcertDateIds()).thenReturn(finishedConcertDateIds);
        // when
        concertMaintenanceService.deletePastConcertDates();
        // then
        for(Long id: finishedConcertDateIds) {
            // 삭제 메소드 호출확인
            verify(concertSeatRepository).deleteConcertSeatByConcertDateId(id);
            // 공연일정 삭제 호출확인
            verify(concertDateRepository).deleteConcertDate(id);
        }
        // 추가적으로 더이상 호출이 없음을 보장한다
        verifyNoMoreInteractions(concertSeatRepository, concertDateRepository);
    }
    @Test
    void 공연일정이_전부만석인_일정이_존재한다면_예약가능여부를_false로_지정해야한다() {
        // given
        LocalDate now = LocalDate.now();
        Concert concert = Concert.create("테스트콘서트", "아티스트이름", now, "콘서트 장소", 5000);
        concert.addConcertDate(now.plusDays(7), "콘서트장소 2", 3000);
        List<ConcertDate> concertDates = concert.getDates();
        assertEquals(2, concertDates.size());

        // 0번째 콘서트는 예약이 가능함.
        ConcertDate availableConcertDate = concertDates.get(0);
        assertEquals(50, availableConcertDate.countAvailableSeats()); // 잔여좌석: 50석
        // 1번째 콘서트일정은 좌석이 매진 상태
        ConcertDate soldOutConcertDate = concertDates.get(1);
        for(ConcertSeat concertSeat : soldOutConcertDate.concertSeats()) {
            concertSeat.reserve();
        }
        assertEquals(0, soldOutConcertDate.countAvailableSeats()); // 잔여좌석 0 석

        when(concertDateRepository.findAllNotDeleted()).thenReturn(concertDates);

        // when
        concertMaintenanceService.checkSoldOut();

        // then
        assertTrue(availableConcertDate.isAvailable());
        assertFalse(soldOutConcertDate.isAvailable());
        verify(concertDateRepository, times(2)).save(any(ConcertDate.class));
    }
}
