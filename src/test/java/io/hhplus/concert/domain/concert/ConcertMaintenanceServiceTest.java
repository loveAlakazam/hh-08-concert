package io.hhplus.concert.domain.concert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.mockito.Mockito.*;

import io.hhplus.concert.domain.support.JsonSerializer;
import io.hhplus.concert.infrastructure.persistence.snapshots.RedisRankingSnapshotJpaRepository;

@ExtendWith(MockitoExtension.class)
public class ConcertMaintenanceServiceTest {
    @InjectMocks
    private ConcertMaintenanceService concertMaintenanceService;
    @Mock
    private ConcertDateRepository concertDateRepository;
    @Mock
    private ConcertSeatRepository concertSeatRepository;
    @Mock
    private ConcertRankingRepository concertRankingRepository;
    @Mock
    private JsonSerializer jsonSerializer;
    @Mock
    private RedisRankingSnapshotJpaRepository snapshotRepository;


    @BeforeEach
    void setUp() {
        concertMaintenanceService = new ConcertMaintenanceService(
            concertDateRepository,
            concertSeatRepository,
            concertRankingRepository,
            jsonSerializer,
            snapshotRepository
        );
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
}
