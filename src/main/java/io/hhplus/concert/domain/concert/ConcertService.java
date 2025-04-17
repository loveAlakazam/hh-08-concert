package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;

import java.util.List;
import java.util.Optional;

import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final ConcertSeatRepository concertSeatRepository;


    /**
     * 콘서트 목록조회
     *
     */
    public ConcertInfo.GetConcertList getConcertList() {
        // 리스트결과를 가져온다.
        List<Concert> concerts = concertRepository.findAll();
        return ConcertInfo.GetConcertList.from(concerts);
    }
    /**
     * 콘서트 날짜 목록 조회
     *
     * @return ConcertInfo.GetConcertDateList
     */
    public ConcertInfo.GetConcertDateList getConcertDateList(ConcertCommand.GetConcertDateList command) {
        // 리스트결과를 가져온다
        List<ConcertDate> concertDates =  concertDateRepository.findAll(command.concertId());
        return ConcertInfo.GetConcertDateList.from(concertDates);
    }
    /**
     * 콘서트 좌석 목록 조회
     *
     * @param command
     * @return ConcertInfo.GetConcertSeatList
     */
    public ConcertInfo.GetConcertSeatList getConcertSeatList(ConcertCommand.GetConcertSeatList command) {
        List<ConcertSeat> concertSeatList = concertSeatRepository.findConcertSeats(
            command.concertId(),
            command.concertDateId()
        );
        return ConcertInfo.GetConcertSeatList.from(concertSeatList);
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
