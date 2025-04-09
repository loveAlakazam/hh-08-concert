package io.hhplus.concert.domain.concert.service;

import static io.hhplus.concert.domain.common.entity.BaseEntity.*;

import java.util.List;
import java.util.stream.Collectors;

import io.hhplus.concert.domain.concert.entity.Concert;
import io.hhplus.concert.domain.concert.entity.ConcertDate;
import io.hhplus.concert.domain.concert.repository.ConcertDateRepository;
import io.hhplus.concert.domain.concert.repository.ConcertRepository;
import io.hhplus.concert.domain.concert.repository.ConcertSeatRepository;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertDateResponse;
import io.hhplus.concert.interfaces.api.concert.dto.ConcertResponse;
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
     */
    public List<ConcertResponse> getConcertList(int page) {
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
     */
    public List<ConcertDateResponse> getConcertDateList(int page, long concertId) {
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

}
