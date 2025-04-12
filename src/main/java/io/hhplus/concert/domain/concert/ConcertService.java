package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.common.BaseEntity.*;
import static io.hhplus.concert.domain.concert.ConcertExceptionMessage.*;

import java.util.List;
import java.util.stream.Collectors;

import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.common.exceptions.NotFoundException;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertDateResponse;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertResponse;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertSeatDetailResponse;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertSeatResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final ConcertSeatRepository concertSeatRepository;

    /**
     * @deprecated
     * 콘서트 목록 조회
     *
     * @return List<ConcertResponse>
     */
    public List<ConcertResponse> getConcertList() {
        List<Concert> concerts = concertRepository.findAll();
        return concerts.stream().map( concert -> ConcertResponse.of(
            concert.getId(),
            concert.getName(),
            concert.getArtistName()
        )).collect(Collectors.toList());
    }
    /**
     * 콘서트 목록조회 + offset 기반 페이지네이션
     *
     * @param page - 조회 페이지
     * @throws InvalidValidationException
     */
    public List<ConcertResponse> getConcertList(int page) {
        Concert.validatePage(page);
        Pageable pageable = PageRequest.of(page -1, PAGE_SIZE);
        Page<Concert> concertPage = concertRepository.findAll(pageable);

        return concertPage.getContent()
            .stream()
            .map(
                concert -> ConcertResponse.of(
                    concert.getId(),
                    concert.getName(),
                    concert.getArtistName()
            )).collect(Collectors.toList());
    }

    /**
     * 콘서트 날짜 목록 조회 + offset기반 페이지네이션
     *
     * @param concertId - 콘서트 PK
     * @return List<ConcertDateResponse>
     * @throws InvalidValidationException
     */
    public List<ConcertDateResponse> getConcertDateList(int page, long concertId) {
        ConcertDate.validatePage(page);
        Pageable pageable = PageRequest.of(page -1, PAGE_SIZE);
        Page<ConcertDate> concertDatePage = concertDateRepository.findAll(concertId, pageable);

        return concertDatePage.getContent().stream().map(
            concertDate-> ConcertDateResponse.of(
                concertDate.getId(),
                concertDate.getProgressDate(),
                concertDate.isAvailable(),
                concertDate.getPlace()
            )).collect(Collectors.toList());
    }

    /**
     * 콘서트 좌석 목록 조회
     *
     * @param concertId - 콘서트 PK
     * @param concertDateId - 콘서트날짜 PK
     * @return List<ConcertSeatResponse>
     */
    public List<ConcertSeatResponse> getConcertSeatList(long concertId, long concertDateId) {
        return concertSeatRepository.findConcertSeats(concertId, concertDateId)
            .stream()
            .map(concertSeat -> ConcertSeatResponse.of(
                concertSeat.getId(),
                concertSeat.getNumber(),
                concertSeat.getPrice(),
                concertSeat.isAvailable()
            )).collect(Collectors.toList());
    }

    /**
     * 콘서트 좌석 세부 정보 조회
     * @param concertSeatId - 콘서트좌석 PK
     * @return ConcertSeatDetailResponse
     * @throws NotFoundException
     */
    public ConcertSeatDetailResponse getConcertSeatInfo(long concertSeatId ) {
        ConcertSeatDetailResponse concertSeatInfo = concertSeatRepository.getConcertSeatInfo(concertSeatId);
        if (concertSeatInfo == null) throw new NotFoundException(CONCERT_SEAT_NOT_FOUND);
        return concertSeatInfo;
    }

    /**
     * 식별자(id)로 콘서트 도메인 엔티티를 호출
     *
     * @param id - 콘서트 PK
     * @return Concert | null
     */
    public Concert getConcertEntityById(long id) {
        return concertRepository.findConcertById(id);
    }

    /**
     * 식별자(id)로 콘서트날짜 도메인 엔티티를 호출
     *
     * @param id - 콘서트날짜 PK
     * @return ConcertDate | null
     */
    public ConcertDate getConcertDateEntityById(long id) {
        return concertDateRepository.findConcertDateById(id);
    }

    /**
     * 식별자(id)로 콘서트좌석 도메인 엔티티를 호출
     *
     * @param id - 콘서트좌석 PK
     * @return ConcertSeat | null
     */
    public ConcertSeat getConcertSeatEntityById(long id) {
        ConcertSeat concertSeat = concertSeatRepository.findConcertSeatById(id);
        if(concertSeat == null) throw new NotFoundException(CONCERT_SEAT_NOT_FOUND);
        return concertSeat;
    }

    /**
     * 좌석정보 생성 및 변경
     *
     * @param concertSeat - 좌석도메인
     * @return ConcertSeat
     */
    public ConcertSeat saveOrUpdateConcertSeat(ConcertSeat concertSeat) {
        return concertSeatRepository.saveOrUpdate(concertSeat);
    }
}
