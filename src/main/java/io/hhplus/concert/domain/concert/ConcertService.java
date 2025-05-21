package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.hhplus.concert.interfaces.api.common.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final ConcertSeatRepository concertSeatRepository;

    /**
     * 콘서트 목록조회
     * - 레디스키: concert:list
     *
     */
    @Transactional(readOnly = true)
    public ConcertInfo.GetConcertList getConcertList() {
		return concertRepository.findAll();
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
        // 캐시미스일 경우 - 데이터베이스로부터 리스트결과를 가져온후에 캐시에 저장한다.
		return concertDateRepository.findAllAvailable(command.concertId());
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
		return concertSeatRepository.findConcertSeats(
			command.concertId(),
			command.concertDateId()
		);
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

    public Optional<ConcertSeat> findConcertSeatById(long id) {
        return concertSeatRepository.findById(id);
    }
    public Concert findConcertById(long id) {
        return concertRepository.findById(id);
    }

    public ConcertSeat saveOrUpdate(ConcertSeat concertSeat) {
        return concertSeatRepository.saveOrUpdate(concertSeat);
    }


    public long countTotalSeats(ConcertCommand.CountTotalSeats command) {
        long concertId = command.concertId();
        long concertDateId = command.concertDateId();
        // 콘서트일정 조회
        ConcertDate concertDate =concertDateRepository.findConcertDateById(concertDateId);
        if(concertDate == null) throw new BusinessException(CONCERT_DATE_NOT_FOUND);

        // 콘서트 좌석의 전체 개수를 구한다.
        ConcertInfo.GetConcertSeatList info = concertSeatRepository.findConcertSeats(concertId, concertDateId);
        long total = info.concertSeatList().size();
        if(total == 0) throw new BusinessException(CONCERT_SEAT_NOT_FOUND);
        return total;
    }
    public ConcertDate soldOut(long concertDateId) {
        ConcertDate concertDate = concertDateRepository.findConcertDateById(concertDateId);
        if(concertDate == null) throw new BusinessException(CONCERT_DATE_NOT_FOUND);

        concertDate.soldOut(); // 상태변경
        return concertDateRepository.save(concertDate);
    }

}
