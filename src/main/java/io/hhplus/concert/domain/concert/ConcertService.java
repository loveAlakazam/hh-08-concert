package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final ConcertSeatRepository concertSeatRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public static final String CONCERT_LIST_CACHE_KEY = "concert:list";
    private static final Duration CONCERT_LIST_CACHE_TTL = Duration.ofHours(1);

    public static final String CONCERT_DATE_LIST_CACHE_KEY= "concert_date:list";
    private static final Duration CONCERT_DATE_LIST_CACHE_TTL = Duration.ofMinutes(30);

    public static final String CONCERT_SEAT_LIST_CACHE_KEY= "concert_seat:list";
    private static final Duration CONCERT_SEAT_LIST_CACHE_TTL= Duration.ofMinutes(5);

    /**
     * 콘서트 목록조회
     * - 레디스키: concert:list
     *
     */
    @Transactional(readOnly = true)
    public ConcertInfo.GetConcertList getConcertList() {
        // 캐시조회
        Object cachedRaw = redisTemplate.opsForValue().get(CONCERT_LIST_CACHE_KEY);
        if(cachedRaw != null) {
			return objectMapper.convertValue(cachedRaw, ConcertInfo.GetConcertList.class);
        }

        // 캐시미스일 경우 - 데이터베이스로부터 리스트결과를 가져온후에 캐시에 저장한다.
        ConcertInfo.GetConcertList concerts = concertRepository.findAll();
        redisTemplate.opsForValue().set(CONCERT_LIST_CACHE_KEY, concerts, CONCERT_LIST_CACHE_TTL);
        return concerts;
    }
    /**
     * 예약가능한 콘서트 날짜 목록 조회
     * * 레디스키: concert_date:list-concert_id:{concertId}
     * * 콘서트별로 콘서트날짜목록을 가지고 있으므로 concertId 로 구분이 필요.
     *
     * @return ConcertInfo.GetConcertDateList
     *
     *
     */
    public ConcertInfo.GetConcertDateList getConcertDateList(ConcertCommand.GetConcertDateList command) {
        // 캐시 조회
        String cacheKey = CONCERT_DATE_LIST_CACHE_KEY + "-" + "concert_id:" + command.concertId();
        Object cachedRaw = redisTemplate.opsForValue().get(cacheKey);
        if(cachedRaw != null) {
            return objectMapper.convertValue(cachedRaw, ConcertInfo.GetConcertDateList.class);
        }

        // 캐시미스일 경우 - 데이터베이스로부터 리스트결과를 가져온후에 캐시에 저장한다.
        ConcertInfo.GetConcertDateList concertDates =  concertDateRepository.findAllAvailable(command.concertId());
        redisTemplate.opsForValue().set(cacheKey, concertDates, CONCERT_DATE_LIST_CACHE_TTL);
        return concertDates;
    }
    /**
     * 콘서트 좌석 목록 조회
     * * 레디스키: concert_seat:list-concert_id:{concertId}-concert_date_id:{concertDateId}
     * * 콘서트별로 콘서트날짜목록을 갖고, 콘서트날짜별로 콘서트좌석목록(좌석50개)를 가지므로 concertId와 concertDate 로 구분이 필요.
     *
     * @param command
     * @return ConcertInfo.GetConcertSeatList
     */
    public ConcertInfo.GetConcertSeatList getConcertSeatList(ConcertCommand.GetConcertSeatList command) {
        // 캐시조회
        String cacheKey = CONCERT_SEAT_LIST_CACHE_KEY + "-" + "concert_id:" + command.concertId() +"-" + "concert_date_id:" + command.concertDateId();
        Object cachedRaw = redisTemplate.opsForValue().get(cacheKey);
        if(cachedRaw != null) {
            return objectMapper.convertValue(cachedRaw, ConcertInfo.GetConcertSeatList.class);
        }

        // 캐시 미스일경우 - 데이터베이스로부터 리스트결과를 가져온후에 캐시에 저장한다.
        ConcertInfo.GetConcertSeatList concertSeats = concertSeatRepository.findConcertSeats(
            command.concertId(),
            command.concertDateId()
        );
        redisTemplate.opsForValue().set(cacheKey, concertSeats, CONCERT_SEAT_LIST_CACHE_TTL);
        return concertSeats;
    }
    /**
     * 콘서트 좌석 세부 정보 조회
     * @param command
     * @return ConcertInfo.GetConcertSeat
     * @throws BusinessException
     */
    public ConcertInfo.GetConcertSeat getConcertSeat(ConcertCommand.GetConcertSeat command ) {
         ConcertSeat result =  concertSeatRepository.getConcertSeatInfo(command.concertSeatId());
        if(result == null) throw new BusinessException(CONCERT_SEAT_NOT_FOUND);
        if(!result.isAvailable()) throw new BusinessException(ALREADY_RESERVED_SEAT);
        return ConcertInfo.GetConcertSeat.from(result);
    }
    /**
     * 공연, 공연일정, 공연좌석(50석) 을 초기화 시켜 공연정보를 생성한다
     *
     * @param command
     * @return
     */
    public ConcertInfo.CreateConcert create(ConcertCommand.CreateConcert command) {
        Concert concert = Concert.create(
            command.name(),
            command.artistName(),
            command.progressDate(),
            command.place(),
            command.price()
        );
        concertRepository.saveOrUpdate(concert);
        return ConcertInfo.CreateConcert.from(concert);
    }
    public ConcertInfo.AddConcertDate addConcertDate(ConcertCommand.AddConcertDate command) {
        // 콘서트정보를 구한다.
        Concert concert = concertRepository.findById(command.id());
        concert.addConcertDate(command.progressDate(), command.place(), command.price());

        concertRepository.saveOrUpdate(concert);
        return ConcertInfo.AddConcertDate.from(concert);
    }

    public Optional<ConcertSeat> findById(long id) {
        return concertSeatRepository.findById(id);
    }

    public ConcertSeat saveOrUpdate(ConcertSeat concertSeat) {
        return concertSeatRepository.saveOrUpdate(concertSeat);
    }

}
